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

package edu.cmu.sv.isstac.canopy.quantification;

import java.math.BigDecimal;
import java.util.logging.Logger;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.VM;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.utils.BigRational;

/**
 * @author Kasper Luckow
 */
public class ModelCountingPathQuantifier implements PathQuantifier {
  private static final Logger LOGGER = JPF.getLogger(ModelCountingPathQuantifier
      .class.getName());

  private final SPFModelCounter modelCounter;

  public ModelCountingPathQuantifier(SPFModelCounter modelCounter) {
    this.modelCounter = modelCounter;
  }

  @Override
  public long quantifyPath(VM vm) {
    PathCondition pc = PathCondition.getPC(vm);
    try {
      BigRational count = this.modelCounter.countPointsOfPC(pc);

      //TODO: Ugly conversion to string
      BigDecimal decimalCount = new BigDecimal(count.toString());

      //TODO: Is this correct?
      return decimalCount.longValue();

    } catch (AnalysisException e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
      throw new ModelCountingException(e);
    }
  }
}
