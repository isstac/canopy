package edu.cmu.sv.isstac.sampling;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.LiveAnalysisStatistics;
import edu.cmu.sv.isstac.sampling.analysis.SampleStatistics;
import edu.cmu.sv.isstac.sampling.analysis.SampleStatisticsOutputter;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.quantification.ConcretePathQuantifier;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.ModelCountingPathQuantifier;
import edu.cmu.sv.isstac.sampling.quantification.PathQuantifier;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.search.SamplingListener;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
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

    public Builder setPathQuantifier(PathQuantifier pathQuantifier) {
      this.pathQuantifier = pathQuantifier;
      return this;
    }

    public SamplingAnalysis build(Config jpfConfig, AnalysisStrategy analysisStrategy)
        throws AnalysisCreationException {
      if(rewardFunction == null) {
        if(jpfConfig.hasValue(Options.REWARD_FUNCTION)) {
          this.rewardFunction = jpfConfig.getInstance(Options.REWARD_FUNCTION,
              RewardFunction.class);
        } else {
          this.rewardFunction = Options.DEFAULT_REWARD_FUNCTION;
        }
      }

      if(terminationStrategy == null) {
        if(jpfConfig.hasValue(Options.TERMINATION_STRATEGY)) {
          this.terminationStrategy = jpfConfig.getInstance(Options.TERMINATION_STRATEGY,
              TerminationStrategy.class);
        } else {
          this.terminationStrategy = Options.DEFAULT_TERMINATION_STRATEGY;
        }
      }

      if(choicesStrategy == null) {
        if(jpfConfig.hasValue(Options.CHOICES_STRATEGY)) {
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

      if(pathQuantifier == null) {
        if(jpfConfig.hasValue(Options.PATH_QUANTIFIER)) {
          pathQuantifier = jpfConfig.getInstance(Options.PATH_QUANTIFIER, PathQuantifier.class);
        } else {
          if(jpfConfig.getBoolean(Options.USE_MODELCOUNT_AMPLIFICATION,
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
      if(!liveAnalysis
          && jpfConfig.getBoolean(Options.SHOW_LIVE_STATISTICS,
              Options.DEFAULT_SHOW_LIVE_STATISTICS)) {
        this.eventObservers.add(new LiveAnalysisStatistics());
      }

      boolean finalStats = eventObservers.stream()
          .anyMatch(eventObserver -> eventObserver instanceof SampleStatisticsOutputter);

      if(!finalStats
          && jpfConfig.getBoolean(Options.SHOW_STATISTICS,
          Options.DEFAULT_SHOW_STATISTICS)) {
        this.eventObservers.add(new SampleStatisticsOutputter(new SampleStatistics(), System.out));
      }

      if(jpfConfig.hasValue(Options.EVENT_OBSERVERS)) {
        this.eventObservers.addAll(jpfConfig.getInstances(Options.EVENT_OBSERVERS,
            AnalysisEventObserver.class));
      }

      SamplingListener samplingListener = new SamplingListener(analysisStrategy, rewardFunction,
          pathQuantifier, terminationStrategy, choicesStrategy, eventObservers);
      SamplingAnalysis samplingAnalysis = new SamplingAnalysis(jpfConfig, samplingListener);

      return samplingAnalysis;
    }
  }

  private final JPF jpf;
  private final Config config;

  private SamplingAnalysis(Config config, SamplingListener samplingListener) {
    this.jpf = JPFSamplerFactory.create(config);
    this.config = config;
    this.jpf.addListener(samplingListener);
  }

  public void run() {

    // Run the analysis
    jpf.run();

    // Clean up temp files from model counting
    // TODO: maybe move this to somewhere more sensible
    if(!config.getBoolean(ModelCounterFactory.KEEP_TMP_DIR_CONF,
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
