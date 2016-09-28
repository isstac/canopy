package edu.cmu.sv.isstac.sampling.reinforcement;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
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
    double history = config.getDouble(Utils.HISTORY, Utils.DEFAULT_HISTORY);
    SPFModelCounter modelCounter = ModelCounterFactory.getInstance(config);
    this.samplingAnalysis = samplingAnalysisBuilder.build(config,
        new ReinforcementLearningStrategy(samplesPerOptimization, epsilon, history, modelCounter));
  }

  @Override
  public void start(String[] args) {
    samplingAnalysis.run();
  }
}
