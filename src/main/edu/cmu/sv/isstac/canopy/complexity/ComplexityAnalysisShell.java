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

package edu.cmu.sv.isstac.canopy.complexity;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.canopy.AnalysisCreationException;
import edu.cmu.sv.isstac.canopy.AnalysisException;
import edu.cmu.sv.isstac.canopy.Options;
import edu.cmu.sv.isstac.canopy.SamplingAnalysis;
import edu.cmu.sv.isstac.canopy.analysis.AnalysisFactory;
import edu.cmu.sv.isstac.canopy.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class ComplexityAnalysisShell implements JPFShell {

  public static Logger logger = JPFLogger.getLogger(ComplexityAnalysisShell.class.getName());

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
        edu.cmu.sv.isstac.canopy.complexity.Utils.INPUT_RANGE);
    assert inputRange[0] <= inputRange[1];

    int increment = config.getInt(edu.cmu.sv.isstac.canopy.complexity.Utils.INPUT_INCREMENT,
        edu.cmu.sv.isstac.canopy.complexity.Utils.INPUT_INCREMENT_DEFAULT);

    boolean visualize = config.getBoolean(edu.cmu.sv.isstac.canopy.complexity.Utils.VISUALIZE,
        edu.cmu.sv.isstac.canopy.complexity.Utils.VISUALIZE_DEFAULT);

    ca = new ComplexityAnalyzer(af, inputRange[0], inputRange[1], increment, visualize, config);
  }

  @Override
  public void start(String[] args) {
    try {
      this.ca.run();
    } catch (AnalysisCreationException e) {
      logger.severe(e.getMessage());
      throw new AnalysisException(e);
    }
  }

  private AnalysisFactory getAnalysisFactory(Config config) {
    switch(config.getString(edu.cmu.sv.isstac.canopy.complexity.Utils.ANALYSIS_TYPE)) {
      case "mcts":
        return AnalysisFactory.mctsFactory;
      case "mc":
        return AnalysisFactory.mcFactory;
      case "rl":
        return AnalysisFactory.rlFactory;
      case "exhaustive":
        return AnalysisFactory.exhaustiveFactory;
      default:
        throw new AnalysisException("Config " + edu.cmu.sv.isstac.canopy.complexity.Utils
            .ANALYSIS_TYPE + " must be one of: mcts, mc, rl, exhaustive");
    }
  }

}
