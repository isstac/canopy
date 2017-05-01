package edu.cmu.sv.isstac.sampling.montecarlo;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.AnalysisException;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 *
 */
public class MonteCarloShell implements JPFShell {

  private final Logger logger = JPFLogger.getLogger(MonteCarloShell.class.getName());

  private final SamplingAnalysis.Builder samplingAnalysisBuilder;
  private final SimulationPolicy simulationPolicy;
  private final Config config;

  //ctor required for jpf shell
  public MonteCarloShell(Config config) throws ModelCounterCreationException {

    simulationPolicy = Utils.createSimulationPolicy(config);
    this.config = config;
    samplingAnalysisBuilder = new SamplingAnalysis.Builder();
  }

  public void addListener(JPFListener listener) {
    this.samplingAnalysisBuilder.addListener(listener);
  }

  @Override
  public void start(String[] args) {

    SamplingAnalysis samplingAnalysis = null;
    try {
      samplingAnalysis = samplingAnalysisBuilder.build(config, new MonteCarloStrategy
          (simulationPolicy), new JPFSamplerFactory());
    } catch (AnalysisCreationException e) {
      logger.severe(e.getMessage());
      throw new AnalysisException(e);
    }
    samplingAnalysis.run();
  }
}
