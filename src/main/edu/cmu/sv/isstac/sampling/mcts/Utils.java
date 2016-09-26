package edu.cmu.sv.isstac.sampling.mcts;

import java.util.Random;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.mcts.SelectionPolicy;
import edu.cmu.sv.isstac.sampling.mcts.UCBPolicy;
import edu.cmu.sv.isstac.sampling.policies.CountWeightedSimulationPolicy;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.policies.UniformSimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class Utils {

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

  public static SimulationPolicy createSimulationPolicy(Config conf)
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

  public static SelectionPolicy createSelectionPolicy(Config conf) {
    if(conf.hasValue(SELECTION_POLICY)) {
      return conf.getInstance(SELECTION_POLICY, SelectionPolicy.class);
    }

    double uctBias = conf.getDouble(UCT_BIAS, DEFAULT_UCT_BIAS);
    boolean useRandomSeed = conf.getBoolean(Options.RNG_RANDOM_SEED, Options.DEFAULT_RANDOM_SEED);


    if(useRandomSeed) {
      return new UCBPolicy(uctBias);
    } else {
      long seed = conf.getLong(Options.RNG_SEED, Options.DEFAULT_RNG_SEED);
      return new UCBPolicy(seed, uctBias);
    }
  }
}
