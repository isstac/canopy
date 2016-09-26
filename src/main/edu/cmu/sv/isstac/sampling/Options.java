package edu.cmu.sv.isstac.sampling;

import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import edu.cmu.sv.isstac.sampling.reward.DepthRewardFunction;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.termination.NeverTerminateStrategy;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;

/**
 * @author Kasper Luckow
 */
public class Options {
  public static final String SAMPLING_CONF_PREFIX = "symbolic.security.sampling";

  public static final String REWARD_FUNCTION = SAMPLING_CONF_PREFIX + ".rewardfunc";
  public static final String PATH_QUANTIFIER = SAMPLING_CONF_PREFIX + ".pathquantifier";

  public static final String TERMINATION_STRATEGY = SAMPLING_CONF_PREFIX + ".termination";

  public static final String MODEL_COUNTING_PREFIX = SAMPLING_CONF_PREFIX + ".modelcounting";
  public static final String USE_MODELCOUNT_AMPLIFICATION = MODEL_COUNTING_PREFIX +
      ".amplifyrewards";

  public static final String EVENT_OBSERVERS = SAMPLING_CONF_PREFIX + ".eventobservers";

  public static final String SHOW_LIVE_STATISTICS = SAMPLING_CONF_PREFIX + ".livestats";
  public static final String SHOW_STATISTICS = SAMPLING_CONF_PREFIX + ".stats";

  // Defaults
  public static final RewardFunction DEFAULT_REWARD_FUNCTION = new DepthRewardFunction();
  public static final TerminationStrategy DEFAULT_TERMINATION_STRATEGY
      = new NeverTerminateStrategy();

  public static final String CHOICES_STRATEGY = SAMPLING_CONF_PREFIX + ".choicesstrategy";
  public static final ChoicesStrategy DEFAULT_CHOICES_STRATEGY = PruningChoicesStrategy.getInstance();

  public static final boolean DEFAULT_USE_MODELCOUNT_AMPLIFICATION = true;

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

}
