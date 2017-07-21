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

package edu.cmu.sv.isstac.sampling.quantification;

import java.util.Set;

import edu.cmu.sv.isstac.sampling.util.JPFUtil;
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
    String pString = JPFUtil.clean(pc);
    return this.analyzeSpfPC(pString);
  }

  @Override
  public BigRational countPointsOfPC(PathCondition pc) throws AnalysisException {
    String pString = JPFUtil.clean(pc);
    return this.countPointsOfPC(pString);
  }
}
