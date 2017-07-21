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

package edu.cmu.sv.isstac.sampling.distributed.rmi;

import com.google.common.base.Preconditions;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.AnalysisException;
import edu.cmu.sv.isstac.sampling.distributed.SamplingWorker;
import edu.cmu.sv.isstac.sampling.distributed.WorkerResult;
import edu.cmu.sv.isstac.sampling.distributed.WorkerStatistics;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class Worker implements RMIWorker {

  private static final Logger LOGGER = JPFLogger.getLogger(Worker.class.getName());

  private transient SamplingWorker worker;
  private final String id;

  public static void main(String args[]) throws RemoteException, NotBoundException, MalformedURLException {
    int port = Utils.DEFAULT_MASTER_PORT;
    String hostname = Utils.DEFAULT_HOSTNAME;

    String workerID = "";

    if(args.length < 1) {
      printUsage();
      System.exit(1);
    } else {
      workerID = args[0];
    }
    if(args.length > 1) {
      hostname = args[1];
      try {
        port = Integer.parseInt(args[2]);
      } catch (NumberFormatException e) {
        printUsage();
        throw e;
      }
    }
    String registryURL = "rmi://" + hostname + ":" + port + "/" + Utils.SERVICE_NAME;
    LOGGER.info("Looking up registry url: " + registryURL);
    RMIMaster server = (RMIMaster) Naming.lookup(registryURL);

    RMIWorker rmiWorker = new Worker(workerID);
    boolean ok = server.register(rmiWorker);
    if(!ok) {
      LOGGER.severe("Worker with ID " + workerID + " could not register with master");
      System.exit(1);
    }
    LOGGER.info("Worker with ID " + workerID + " registered with master");
  }

  public static void printUsage() {
    System.err.println("Usage: " + Worker.class.getSimpleName() + " <ID> " + "[<hostname> <port>]");
  }

  public Worker(String id) throws RemoteException {
    this.id = id;
  }

  @Override
  public String getID() throws RemoteException {
    return this.id;
  }

  @Override
  public WorkerResult runAnalysis(Path frontierNode, Config config) throws RemoteException {
    LOGGER.info("Worker " + this.getID() + " running with frontier " + frontierNode.toString());
    this.worker = new SamplingWorker();
    Preconditions.checkNotNull(this.worker);
    try {
      return this.worker.runAnalysis(frontierNode, config);
    } catch (AnalysisCreationException e) {
      LOGGER.severe(e.getMessage());
      throw new AnalysisException(e);
    }
  }

  @Override
  public void terminate() throws RemoteException {
    UnicastRemoteObject.unexportObject(this, true);
  }

  @Override
  public WorkerStatistics getStatus() throws RemoteException {
    return this.worker.getStatus();
  }
}
