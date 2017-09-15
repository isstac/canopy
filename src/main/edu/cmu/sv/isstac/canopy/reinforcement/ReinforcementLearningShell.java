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

package edu.cmu.sv.isstac.canopy.reinforcement;

import edu.cmu.sv.isstac.canopy.AnalysisCreationException;
import edu.cmu.sv.isstac.canopy.JPFSamplerFactory;
import edu.cmu.sv.isstac.canopy.Options;
import edu.cmu.sv.isstac.canopy.SamplingAnalysis;
import edu.cmu.sv.isstac.canopy.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.canopy.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.canopy.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.canopy.structure.NodeFactory;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;

/**
 * @author Kasper Luckow
 *
 */
public class ReinforcementLearningShell implements JPFShell {

  private final SamplingAnalysis samplingAnalysis;
  
  //ctor required for jpf shell
  public ReinforcementLearningShell(Config config) throws ModelCounterCreationException,
      AnalysisCreationException {
    SamplingAnalysis.Builder samplingAnalysisBuilder = new SamplingAnalysis.Builder();

    int samplesPerOptimization = config.getInt(Utils.SAMPLES_PER_OPTIMIZATION,
        Utils.DEFAULT_SAMPLES_PER_OPTIMIZATION);

    double epsilon = config.getDouble(Utils.EPSILON, Utils.DEFAULT_EPSILON);
    double historyWeight = config.getDouble(Utils.HISTORY, Utils.DEFAULT_HISTORY);

    NodeFactory<RLNode> factory;

    if(config.getBoolean(Utils.USE_MODELCOUNTING, Utils.DEFAULT_USE_MODELCOUNTING)) {
      SPFModelCounter modelCounter = ModelCounterFactory.getInstance(config);
      factory = new RLNodeFactoryMCDecorator(modelCounter);
    } else {
      factory = new RLNodeFactory();
    }

    this.samplingAnalysis = samplingAnalysisBuilder.build(config,
        new ReinforcementLearningStrategy(samplesPerOptimization, epsilon,
            historyWeight, factory, Options.getSeed(config)), new JPFSamplerFactory());
  }

  @Override
  public void start(String[] args) {
    samplingAnalysis.run();
  }
}
