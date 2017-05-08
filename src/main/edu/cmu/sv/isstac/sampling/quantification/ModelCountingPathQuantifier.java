package edu.cmu.sv.isstac.sampling.quantification;

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
