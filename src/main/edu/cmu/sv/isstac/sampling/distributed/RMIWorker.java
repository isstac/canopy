/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
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

package edu.cmu.sv.isstac.sampling.distributed;

import com.google.common.base.Preconditions;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.AnalysisException;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class RMIWorker implements Worker {

  private static final Logger LOGGER = JPFLogger.getLogger(RMIWorker.class.getName());

  private final SamplingWorker worker;

  // Not sure if this is needed or not according to the RMI spec
  public RMIWorker() throws RemoteException {
    this.worker = null;
  }

  public RMIWorker(SamplingWorker worker) throws RemoteException {
    this.worker = worker;
  }

  @Override
  public String getID() throws RemoteException {
    return null;
  }

  @Override
  public WorkerResult runAnalysis(Path frontierNode, Config config) throws RemoteException {
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
    Preconditions.checkNotNull(this.worker);

  }

  @Override
  public WorkerStatistics getStatus() throws RemoteException {
    return this.worker.getStatus();
  }
}
