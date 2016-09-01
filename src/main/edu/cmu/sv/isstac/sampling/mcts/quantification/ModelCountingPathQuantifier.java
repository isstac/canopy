package edu.cmu.sv.isstac.sampling.mcts.quantification;

import java.math.BigDecimal;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.reward.ModelCountingAmplifierDecorator;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.VM;
import modelcounting.analysis.Analyzer;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.utils.BigRational;

/**
 * @author Kasper Luckow
 */
public class ModelCountingPathQuantifier implements PathQuantifier {
  private static final Logger LOGGER = JPF.getLogger(ModelCountingAmplifierDecorator.class
      .getName());

  private final Analyzer runtimeAnalyzer;

  public ModelCountingPathQuantifier(Analyzer runtimeAnalyzer) {
    this.runtimeAnalyzer = runtimeAnalyzer;
  }

  @Override
  public long quantifyPath(VM vm) {
    PathCondition pc = PathCondition.getPC(vm);
    String pString = PathUtil.clean(pc);
    try {
      BigRational count = this.runtimeAnalyzer.countPointsOfPC(pString);

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
