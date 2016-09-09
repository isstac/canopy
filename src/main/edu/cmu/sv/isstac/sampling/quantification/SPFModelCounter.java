package edu.cmu.sv.isstac.sampling.quantification;

import java.util.Set;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import modelcounting.analysis.Analyzer;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.utils.BigRational;

/**
 * @author Kasper Luckow
 */
public interface SPFModelCounter extends Analyzer {

  public BigRational analyzeSpfPC(PathCondition pc) throws AnalysisException;

  public BigRational countPointsOfPC(PathCondition pc) throws AnalysisException;
}
