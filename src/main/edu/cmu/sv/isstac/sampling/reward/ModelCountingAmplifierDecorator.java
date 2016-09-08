package edu.cmu.sv.isstac.sampling.reward;

import java.math.BigDecimal;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.quantification.ModelCountingException;
import edu.cmu.sv.isstac.sampling.quantification.PathUtil;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.VM;
import modelcounting.analysis.Analyzer;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.utils.BigRational;

/**
 * Created by luckow on 8/31/16.
 */
public class ModelCountingAmplifierDecorator implements RewardFunction {

  private static final Logger LOGGER = JPF.getLogger(ModelCountingAmplifierDecorator.class
      .getName());
  private final Analyzer runtimeAnalyzer;
  private final RewardFunction rewardFunction;

  public ModelCountingAmplifierDecorator(RewardFunction rewardFunction, Analyzer runtimeAnalyzer) {
    this.rewardFunction = rewardFunction;
    this.runtimeAnalyzer = runtimeAnalyzer;
  }

  @Override
  public final long computeReward(VM vm) {
    // Compute the reward with the original reward function we are wrapping
    long reward = this.rewardFunction.computeReward(vm);

    PathCondition pc = PathCondition.getPC(vm);
    String pString = PathUtil.clean(pc);

    try {
      BigRational rationalCount = this.runtimeAnalyzer.countPointsOfPC(pString);

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
