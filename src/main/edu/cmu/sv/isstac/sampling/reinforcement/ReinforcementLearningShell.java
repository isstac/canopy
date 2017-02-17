package edu.cmu.sv.isstac.sampling.reinforcement;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.structure.DefaultNodeFactory;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import edu.cmu.sv.isstac.sampling.termination.SampleSizeTerminationStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;

/**
 * @author Kasper Luckow
 *
 */
public class ReinforcementLearningShell implements JPFShell {

  private final SamplingAnalysis samplingAnalysis;
  
  //ctor required for jpf shell
  public ReinforcementLearningShell(Config config) throws ModelCounterCreationException,
      AnalysisCreationException {
    SamplingAnalysis.Builder samplingAnalysisBuilder = new SamplingAnalysis.Builder();

    int samplesPerOptimization = config.getInt(Utils.SAMPLES_PER_OPTIMIZATION,
        Utils.DEFAULT_SAMPLES_PER_OPTIMIZATION);

    double epsilon = config.getDouble(Utils.EPSILON, Utils.DEFAULT_EPSILON);
    double historyWeight = config.getDouble(Utils.HISTORY, Utils.DEFAULT_HISTORY);

    NodeFactory<RLNode> factory;

    if(config.getBoolean(Utils.USE_MODELCOUNTING, Utils.DEFAULT_USE_MODELCOUNTING)) {
      SPFModelCounter modelCounter = ModelCounterFactory.getInstance(config);
      factory = new RLNodeFactoryMCDecorator(modelCounter);
    } else {
      factory = new RLNodeFactory();
    }

    this.samplingAnalysis = samplingAnalysisBuilder.build(config,
        new ReinforcementLearningStrategy(samplesPerOptimization, epsilon,
            historyWeight, factory, Options.getSeed(config)), new JPFSamplerFactory());
  }

  @Override
  public void start(String[] args) {
    samplingAnalysis.run();
  }
}
