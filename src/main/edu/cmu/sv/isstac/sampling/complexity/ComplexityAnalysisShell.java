/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.sv.isstac.sampling.complexity;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.AnalysisException;
import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.JPFFactory;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.exhaustive.ExhaustiveStrategy;
import edu.cmu.sv.isstac.sampling.exhaustive.JPFExhaustiveFactory;
import edu.cmu.sv.isstac.sampling.mcts.MCTSStrategy;
import edu.cmu.sv.isstac.sampling.mcts.SelectionPolicy;
import edu.cmu.sv.isstac.sampling.mcts.Utils;
import edu.cmu.sv.isstac.sampling.montecarlo.MonteCarloStrategy;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.reinforcement.RLNode;
import edu.cmu.sv.isstac.sampling.reinforcement.RLNodeFactory;
import edu.cmu.sv.isstac.sampling.reinforcement.RLNodeFactoryMCDecorator;
import edu.cmu.sv.isstac.sampling.reinforcement.ReinforcementLearningStrategy;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import edu.cmu.sv.isstac.sampling.visualization.SymTreeVisualizer;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class ComplexityAnalysisShell implements JPFShell {

  public static Logger logger = JPFLogger.getLogger(ComplexityAnalysisShell.class.getName());

  private static AnalysisFactory mctsFactory = new AnalysisFactory() {
    @Override
    public AnalysisStrategy createAnalysis(Config config) throws AnalysisCreationException {
      SelectionPolicy selectionPolicy = Utils.createSelectionPolicy(config);
      SimulationPolicy simulationPolicy = null;
      try {
        simulationPolicy = Utils.createSimulationPolicy(config);
      } catch (ModelCounterCreationException e) {
        logger.severe(e.getMessage());
        throw new AnalysisCreationException(e);
      }

      return new MCTSStrategy(selectionPolicy, simulationPolicy);
    }

    @Override
    public JPFFactory getJPFFactory() {
      return new JPFSamplerFactory();
    }
  };

  private static AnalysisFactory mcFactory = new AnalysisFactory() {
    @Override
    public AnalysisStrategy createAnalysis(Config config) throws AnalysisCreationException {
      SimulationPolicy simulationPolicy = null;
      try {
        simulationPolicy = Utils.createSimulationPolicy(config);
      } catch (ModelCounterCreationException e) {
        logger.severe(e.getMessage());
        throw new AnalysisCreationException(e);
      }

      return new MonteCarloStrategy(simulationPolicy);
    }

    @Override
    public JPFFactory getJPFFactory() {
      return new JPFSamplerFactory();
    }
  };

  private static AnalysisFactory rlFactory = new AnalysisFactory() {
    @Override
    public AnalysisStrategy createAnalysis(Config config) throws AnalysisCreationException {
      int samplesPerOptimization = config.getInt(
          edu.cmu.sv.isstac.sampling.reinforcement.Utils.SAMPLES_PER_OPTIMIZATION,
          edu.cmu.sv.isstac.sampling.reinforcement.Utils.DEFAULT_SAMPLES_PER_OPTIMIZATION);

      double epsilon = config.getDouble(edu.cmu.sv.isstac.sampling.reinforcement.Utils.EPSILON,
          edu.cmu.sv.isstac.sampling.reinforcement.Utils.DEFAULT_EPSILON);
      double historyWeight = config.getDouble(
          edu.cmu.sv.isstac.sampling.reinforcement.Utils.HISTORY,
          edu.cmu.sv.isstac.sampling.reinforcement.Utils.DEFAULT_HISTORY);

      NodeFactory<RLNode> factory;

      if (config.getBoolean(edu.cmu.sv.isstac.sampling.reinforcement.Utils.USE_MODELCOUNTING,
          edu.cmu.sv.isstac.sampling.reinforcement.Utils.DEFAULT_USE_MODELCOUNTING)) {
        SPFModelCounter modelCounter = null;
        try {
          modelCounter = ModelCounterFactory.getInstance(config);
        } catch (ModelCounterCreationException e) {
          logger.severe(e.getMessage());
          throw new AnalysisCreationException(e);
        }
        factory = new RLNodeFactoryMCDecorator(modelCounter);
      } else {
        factory = new RLNodeFactory();
      }

      return new ReinforcementLearningStrategy(samplesPerOptimization, epsilon,
          historyWeight, factory, Options.getSeed(config));
    }

    @Override
    public JPFFactory getJPFFactory() {
      return new JPFSamplerFactory();
    }
  };

  private static AnalysisFactory exhaustiveFactory = new AnalysisFactory() {
    @Override
    public AnalysisStrategy createAnalysis(Config config) throws AnalysisCreationException {
      return  new ExhaustiveStrategy();
    }

    @Override
    public JPFFactory getJPFFactory() {
      return new JPFExhaustiveFactory();
    }
  };


  private final SamplingAnalysis samplingAnalysis;
  private final ComplexityAnalyzer ca;

  //ctor required for jpf shell
  public ComplexityAnalysisShell(Config config) throws AnalysisCreationException, ModelCounterCreationException {

    //disable livetracker chart
    config.setProperty(Options.SHOW_LIVE_STATISTICS, "false");

    AnalysisFactory af = getAnalysisFactory(config);

    SamplingAnalysis.Builder analysisBuilder =
        new SamplingAnalysis.Builder();

    this.samplingAnalysis = analysisBuilder.build(config,
        af.createAnalysis(config),
        af.getJPFFactory());

    int[] inputRange = config.getIntArray(
        edu.cmu.sv.isstac.sampling.complexity.Utils.INPUT_RANGE);
    assert inputRange[0] <= inputRange[1];

    int increment = config.getInt(edu.cmu.sv.isstac.sampling.complexity.Utils.INPUT_INCREMENT,
        edu.cmu.sv.isstac.sampling.complexity.Utils.INPUT_INCREMENT_DEFAULT);

    boolean visualize = config.getBoolean(edu.cmu.sv.isstac.sampling.complexity.Utils.VISUALIZE,
        edu.cmu.sv.isstac.sampling.complexity.Utils.VISUALIZE_DEFAULT);

    ca = new ComplexityAnalyzer(af, inputRange[0], inputRange[1], increment, visualize, config);
  }

  @Override
  public void start(String[] args) {
    try {
      this.ca.run();
    } catch (AnalysisCreationException e) {
      logger.severe(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private AnalysisFactory getAnalysisFactory(Config config) {
    switch(config.getString(edu.cmu.sv.isstac.sampling.complexity.Utils.ANALYSIS_TYPE)) {
      case "mcts":
        return mctsFactory;
      case "mc":
        return mcFactory;
      case "rl":
        return rlFactory;
      case "exhaustive":
        return exhaustiveFactory;
      default:
        throw new AnalysisException("Config " + edu.cmu.sv.isstac.sampling.complexity.Utils
            .ANALYSIS_TYPE + " must be one of: mcts, mc, rl, exhaustive");
    }
  }

}
