package edu.cmu.sv.isstac.sampling.montecarlo;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;

/**
 * @author Kasper Luckow
 *
 */
public class MonteCarloShell implements JPFShell {

  private final SamplingAnalysis samplingAnalysis;
  
  //ctor required for jpf shell
  public MonteCarloShell(Config config) throws ModelCounterCreationException,
      AnalysisCreationException {

    SimulationPolicy simulationPolicy = Utils.createSimulationPolicy(config);

    SamplingAnalysis.Builder samplingAnalysisBuilder = new SamplingAnalysis.Builder();

    // We never use model counting with monte carlo. It does not give anything
    config.setProperty(Options.USE_MODELCOUNT_AMPLIFICATION, Boolean.toString(false));

    this.samplingAnalysis = samplingAnalysisBuilder.build(config,
        new MonteCarloStrategy(simulationPolicy));
  }

  @Override
  public void start(String[] args) {
    samplingAnalysis.run();
  }
}
