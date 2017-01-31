package edu.cmu.sv.isstac.sampling.mcts;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.visualization.SymTreeVisualizer;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;

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
      mcts.addObserver(new SymTreeVisualizer());
    }

    this.samplingAnalysis = analysisBuilder.build(config, mcts, new JPFSamplerFactory());
  }

  @Override
  public void start(String[] args) {
    this.samplingAnalysis.run();
  }

}
