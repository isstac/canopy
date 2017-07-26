/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.cmu.sv.isstac.canopy.distributed.rmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.canopy.AnalysisCreationException;
import edu.cmu.sv.isstac.canopy.AnalysisException;
import edu.cmu.sv.isstac.canopy.distributed.WorkerResult;
import edu.cmu.sv.isstac.canopy.exploration.Path;
import edu.cmu.sv.isstac.canopy.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class Server implements RMIMaster, JPFShell {

  private static final Logger LOGGER = JPFLogger.getLogger(Server.class.getName());

  private Map<String, RMIWorker> workers = new HashMap<>();
  private final Config config;

  private static Registry registry;

  private ExecutorService threadPool;
  private CompletionService<WorkerResult> pool;

  private int numberOfWorkers;

  //ctor required for jpf shell
  public Server(Config config) throws AnalysisCreationException,
      ModelCounterCreationException, RemoteException {
    this.config = config;
    System.setProperty("java.rmi.server.hostname", "127.0.0.1");
    int port = config.getInt(Utils.MASTER_PORT_CONF, Utils.DEFAULT_MASTER_PORT);

    RMIMaster stub = (RMIMaster) UnicastRemoteObject.exportObject(this, port);
    registry = LocateRegistry.createRegistry(Utils.DEFAULT_MASTER_PORT);//LocateRegistry
    // .getRegistry();
    try {
      registry.rebind(Utils.SERVICE_NAME, stub);
    } catch(Exception e) {
      throw new RemoteException("Did you forget to run rmiregistry?", e);
    }
    LOGGER.info(Utils.SERVICE_NAME + " bound to RMI registry");

    this.numberOfWorkers = config.getInt(Utils.CLIENTS_NUM_CONF);
    LOGGER.info("Expecting " + this.numberOfWorkers + " number of workers to connect");

    // make threadpool
    this.threadPool = Executors.newFixedThreadPool(numberOfWorkers);
    this.pool = new ExecutorCompletionService<>(threadPool);
  }

  @Override
  public synchronized boolean register(RMIWorker worker) throws RemoteException {
    if(!workers.containsKey(worker.getID())) {
      workers.put(worker.getID(), worker);
      LOGGER.info("Registered worker: " + worker.getID());
      notifyAll();
      return true;
    } else {
      LOGGER.warning("Did *not* register worker: " + worker.getID());
      return false;
    }
  }

  @Override
  public synchronized boolean unregister(RMIWorker worker) throws RemoteException {
    if(!workers.containsKey(worker.getID())) {
      workers.remove(worker.getID());
      LOGGER.info("Unregistered worker: " + worker.getID());
      notifyAll();
      return true;
    } else {
      LOGGER.warning("Did *not* unregister worker: " + worker.getID());
      return false;
    }
  }

  private Path[] getFrontierNodes() {
    Path[] frontiers = new Path[2];
    List<Integer> first = new LinkedList<>();
    first.add(0);
    first.add(0);
    first.add(0);
    frontiers[0] = new Path(first);


    List<Integer> second = new LinkedList<>();
    second.add(0);
    second.add(0);
    second.add(1);
    frontiers[1] = new Path(second);
    return frontiers;
  }

  @Override
  public void start(String[] args) {
    while(this.workers.size() < this.numberOfWorkers) {
      try {
        LOGGER.info(this.workers.size() + "/" + this.numberOfWorkers + " have connected");
        synchronized (this) {
          this.wait();
        }
      } catch (InterruptedException e) {
        LOGGER.severe(e.getMessage());
        throw new AnalysisException(e);
      }
    }
    LOGGER.info("All " + this.workers.size() + " workers have connected. Starting analysis");

    Path[] frontiers = getFrontierNodes();
    int n = 0;
    for(RMIWorker worker : this.workers.values()) {
      this.pool.submit(new WorkerTask(worker, config, frontiers[n++]));
    }

    for(int i = 0; i < this.workers.size(); i++) {
      try {
				/* We will NOT proceed to scheduler improvement before
				 * ALL clients have returned, hence, the scheduler evaluation
				 * phase is currently bounded by the execution of the slowest
				 * client
				 */
        WorkerResult taskResult = pool.take().get();
        System.out.println(taskResult.toString());

      } catch (InterruptedException | ExecutionException e) {
        //We just proceed....
        LOGGER.severe(e.getMessage());
      }
    }

    //Don't allow more threads to be added to pool---wait until they complete
    this.threadPool.shutdown();

    try {
      this.threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOGGER.severe(e.getMessage());
      throw new AnalysisException(e);
    }
    LOGGER.info("Analysis done. Master terminates");
    terminateCluster();
  }

  public void terminateCluster() {
    try {
      for (String label : registry.list()) {
        Remote remote = registry.lookup(label);
        this.registry.unbind(label);
        if(remote instanceof UnicastRemoteObject) {
          UnicastRemoteObject.unexportObject(remote, true);
        }
      }
      for(RMIWorker worker : this.workers.values()) {
        worker.terminate();
      }
      UnicastRemoteObject.unexportObject(this,true);
    } catch (RemoteException | NotBoundException e) {
      LOGGER.severe(e.getMessage());
    }
  }
}
