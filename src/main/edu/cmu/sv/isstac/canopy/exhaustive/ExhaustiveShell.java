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

package edu.cmu.sv.isstac.canopy.exhaustive;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.canopy.AnalysisCreationException;
import edu.cmu.sv.isstac.canopy.AnalysisException;
import edu.cmu.sv.isstac.canopy.AnalysisStrategy;
import edu.cmu.sv.isstac.canopy.SamplingAnalysis;
import edu.cmu.sv.isstac.canopy.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class ExhaustiveShell implements JPFShell {

  private final Logger logger = JPFLogger.getLogger(edu.cmu.sv.isstac.canopy
      .exhaustive.ExhaustiveShell.class.getName());

  private final SamplingAnalysis.Builder samplingAnalysisBuilder;
  private final Config config;

  //ctor required for jpf shell
  public ExhaustiveShell(Config config) throws AnalysisCreationException, ModelCounterCreationException {
    this.config = config;

    samplingAnalysisBuilder = new SamplingAnalysis.Builder();
  }

  public void addListener(JPFListener listener) {
    this.samplingAnalysisBuilder.addListener(listener);
  }

  @Override
  public void start(String[] args) {
    AnalysisStrategy strategy = new ExhaustiveStrategy();
    SamplingAnalysis samplingAnalysis;
    try {
      samplingAnalysis = samplingAnalysisBuilder.build(config, strategy, new
          JPFExhaustiveFactory());
    } catch (AnalysisCreationException e) {
      logger.severe(e.getMessage());
      throw new AnalysisException(e);
    }
    samplingAnalysis.run();
  }
}
