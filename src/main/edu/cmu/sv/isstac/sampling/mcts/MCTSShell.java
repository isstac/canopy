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

  private static final Logger logger = JPFLogger.getLogger(MCTSShell.class.getName());

  public static final String MCTS_CONF_PRFX = Options.SAMPLING_CONF_PREFIX + ".mcts";

  public static final String SELECTION_POLICY = MCTS_CONF_PRFX + ".selectionpol";
  public static final String SIMULATION_POLICY = MCTS_CONF_PRFX + ".simulationpol";
  
  //Policies conf
  public static final String UCT_BIAS = MCTS_CONF_PRFX + ".uct.bias";
  public static final double DEFAULT_UCT_BIAS = Math.sqrt(2); // Is this an appropriate value?


  public static final String USE_MODELCOUNT_WEIGHTED_SIMULATION = MCTS_CONF_PRFX +
      ".weightedsampling";

  public static final String USE_TREE_VISUALIZATION = MCTS_CONF_PRFX + ".treevisualizer";
  public static final boolean DEFAULT_USE_TREE_VISUALIZATION = false;

  private final SamplingAnalysis samplingAnalysis;

  //ctor required for jpf shell
  public MCTSShell(Config config) throws AnalysisCreationException, ModelCounterCreationException {
    SelectionPolicy selectionPolicy = createSelectionPolicy(config);
    SimulationPolicy simulationPolicy = createSimulationPolicy(config);

    MCTSStrategy mcts = new MCTSStrategy(selectionPolicy, simulationPolicy);

    SamplingAnalysis.Builder analysisBuilder =
        new SamplingAnalysis.Builder();

    if(config.getBoolean(USE_TREE_VISUALIZATION, DEFAULT_USE_TREE_VISUALIZATION)) {
      analysisBuilder.addEventObserver(new SymTreeVisualizer());
    }

    this.samplingAnalysis = analysisBuilder.build(config, mcts);
  }

  @Override
  public void start(String[] args) {
    this.samplingAnalysis.run();
  }

  private static SimulationPolicy createSimulationPolicy(Config conf)
      throws ModelCounterCreationException {
    if(conf.hasValue(SIMULATION_POLICY)) {
      return conf.getInstance(SIMULATION_POLICY, SimulationPolicy.class);
    }

    boolean useRandomSeed = conf.getBoolean(Options.RNG_RANDOM_SEED, Options.DEFAULT_RANDOM_SEED);

    SimulationPolicy simulationPolicy;
    if(useRandomSeed) {
      if(conf.getBoolean(USE_MODELCOUNT_WEIGHTED_SIMULATION)) {
        SPFModelCounter modelCounter = ModelCounterFactory.getInstance(conf);
        simulationPolicy = new CountWeightedSimulationPolicy(modelCounter);
      }
      else {
        simulationPolicy = new UniformSimulationPolicy();
      }
    } else {
      long seed = conf.getLong(Options.RNG_SEED, Options.DEFAULT_RNG_SEED);
      if(conf.getBoolean(USE_MODELCOUNT_WEIGHTED_SIMULATION)) {
        SPFModelCounter modelCounter = ModelCounterFactory.getInstance(conf);
        simulationPolicy = new CountWeightedSimulationPolicy(modelCounter, new Random(seed));
      } else {
        simulationPolicy = new UniformSimulationPolicy(seed);
      }
    }
    return simulationPolicy;
  }

  private static SelectionPolicy createSelectionPolicy(Config conf) {
    if(conf.hasValue(SELECTION_POLICY)) {
      return conf.getInstance(SELECTION_POLICY, SelectionPolicy.class);
    }

    double uctBias = conf.getDouble(UCT_BIAS, DEFAULT_UCT_BIAS);
    boolean useRandomSeed = conf.getBoolean(Options.RNG_RANDOM_SEED, Options.DEFAULT_RANDOM_SEED);

    SelectionPolicy selectionPolicy;
    if(useRandomSeed) {
      return new UCBPolicy(uctBias);
    } else {
      long seed = conf.getLong(Options.RNG_SEED, Options.DEFAULT_RNG_SEED);
      return new UCBPolicy(seed, uctBias);
    }
  }

}
