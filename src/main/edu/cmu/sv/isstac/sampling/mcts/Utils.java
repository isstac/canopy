/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
  public static final boolean DEFAULT_USE_MODELCOUNT_WEIGHTED_SIMULATION = false;

  public static final String USE_TREE_VISUALIZATION = MCTS_CONF_PRFX + ".treevisualizer";
  public static final boolean DEFAULT_USE_TREE_VISUALIZATION = false;

  public static SimulationPolicy createSimulationPolicy(Config conf)
      throws ModelCounterCreationException {
    if(conf.hasValue(SIMULATION_POLICY)) {
      return conf.getInstance(SIMULATION_POLICY, SimulationPolicy.class);
    }

    long seed = Options.getSeed(conf);
    if(conf.getBoolean(USE_MODELCOUNT_WEIGHTED_SIMULATION,
        DEFAULT_USE_MODELCOUNT_WEIGHTED_SIMULATION)) {
      SPFModelCounter modelCounter = ModelCounterFactory.getInstance(conf);
      return new CountWeightedSimulationPolicy(modelCounter, seed);
    }
    else {
      return new UniformSimulationPolicy(seed);
    }
  }

  public static SelectionPolicy createSelectionPolicy(Config conf) {
    if(conf.hasValue(SELECTION_POLICY)) {
      return conf.getInstance(SELECTION_POLICY, SelectionPolicy.class);
    }

    double uctBias = conf.getDouble(UCT_BIAS, DEFAULT_UCT_BIAS);
    long seed = Options.getSeed(conf);
    return new UCBPolicy(seed, uctBias);
  }
}
