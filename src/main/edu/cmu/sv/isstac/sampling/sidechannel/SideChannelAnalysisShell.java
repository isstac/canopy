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

package edu.cmu.sv.isstac.sampling.sidechannel;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.montecarlo.MonteCarloStrategy;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class SideChannelAnalysisShell implements JPFShell {

  public static Logger logger = JPFLogger.getLogger(SideChannelAnalysisShell.class.getName());

  private final SamplingAnalysis samplingAnalysis;

  //ctor required for jpf shell
  public SideChannelAnalysisShell(Config config) throws AnalysisCreationException, ModelCounterCreationException {

    SimulationPolicy simulationPolicy = edu.cmu.sv.isstac.sampling.montecarlo.Utils.createSimulationPolicy(config);

    ChannelCapacityListener ccListener = new ChannelCapacityListener();

    SamplingAnalysis.Builder samplingAnalysisBuilder = new SamplingAnalysis.Builder();

    samplingAnalysisBuilder.addEventObserver(ccListener);

    if(config.hasValue(Utils.CHANNEL_CAPACITY_K_CONF_PRFX)) {
      double k = config.getDouble(Utils.CHANNEL_CAPACITY_K_CONF_PRFX);
      TerminationStrategy kboundedTermination = new CapacityBoundedTerminationStrategy(k, ccListener);
      samplingAnalysisBuilder.setTerminationStrategy(kboundedTermination);
    }

    this.samplingAnalysis = samplingAnalysisBuilder.build(config, new MonteCarloStrategy
        (simulationPolicy), new JPFSamplerFactory());
  }

  @Override
  public void start(String[] args) {
    samplingAnalysis.run();
  }

}
