package edu.cmu.sv.isstac.sampling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.LiveAnalysisStatistics;
import edu.cmu.sv.isstac.sampling.analysis.SampleStatisticsOutputter;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.quantification.ConcretePathQuantifier;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.ModelCountingPathQuantifier;
import edu.cmu.sv.isstac.sampling.quantification.PathQuantifier;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.search.SamplingAnalysisListener;
import edu.cmu.sv.isstac.sampling.exploration.cache.StateCache;
import edu.cmu.sv.isstac.sampling.termination.SampleSizeTerminationStrategy;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class SamplingAnalysis {

  private static final Logger logger = JPFLogger.getLogger(SamplingAnalysis.class.getName());

  public static class Builder {
    private Collection<AnalysisEventObserver> eventObservers = new HashSet<>();
    private ChoicesStrategy choicesStrategy = null;
    private TerminationStrategy terminationStrategy = null;
    private PathQuantifier pathQuantifier = null;
    private RewardFunction rewardFunction = null;
    private StateCache stateCache;

    public Builder setRewardFunction(RewardFunction rewardFunction) {
      this.rewardFunction = rewardFunction;
      return this;
    }

    public Builder addEventObserver(AnalysisEventObserver eventObserver) {
      this.eventObservers.add(eventObserver);
      return this;
    }

    public Builder setChoicesStrategy(ChoicesStrategy choicesStrategy) {
      this.choicesStrategy = choicesStrategy;
      return this;
    }

    public Builder setTerminationStrategy(TerminationStrategy terminationStrategy) {
      this.terminationStrategy = terminationStrategy;
      return this;
    }

    public Builder setStateCache(StateCache stateCache) {
      this.stateCache = stateCache;
      return this;
    }

    public Builder setPathQuantifier(PathQuantifier pathQuantifier) {
      this.pathQuantifier = pathQuantifier;
      return this;
    }

    public SamplingAnalysis build(Config jpfConfig, AnalysisStrategy analysisStrategy,
                                  JPFFactory jpfFactory) throws AnalysisCreationException {

      List<JPFListener> jpfListeners = new ArrayList<>();

      if (rewardFunction == null) {
        this.rewardFunction = jpfConfig.getInstance(Options.REWARD_FUNCTION,
            RewardFunction.class, Options.DEFAULT_REWARD_FUNCTION);
      }

      //TODO: should fix this mess---it seems weird to add a reward function
      //if it implements jpflistener, but seems to be the easiest fix for depth
      //reward function that also supports measured methods i.e. not blindly relying on jpf's
      // notion of depth. We could also change this by expanding analysis event observers but
      // would ultimately give the same functionality (although a bit cleaner I believe)
      if(this.rewardFunction instanceof JPFListener) {
        jpfListeners.add((JPFListener) this.rewardFunction);
      }

      if (terminationStrategy == null) {
        this.terminationStrategy = jpfConfig.getInstance(Options.TERMINATION_STRATEGY,
            TerminationStrategy.class, Options.DEFAULT_TERMINATION_STRATEGY);


        //TODO: This is very specific to control the sampling size termination strategy. This is
        // only used for convenience and should be better integrated!
        if(jpfConfig.hasValue(Options.SAMPLING_SIZE_TERMINATION_STRATEGY)) {
          int samplingSize = jpfConfig.getInt(Options.SAMPLING_SIZE_TERMINATION_STRATEGY);
          this.terminationStrategy = new SampleSizeTerminationStrategy(samplingSize);
        }
      }

      if (choicesStrategy == null) {
        if (jpfConfig.hasValue(Options.CHOICES_STRATEGY)) {
          choicesStrategy = jpfConfig.getInstance(Options.CHOICES_STRATEGY, ChoicesStrategy.class);
        } else {
          //This is pretty ugly, but right now I'm not sure how we can get around it
          //because SamplingSearch cannot be instantiated :/
          choicesStrategy = Options.DEFAULT_CHOICES_STRATEGY;
          Options.choicesStrategy = choicesStrategy;
        }
      } else {
        choicesStrategy = Options.DEFAULT_CHOICES_STRATEGY;
        //This is pretty ugly, but right now I'm not sure how we can get around it
        //because SamplingSearch cannot be instantiated :/
        Options.choicesStrategy = choicesStrategy;
      }

      if(stateCache == null) {
        stateCache = jpfConfig.getInstance(Options.STATE_CACHE, StateCache.class, Options
            .DEFAULT_STATE_CACHE.getName());
      }
      //TODO: We should log the entire config
      logger.info("Using state caching implemented by class: " + stateCache.getClass().getName());

      if (pathQuantifier == null) {
        if (jpfConfig.hasValue(Options.PATH_QUANTIFIER)) {
          pathQuantifier = jpfConfig.getInstance(Options.PATH_QUANTIFIER, PathQuantifier.class);
        } else {
          if (jpfConfig.getBoolean(Options.USE_MODELCOUNT_AMPLIFICATION,
              Options.DEFAULT_USE_MODELCOUNT_AMPLIFICATION)) {

            //Create model counter
            SPFModelCounter modelCounter;
            try {
              modelCounter = ModelCounterFactory.getInstance(jpfConfig);
            } catch (ModelCounterCreationException e) {
              logger.severe(e.getMessage());
              throw new AnalysisCreationException(e);
            }
            pathQuantifier = new ModelCountingPathQuantifier(modelCounter);
          } else {
            pathQuantifier = new ConcretePathQuantifier();
          }
        }
      }

      boolean liveAnalysis = eventObservers.stream()
          .anyMatch(eventObserver -> eventObserver instanceof LiveAnalysisStatistics);
      if (!liveAnalysis
          && jpfConfig.getBoolean(Options.SHOW_LIVE_STATISTICS,
          Options.DEFAULT_SHOW_LIVE_STATISTICS)) {
        if(jpfConfig.hasValue(Options.SHOW_LIVE_STATISTICS_BUDGET)) {
          this.eventObservers.add(new LiveAnalysisStatistics(
              jpfConfig.getLong(Options.SHOW_LIVE_STATISTICS_BUDGET)));
        } else {
          this.eventObservers.add(new LiveAnalysisStatistics());
        }
      }

      boolean finalStats = eventObservers.stream()
          .anyMatch(eventObserver -> eventObserver instanceof SampleStatisticsOutputter);

      if (!finalStats
          && jpfConfig.getBoolean(Options.SHOW_STATISTICS,
          Options.DEFAULT_SHOW_STATISTICS)) {
        this.eventObservers.add(new SampleStatisticsOutputter(System.out));
      }

      if (jpfConfig.hasValue(Options.EVENT_OBSERVERS)) {
        this.eventObservers.addAll(jpfConfig.getInstances(Options.EVENT_OBSERVERS,
            AnalysisEventObserver.class));
      }

      SamplingAnalysisListener samplingListener = new SamplingAnalysisListener(analysisStrategy, rewardFunction,
          pathQuantifier, terminationStrategy, choicesStrategy, stateCache, eventObservers);
      jpfListeners.add(samplingListener);

      SamplingAnalysis samplingAnalysis = new SamplingAnalysis(jpfConfig, jpfListeners, jpfFactory);

      return samplingAnalysis;
    }
  }

  private final JPF jpf;
  private final Config config;

  private SamplingAnalysis(Config config, Collection<JPFListener> jpfListeners,
                           JPFFactory jpfFactory) {
    // Check that config object is using the symbolic instruction factory
    Class<?> instrFactory = config.getClass("jvm.insn_factory.class");
    if(!instrFactory.equals(SymbolicInstructionFactory.class)) {
      String msg = "Incorrect instruction factory " + instrFactory.getName() + ". Must be an " +
          "instance " + SymbolicInstructionFactory.class.getName() + ". Is your site.properties, " +
          "jpf.properties or app properties file incorrect?";
      logger.severe(msg);
      throw new AnalysisException(msg);
    }

    this.jpf = jpfFactory.buildInstance(config);
    this.config = config;
    jpfListeners.forEach(e -> this.jpf.addListener(e));
  }

  public void run() {

    // Run the analysis
    jpf.run();

    // Clean up temp files from model counting
    // TODO: maybe move this to somewhere more sensible
    if (!config.getBoolean(ModelCounterFactory.KEEP_TMP_DIR_CONF,
        ModelCounterFactory.KEEP_TMP_DIR_DEF)) {
      try {
        ModelCounterFactory.cleanUpTempFiles(config);
      } catch (IOException e) {
        logger.severe(e.getMessage());
        throw new AnalysisException(e);
      }
    }
  }
}
