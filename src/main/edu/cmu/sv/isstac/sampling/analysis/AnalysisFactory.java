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

package edu.cmu.sv.isstac.sampling.analysis;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.JPFFactory;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.Options;
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
import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public interface AnalysisFactory {
  AnalysisStrategy createAnalysis(Config config) throws AnalysisCreationException;
  JPFFactory getJPFFactory();

  public static Logger logger = JPFLogger.getLogger(AnalysisFactory.class.getName());

  AnalysisFactory mctsFactory = new AnalysisFactory() {
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

  AnalysisFactory mcFactory = new AnalysisFactory() {
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

  AnalysisFactory rlFactory = new AnalysisFactory() {
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

  AnalysisFactory exhaustiveFactory = new AnalysisFactory() {
    @Override
    public AnalysisStrategy createAnalysis(Config config) throws AnalysisCreationException {
      return  new ExhaustiveStrategy();
    }

    @Override
    public JPFFactory getJPFFactory() {
      return new JPFExhaustiveFactory();
    }
  };
}
