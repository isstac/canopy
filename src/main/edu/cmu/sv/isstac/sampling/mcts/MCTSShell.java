package edu.cmu.sv.isstac.sampling.mcts;

import java.util.Random;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.policies.CountWeightedSimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.policies.UniformSimulationPolicy;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.visualization.SymTreeVisualizer;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 *
 */
public class MCTSShell implements JPFShell {

  private final SamplingAnalysis samplingAnalysis;

  //ctor required for jpf shell
  public MCTSShell(Config config) throws AnalysisCreationException, ModelCounterCreationException {
    SelectionPolicy selectionPolicy = Utils.createSelectionPolicy(config);
    SimulationPolicy simulationPolicy = Utils.createSimulationPolicy(config);

    MCTSStrategy mcts = new MCTSStrategy(selectionPolicy, simulationPolicy);

    SamplingAnalysis.Builder analysisBuilder =
        new SamplingAnalysis.Builder();

    if(config.getBoolean(Utils.USE_TREE_VISUALIZATION, Utils.DEFAULT_USE_TREE_VISUALIZATION)) {
      analysisBuilder.addEventObserver(new SymTreeVisualizer());
    }

    this.samplingAnalysis = analysisBuilder.build(config, mcts);
  }

  @Override
  public void start(String[] args) {
    this.samplingAnalysis.run();
  }

}
