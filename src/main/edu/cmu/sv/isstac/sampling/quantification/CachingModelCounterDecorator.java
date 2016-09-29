package edu.cmu.sv.isstac.sampling.quantification;

import java.util.Set;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.analysis.exceptions.EmptyDomainException;
import modelcounting.domain.Problem;
import modelcounting.utils.BigRational;

/**
 * @author Kasper Luckow
 */
public class CachingModelCounterDecorator implements SPFModelCounter {

  private final SPFModelCounter decoratee;



  public CachingModelCounterDecorator(SPFModelCounter decoratee) {
    this.decoratee = decoratee;
  }

  @Override
  public BigRational analyzeSpfPC(String pc) throws AnalysisException {
    return this.decoratee.analyzeSpfPC(pc);
  }

  @Override
  public BigRational analyzeSetOfSpfPC(Set<String> pcs) throws AnalysisException {
    return this.decoratee.analyzeSetOfSpfPC(pcs);
  }

  @Override
  public BigRational countPointsOfPC(String pc) throws AnalysisException {
    return this.decoratee.countPointsOfPC(pc);
  }

  @Override
  public BigRational countPointsOfSetOfPCs(Set<String> pcs) throws AnalysisException {
    return this.decoratee.countPointsOfSetOfPCs(pcs);
  }

  @Override
  public BigRational analyzeSpfPC(PathCondition pc) throws AnalysisException {
    return this.decoratee.analyzeSpfPC(pc);
  }

  @Override
  public BigRational countPointsOfPC(PathCondition pc) throws AnalysisException {
    return this.decoratee.countPointsOfPC(pc);
  }

  @Override
  public BigRational getDomainSize() throws AnalysisException {
    return this.decoratee.getDomainSize();
  }

  @Override
  public Set<Problem> excludeFromDomain(String pc) throws AnalysisException, EmptyDomainException {
    return this.decoratee.excludeFromDomain(pc);
  }

  @Override
  public Set<Problem> excludeFromDomain(Set<String> pcs) throws AnalysisException, EmptyDomainException {
    return this.decoratee.excludeFromDomain(pcs);
  }

  @Override
  public void terminate() {
    this.decoratee.terminate();
  }
}
