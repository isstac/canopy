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

package edu.cmu.sv.isstac.sampling.reward;

import java.math.BigDecimal;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.quantification.ModelCountingException;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.VM;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.utils.BigRational;

/**
 * Created by luckow on 8/31/16.
 * This class is not used atm
 */
@Deprecated
public class ModelCountingAmplifierDecorator implements RewardFunction {

  private static final Logger LOGGER = JPF.getLogger(ModelCountingAmplifierDecorator.class
      .getName());
  private final SPFModelCounter modelCounter;
  private final RewardFunction rewardFunction;

  public ModelCountingAmplifierDecorator(RewardFunction rewardFunction, SPFModelCounter modelCounter) {
    this.rewardFunction = rewardFunction;
    this.modelCounter = modelCounter;
  }

  @Override
  public final long computeReward(VM vm) {
    // Compute the reward with the original reward function we are wrapping
    long reward = this.rewardFunction.computeReward(vm);

    PathCondition pc = PathCondition.getPC(vm);

    try {
      BigRational rationalCount = this.modelCounter.countPointsOfPC(pc);

      //TODO: Ugly conversion to string
      //TODO: Is this correct?
      long count = new BigDecimal(rationalCount.toString()).longValue();

      // We amplify the original reward with the model count
      return count * reward;

    } catch (AnalysisException e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
      throw new ModelCountingException(e);
    }
  }
}
