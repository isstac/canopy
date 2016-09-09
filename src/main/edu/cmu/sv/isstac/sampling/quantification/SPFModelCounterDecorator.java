package edu.cmu.sv.isstac.sampling.quantification;

import java.util.Set;

import edu.cmu.sv.isstac.sampling.util.PathUtil;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import modelcounting.analysis.Analyzer;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.analysis.exceptions.EmptyDomainException;
import modelcounting.domain.Problem;
import modelcounting.utils.BigRational;

/**
 * @author Kasper Luckow
 */
public class SPFModelCounterDecorator implements SPFModelCounter {

  private final Analyzer analyzer;

  public SPFModelCounterDecorator(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  @Override
  public BigRational analyzeSpfPC(String pc) throws AnalysisException {
    return this.analyzer.analyzeSpfPC(pc);
  }

  @Override
  public BigRational analyzeSetOfSpfPC(Set<String> pcs) throws AnalysisException {
    return this.analyzer.analyzeSetOfSpfPC(pcs);
  }

  @Override
  public BigRational countPointsOfPC(String pc) throws AnalysisException {
    return this.analyzer.countPointsOfPC(pc);
  }

  @Override
  public BigRational countPointsOfSetOfPCs(Set<String> pcs) throws AnalysisException {
    return this.analyzer.countPointsOfSetOfPCs(pcs);
  }

  @Override
  public BigRational getDomainSize() throws AnalysisException {
    return this.analyzer.getDomainSize();
  }

  @Override
  public Set<Problem> excludeFromDomain(String pc) throws AnalysisException, EmptyDomainException {
    return this.analyzer.excludeFromDomain(pc);
  }

  @Override
  public Set<Problem> excludeFromDomain(Set<String> pcs) throws AnalysisException, EmptyDomainException {
    return this.analyzer.excludeFromDomain(pcs);
  }

  @Override
  public void terminate() {
    this.analyzer.terminate();
  }

  @Override
  public BigRational analyzeSpfPC(PathCondition pc) throws AnalysisException {
    String pString = PathUtil.clean(pc);
    return this.analyzeSpfPC(pString);
  }

  @Override
  public BigRational countPointsOfPC(PathCondition pc) throws AnalysisException {
    String pString = PathUtil.clean(pc);
    return this.countPointsOfPC(pString);
  }
}
