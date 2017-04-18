/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.sv.isstac.sampling.distributed;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.AnalysisException;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class FrontierShell implements JPFShell {

  public static Logger logger = JPFLogger.getLogger(FrontierShell.class.getName());
  private final Config config;

  //ctor required for jpf shell
  public FrontierShell(Config config) throws AnalysisCreationException, ModelCounterCreationException {
    this.config = config;
  }

  @Override
  public void start(String[] args) {
    List<Integer> p = new LinkedList<>();
    p.add(0);
    p.add(0);
    Path path = new Path(p);
    SamplingWorker worker = new SamplingWorker();
    try {
      worker.runAnalysis(path, config);
    } catch (AnalysisCreationException e) {
      logger.severe(e.getMessage());
      throw new AnalysisException(e);
    }
  }

}
