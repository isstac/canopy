/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
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

package edu.cmu.sv.isstac.sampling.sidechannel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class EntropyListener implements AnalysisEventObserver {

  public static Logger logger = JPFLogger.getLogger(EntropyListener.class.getName());

  private final Map<Long, Set<PathCondition>> rewards2pc = new HashMap<>();
  private final SPFModelCounter modelCounter;

  public EntropyListener(Config jpfConfig) throws AnalysisCreationException {
    try {
      modelCounter = ModelCounterFactory.getInstance(jpfConfig);
    } catch (ModelCounterCreationException e) {
      logger.severe(e.getMessage());
      throw new AnalysisCreationException(e);
    }
  }

  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward, long pathVolume,
                         SamplingResult.ResultContainer currentBestResult,
                         boolean hasBeenExplored) {
    PathCondition pc = PathCondition.getPC(searchState.getVM());
    Set<PathCondition> pcs = rewards2pc.get(propagatedReward);
    if(pcs == null) {
      pcs = new HashSet<>();
      rewards2pc.put(propagatedReward, pcs);
    }
    pcs.add(pc.make_copy());
  }

  @Override
  public void analysisDone(SamplingResult result) {

  }

  @Override
  public void analysisStarted(Search search) {

  }
}
