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

package edu.cmu.sv.isstac.canopy;

import java.util.Random;

import edu.cmu.sv.isstac.canopy.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.canopy.exploration.TrieBasedPruningStrategy;
import edu.cmu.sv.isstac.canopy.exploration.cache.TrieCache;
import edu.cmu.sv.isstac.canopy.reward.DepthRewardFunction;
import edu.cmu.sv.isstac.canopy.exploration.cache.StateCache;
import edu.cmu.sv.isstac.canopy.termination.NeverTerminateStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.numeric.solvers.IncrementalListener;

/**
 * @author Kasper Luckow
 */
public class Options {
  public static final String SAMPLING_CONF_PREFIX = "canopy";

  public static final String REWARD_FUNCTION = SAMPLING_CONF_PREFIX + ".rewardfunc";
  public static final String PATH_QUANTIFIER = SAMPLING_CONF_PREFIX + ".pathquantifier";

  public static final String TERMINATION_STRATEGY = SAMPLING_CONF_PREFIX + ".termination";

  public static final String SAMPLING_SIZE_TERMINATION_STRATEGY = TERMINATION_STRATEGY +
      ".samplingsize";

  public static final String MODEL_COUNTING_PREFIX = SAMPLING_CONF_PREFIX + ".modelcounting";
  public static final String USE_MODELCOUNT_AMPLIFICATION = MODEL_COUNTING_PREFIX +
      ".amplifyrewards";

  public static final String EVENT_OBSERVERS = SAMPLING_CONF_PREFIX + ".eventobservers";

  public static final String SHOW_LIVE_STATISTICS = SAMPLING_CONF_PREFIX + ".livestats";
  public static final String SHOW_LIVE_STATISTICS_BUDGET = SHOW_LIVE_STATISTICS + ".budget";
  public static final String SHOW_STATISTICS = SAMPLING_CONF_PREFIX + ".stats";

  // Defaults. We rely on Config's instantiation that uses reflection and passes jpf config
  public static final String DEFAULT_REWARD_FUNCTION = DepthRewardFunction.class.getName();
  public static final String DEFAULT_TERMINATION_STRATEGY = NeverTerminateStrategy
      .class.getName();


  public static final String CHOICES_STRATEGY = SAMPLING_CONF_PREFIX + ".choicesstrategy";
  public static final ChoicesStrategy DEFAULT_CHOICES_STRATEGY = TrieBasedPruningStrategy.getInstance();

  public static final String STATE_CACHE = SAMPLING_CONF_PREFIX + ".statecache";
  public static final Class<? extends StateCache> DEFAULT_STATE_CACHE = TrieCache.class;

  public static final String USE_BACKTRACKING_SEARCH = SAMPLING_CONF_PREFIX + ".backtrackingsearch";

  public static final boolean DEFAULT_USE_BACKTRACKING_SEARCH = true;
  public static final boolean DEFAULT_USE_MODELCOUNT_AMPLIFICATION = false;

  public static final boolean DEFAULT_SHOW_STATISTICS = true;
  public static final boolean DEFAULT_SHOW_LIVE_STATISTICS = true;

  // For MCTS and Monte Carlo
  public static final String RNG_SEED = SAMPLING_CONF_PREFIX + ".seed";
  public static final long DEFAULT_RNG_SEED = 15485863;
  public static final String RNG_RANDOM_SEED = SAMPLING_CONF_PREFIX + ".random";
  public static final boolean DEFAULT_RANDOM_SEED = false;


  //TODO: Fix this state. It is added because we cannot obtain an instance of SamplingSearch and
  // thus we have to pass "parameters" to it as statics
  public static ChoicesStrategy choicesStrategy;

  // Utility method for obtaining the seed from the config
  public static long getSeed(Config conf) {
    boolean useRandomSeed = conf.getBoolean(Options.RNG_RANDOM_SEED, Options.DEFAULT_RANDOM_SEED);
    if(useRandomSeed) {
      return new Random().nextLong();
    } else {
      return conf.getLong(Options.RNG_SEED, Options.DEFAULT_RNG_SEED);
    }
  }

  public static void resetIncrementalSolver() {
    //reset incremental solver if used
    assert IncrementalListener.solver != null;
    IncrementalListener.solver.reset();
  }
}
