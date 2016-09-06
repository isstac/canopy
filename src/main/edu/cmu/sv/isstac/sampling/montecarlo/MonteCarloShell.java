package edu.cmu.sv.isstac.sampling.montecarlo;

import edu.cmu.sv.isstac.sampling.analysis.AbstractAnalysisProcessor;
import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.SamplingSearch;
import edu.cmu.sv.isstac.sampling.exploration.AllChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import edu.cmu.sv.isstac.sampling.policies.RandomizedPolicy;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.reward.DepthRewardFunction;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.termination.AllPathsTerminationStrategy;
import edu.cmu.sv.isstac.sampling.termination.SampleSizeTerminationStrategy;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;

/**
 * @author Kasper Luckow
 * There is a lot of redundancy between this class and MCTSShell.
 * We can maybe generalize this shell later if we experiment with more
 * techniques for sampling
 */
public class MonteCarloShell implements JPFShell {
  public static final String MC_CONF_PRFX = "symbolic.security.sampling.montecarlo";
  
  //This setting can be used to disable sampling to exhaustively explore the tree (mostly for debugging...)
  public static final String EXHAUSTIVE_ANALYSIS = MC_CONF_PRFX + ".exhaustive";
  
  public static final String REWARD_FUNCTION = MC_CONF_PRFX + ".rewardfunc"; 

  public static final String SIM_POLICY = MC_CONF_PRFX + ".simulationpol";
  
  //Policies conf  
  public static final String RNG_SEED = MC_CONF_PRFX + ".seed";
  public static final long DEFAULT_RNG_SEED = 15485863;
  public static final String RNG_RANDOM_SEED = MC_CONF_PRFX + ".random";
  public static final boolean DEFAULT_RANDOM_SEED = false;
  
  //Pruning and termination conf
  public static final String PRUNING = MC_CONF_PRFX + ".pruning";
  public static final boolean DEFAULT_USE_PRUNING = true;
  public static final String TERMINATION_STRAT = MC_CONF_PRFX + ".termination";
  public static final String MAX_SAMPLES_TERMINATION_STRAT = TERMINATION_STRAT + ".maxsamples";
  public static final int DEFAULT_MAX_SAMPLES = 1000;
  
  //Analysis processing
  public static final String ANALYSIS_PROCESSOR = MC_CONF_PRFX + ".analysisprocessor";
  
  private final Config jpfConfig;
  private final JPF jpf;
  
  //ctor required for jpf shell
  public MonteCarloShell(Config config) {
    this.jpfConfig = config;
    
    if(!jpfConfig.getBoolean(EXHAUSTIVE_ANALYSIS, false)) {
      //Substitute search object to use our sampler
      this.jpfConfig.setProperty("search.class", SamplingSearch.class.getName());
    }
    
    this.jpf = new JPF(jpfConfig);
    
    boolean useRandomSeed = config.getBoolean(RNG_RANDOM_SEED, DEFAULT_RANDOM_SEED);
    SimulationPolicy defaultSimulationPolicy = null;
    if(useRandomSeed) {
      defaultSimulationPolicy = new RandomizedPolicy();
    } else {
      long seed = config.getLong(RNG_SEED, DEFAULT_RNG_SEED);
      defaultSimulationPolicy = new RandomizedPolicy(seed);
    }
    
    SimulationPolicy simPol = getInstanceOrDefault(config, 
        SIM_POLICY, 
        SimulationPolicy.class, 
        defaultSimulationPolicy);
    
    TerminationStrategy defaultTerminationStrategy = null;
    ChoicesStrategy choicesStrat = null;
    if(config.getBoolean(PRUNING, DEFAULT_USE_PRUNING)) {
      PruningChoicesStrategy prunStrat = new PruningChoicesStrategy();
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
  //  TerminationStrategy terminationStrategy = new RewardBoundedTermination(28, RewardBoundedTermination.EVENT.SUCC);
    RewardFunction rewardFunc = getInstanceOrDefault(config,
        REWARD_FUNCTION, 
        RewardFunction.class, 
        new DepthRewardFunction());
    
    MonteCarloListener mcListener = new MonteCarloListener(simPol, 
        rewardFunc, 
        choicesStrat, 
        terminationStrategy);
    
    // We add the analysis processor as an observer of the monte carlo search events.
    // It will notify the shell when it is done according to
    // the termination strategy
    if(!config.hasValue(ANALYSIS_PROCESSOR)) {
      mcListener.addEventObserver(AbstractAnalysisProcessor.DEFAULT);
    } else {
      for(AnalysisEventObserver obs : config.getInstances(ANALYSIS_PROCESSOR, AnalysisEventObserver.class)) {
        mcListener.addEventObserver(obs);
      }
    }

    jpf.addListener(mcListener);
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
