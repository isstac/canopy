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
