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

package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.JPFFactory;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.exploration.AllChoicesStrategy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.reinforcement.RLNode;
import edu.cmu.sv.isstac.sampling.reinforcement.RLNodeFactory;
import edu.cmu.sv.isstac.sampling.reinforcement.RLNodeFactoryMCDecorator;
import edu.cmu.sv.isstac.sampling.reinforcement.ReinforcementLearningStrategy;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class RLExperiment implements Experiment {

  private final boolean pruning;
  private final boolean rewardAmplifcation;
  private final boolean modelCountSubDomain;
  private final int samplesPerOptimization;
  private final double epsilon;
  private final double historyWeight;

  public RLExperiment(boolean pruning, boolean rewardAmplification, boolean modelCountSubDomain,
                      int samplesPerOptimization,
                      double epsilon, double historyWeight) {
    this.pruning = pruning;
    this.rewardAmplifcation = rewardAmplification;
    this.modelCountSubDomain = modelCountSubDomain;
    this.samplesPerOptimization = samplesPerOptimization;
    this.epsilon = epsilon;
    this.historyWeight = historyWeight;
  }

  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed) {
    if(!pruning) {
      config.setProperty(Options.CHOICES_STRATEGY, AllChoicesStrategy.class.getName());
    }

    config.setProperty(Options.USE_MODELCOUNT_AMPLIFICATION,
        Boolean.toString(this.rewardAmplifcation));
    config.setProperty(Options.RNG_SEED, Integer.toString(seed));

    NodeFactory<RLNode> factory;

    if(modelCountSubDomain) {
      try {
        SPFModelCounter modelCounter = ModelCounterFactory.getInstance(config);
        factory = new RLNodeFactory();
      } catch (ModelCounterCreationException e) {
        throw new BatchProcessorException(e);
      }
    } else {
      factory = new RLNodeFactory();
    }

    return new ReinforcementLearningStrategy(samplesPerOptimization, epsilon, historyWeight,
        factory, seed);

  }

  @Override
  public JPFFactory getJPFFactory() {
    return new JPFSamplerFactory();
  }

  @Override
  public String getName() {
    return "RL[" +
        "pruning=" + this.pruning + ";" +
        "rewardAmp=" + this.rewardAmplifcation + ";" +
        "modelCountSubDomain=" + this.modelCountSubDomain + ";" +
        "samplesPerOptimization=" + this.samplesPerOptimization + ";" +
        "epsilon=" + this.epsilon + ";" +
        "history=" + this.historyWeight + "]";
  }
}
