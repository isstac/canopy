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

package edu.cmu.sv.isstac.sampling.mcts;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.visualization.SymTreeVisualizer;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;

/**
 * @author Kasper Luckow
 *
 */
public class MCTSShell implements JPFShell {

  private final SamplingAnalysis samplingAnalysis;

  //ctor required for jpf shell
  public MCTSShell(Config config) throws AnalysisCreationException, ModelCounterCreationException {
    SelectionPolicy selectionPolicy = Utils.createSelectionPolicy(config);
    SimulationPolicy simulationPolicy = Utils.createSimulationPolicy(config);

    MCTSStrategy mcts = new MCTSStrategy(selectionPolicy, simulationPolicy);

    SamplingAnalysis.Builder analysisBuilder =
        new SamplingAnalysis.Builder();

    if(config.getBoolean(Utils.USE_TREE_VISUALIZATION, Utils.DEFAULT_USE_TREE_VISUALIZATION)) {
      mcts.addObserver(new SymTreeVisualizer());
    }

    this.samplingAnalysis = analysisBuilder.build(config, mcts, new JPFSamplerFactory());
  }

  @Override
  public void start(String[] args) {
    this.samplingAnalysis.run();
  }

}
