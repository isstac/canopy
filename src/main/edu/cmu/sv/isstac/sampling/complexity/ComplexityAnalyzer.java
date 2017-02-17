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

import java.awt.*;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.visualization.SymTreeVisualizer;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class ComplexityAnalyzer {

  private final AnalysisFactory af;
  private final int min;
  private final int max;
  private final int increment;
  private final boolean visualize;
  private final Config config;
  private ComplexityChart chart = null;

  public ComplexityAnalyzer(AnalysisFactory af, int min, int max, int increment,
                            boolean visualize, Config config) {
    this.af = af;
    this.min = min;
    this.max = max;
    this.increment = increment;
    this.visualize = visualize;
    this.config = config;

    if(visualize) {
      chart = new ComplexityChart();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      chart.setPreferredSize(new Dimension(screenSize.width, 768));
      chart.pack();
      chart.setVisible(true);
    }
  }

  public void run() throws AnalysisCreationException {
    for(int inputSize = min; inputSize <= max; inputSize += increment) {
      SamplingAnalysis.Builder analysisBuilder =
          new SamplingAnalysis.Builder();

      updateConfigInputSize(config, inputSize);

      if(visualize) {
        analysisBuilder.addEventObserver(new ComplexityChartUpdater(chart, inputSize));
      }

      SamplingAnalysis samplingAnalysis = analysisBuilder.build(config,
          af.createAnalysis(config),
          af.getJPFFactory());

      samplingAnalysis.run();
    }
  }

  private void updateConfigInputSize(Config conf, int inputSize) {
    if(conf.hasValue("target.args")) {
      conf.remove("target.args");
    }
    conf.setProperty("target.args", Integer.toString(inputSize));
  }
}
