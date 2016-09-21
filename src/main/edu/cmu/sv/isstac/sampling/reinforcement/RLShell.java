package edu.cmu.sv.isstac.sampling.reinforcement;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.search.SamplingSearch;
import edu.cmu.sv.isstac.sampling.analysis.AbstractAnalysisProcessor;
import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.LiveAnalysisStatisticsModelCounting;
import edu.cmu.sv.isstac.sampling.exploration.AllChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.policies.UniformSimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.ConcretePathQuantifier;
import edu.cmu.sv.isstac.sampling.quantification.ModelCountingPathQuantifier;
import edu.cmu.sv.isstac.sampling.quantification.PathQuantifier;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.reward.DepthRewardFunction;
import edu.cmu.sv.isstac.sampling.reward.ModelCountingAmplifierDecorator;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.termination.AllPathsTerminationStrategy;
import edu.cmu.sv.isstac.sampling.termination.SampleSizeTerminationStrategy;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 * There is a lot of redundancy between this class and MonteCarloShell.
 * We can maybe generalize this shell later if we experiment with more
 * techniques for sampling
 */
public class RLShell implements JPFShell {

  private static final Logger LOGGER = JPFLogger.getLogger(RLShell.class.getName());

  public static final String RL_CONF_PRFX = "symbolic.security.sampling.rl";

  //This setting can be used to disable sampling to exhaustively explore the tree (mostly for debugging...)
  public static final String EXHAUSTIVE_ANALYSIS = RL_CONF_PRFX + ".exhaustive";

  public static final String REWARD_FUNCTION = RL_CONF_PRFX + ".rewardfunc";
  public static final String PATH_QUANTIFIER = RL_CONF_PRFX + ".pathquantifier";

  //Reinforcement learning setup
  public static final String SCHEDULER_EVALUATIONS = RL_CONF_PRFX + ".scheduler.evaluations";
  public static final String SCHEDULER_OPTIMIZATIONS = RL_CONF_PRFX + ".scheduler.optimizations";

  //Policies conf
  //History parameter
  public static final String HISTORY_BIAS = RL_CONF_PRFX + ".history";
  public static final double DEFAULT_HISTORY_BIAS = 0.5;
  //Epsilon parameter:
  public static final String GREEDINESS_BIAS = RL_CONF_PRFX + ".greediness";
  public static final double DEFAULT_GREEDINESS_BIAS = 0.5;

  //Rng conf
  public static final String RNG_SEED = RL_CONF_PRFX + ".seed";
  public static final long DEFAULT_RNG_SEED = 15485863;

  public static final String RNG_RANDOM_SEED = RL_CONF_PRFX + ".random";
  public static final boolean DEFAULT_RANDOM_SEED = false;

  //Pruning and termination conf
  public static final String PRUNING = RL_CONF_PRFX + ".pruning";
  public static final boolean DEFAULT_USE_PRUNING = true;
  public static final String TERMINATION_STRAT = RL_CONF_PRFX + ".termination";
  public static final String MAX_SAMPLES_TERMINATION_STRAT = TERMINATION_STRAT + ".maxsamples";
  public static final int DEFAULT_MAX_SAMPLES = 1000;

  //Model counting conf. This will use the model counting reward decorator and propagate
  //the path volume (count) as the number of times a node has been visited
  public static final boolean DEFAULT_USE_MODELCOUNT_AMPLIFICATION = true;
  public static final String USE_MODELCOUNT_AMPLIFICATION = RL_CONF_PRFX + ".modelcounting";

  //Analysis processing
  public static final String ANALYSIS_PROCESSOR = RL_CONF_PRFX + ".analysisprocessor";

  private final Config jpfConfig;
  private final JPF jpf;

  //ctor required for jpf shell
  public RLShell(Config config) {
    this.jpfConfig = config;
    
    if(!jpfConfig.getBoolean(EXHAUSTIVE_ANALYSIS, false)) {
      //Substitute search object to use our sampler
      this.jpfConfig.setProperty("search.class", SamplingSearch.class.getName());
    }
    
    this.jpf = new JPF(jpfConfig);

    boolean useRandomSeed = config.getBoolean(RNG_RANDOM_SEED, DEFAULT_RANDOM_SEED);

    SimulationPolicy defaultSimulationPolicy = null;
    if(useRandomSeed) {
      defaultSimulationPolicy = new UniformSimulationPolicy();
    } else {
      long seed = config.getLong(RNG_SEED, DEFAULT_RNG_SEED);
      defaultSimulationPolicy = new UniformSimulationPolicy(seed);
    }

    //TODO: use RL simulation here probably
    SimulationPolicy simPol = null;
    
    TerminationStrategy defaultTerminationStrategy;
    ChoicesStrategy choicesStrat;
    if(config.getBoolean(PRUNING, DEFAULT_USE_PRUNING)) {
      PruningChoicesStrategy prunStrat = PruningChoicesStrategy.getInstance();
      jpf.addListener(prunStrat);
      choicesStrat = prunStrat;
      
      //termination
      defaultTerminationStrategy = new AllPathsTerminationStrategy(prunStrat);
    } else {
      choicesStrat = new AllChoicesStrategy();
      
      //termination
      int sampleSize = config.getInt(MAX_SAMPLES_TERMINATION_STRAT, DEFAULT_MAX_SAMPLES);
      defaultTerminationStrategy = new SampleSizeTerminationStrategy(sampleSize);
    }
    TerminationStrategy terminationStrategy = getInstanceOrDefault(config, 
        TERMINATION_STRAT,
        TerminationStrategy.class, 
        defaultTerminationStrategy);
    // TerminationStrategy terminationStrategy = new RewardBoundedTermination(44, EVENT.SUCC);
    RewardFunction rewardFunc = getInstanceOrDefault(config,
        REWARD_FUNCTION, 
        RewardFunction.class, 
        new DepthRewardFunction());

    PathQuantifier defaultPathQuantifier = null;
    boolean useMCAmplification = config.getBoolean(USE_MODELCOUNT_AMPLIFICATION,
        DEFAULT_USE_MODELCOUNT_AMPLIFICATION);
    if(useMCAmplification) {
      try {
        SPFModelCounter runtimeAnalyzer = ModelCounterFactory.create(this.jpfConfig);

        //Decorate reward function with model count amplification
        rewardFunc = new ModelCountingAmplifierDecorator(rewardFunc, runtimeAnalyzer);

        //A bit ugly, but we set the default path quantifier to use model counts
        defaultPathQuantifier = new ModelCountingPathQuantifier(runtimeAnalyzer);
      } catch (ModelCounterCreationException e) {
        LOGGER.severe(e.getMessage());
        LOGGER.severe(e.getStackTrace().toString());
        throw new RLAnalysisException(e);
      }
    } else {
      //If we don't use model count amplification,
      //then we just stick we the concrete path quantifier
      //which adds 1 for each explored path
      defaultPathQuantifier = new ConcretePathQuantifier();
    }

    PathQuantifier pathQuantifier = getInstanceOrDefault(config,
        PATH_QUANTIFIER,
        PathQuantifier.class,
        defaultPathQuantifier);
    
    RLListener rl = new RLListener(simPol,
        rewardFunc,
        pathQuantifier,
        choicesStrat, 
        terminationStrategy);
    
    // We add the analysis processor as an observer of the mcts events.
    // It will notify the shell when it is done according to
    // the termination strategy
    if(!config.hasValue(ANALYSIS_PROCESSOR)) {
      rl.addEventObserver(AbstractAnalysisProcessor.DEFAULT);
      rl.addEventObserver(new LiveAnalysisStatisticsModelCounting());
    } else {
      for(AnalysisEventObserver obs : config.getInstances(ANALYSIS_PROCESSOR, AnalysisEventObserver.class)) {
        rl.addEventObserver(obs);
      }
    }
    
    jpf.addListener(rl);
  }

  @Override
  public void start(String[] args) {
    jpf.run();
  }
  
  // Instantiation of defInstance is a bit ugly. Just rely on jpf conf api...
  private static <T> T getInstanceOrDefault(Config conf, String key, Class<T> type, T defInstance) {
    if(conf.hasValue(key)) {
      return conf.getInstance(key, type);
    } else
    return defInstance;
  }
}
