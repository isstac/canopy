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

package edu.cmu.sv.isstac.sampling.search;


import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.Options;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFListenerException;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.solvers.IncrementalListener;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.VM;


/**
 * Kasper: This is basically a copy of DFSearch from jpf-core, where the depth starts at -1.
 * Yes, it does seem counter-intuitive: it is used in the exhaustive shell to make
 * exhaustive results comparable to the sampling results, since the sampling analyses seem to
 * have an off by one error in the depth reward spec.
 */

/**
 * standard depth first model checking (but can be bounded by search depth
 * and/or explicit Verify.ignoreIf)
 */
public class DepthSyncedDFSearch extends Search {
  private static final Logger logger = JPFLogger.getLogger(DepthSyncedDFSearch.class.getName());

  private final boolean incrementalSolving;
  private SamplingAnalysisListener samplingAnalysisListener;

  public DepthSyncedDFSearch(Config config, VM vm) {
  	super(config,vm);


    this.incrementalSolving = isIncrementalSolvingEnabled(config);
    String incSolving = ((!this.incrementalSolving) ? "*NOT* " : "") + "using incremental solving";
    logger.info(incSolving);
  }

  @Override
  public boolean requestBacktrack () {
    doBacktrack = true;

    return true;
  }

  /**
   * state model of the search
   *    next new  -> action
   *     T    T      forward
   *     T    F      backtrack, forward
   *     F    T      backtrack, forward
   *     F    F      backtrack, forward
   *
   * end condition
   *    backtrack failed (no saved states)
   *  | property violation (currently only checked in new states)
   *  | search constraint (depth or memory or time)
   *
   * <2do> we could split the properties into forward and backtrack properties,
   * the latter ones being usable for liveness properties that are basically
   * condition accumulators for sub-paths of the state space, to be checked when
   * we backtrack to the state where they were introduced.
   */
  @Override
  public void search () {
    boolean depthLimitReached = false;

    depth = 0;

    //Get the sampling analysis listener object
    for (int i = 0; i < listeners.length; i++) {
      if (listeners[i] instanceof SamplingAnalysisListener)
        this.samplingAnalysisListener = (SamplingAnalysisListener)listeners[i];
    }
    if(this.samplingAnalysisListener == null) {
      throw new SamplingException("Sampling analysis listener not properly set up");
    }

    //reset incremental solver before we start
    //We do this to ensure that state is reset even
    //if batch processing is used
    if(this.incrementalSolving) {
      Options.resetIncrementalSolver();
    }

    notifySearchStarted();

    while (!done) {
      boolean isIgnoredState = isIgnoredState();
      boolean isNewState = isNewState();
      boolean isEndState = isEndState();
      boolean hadBacktrackingRequest = checkAndResetBacktrackRequest();

      if (hadBacktrackingRequest || !isNewState || isEndState || isIgnoredState || depthLimitReached) {
        if(hadBacktrackingRequest && this.vm.hasPendingException()) {
          logger.fine("Path terminated with error.");
          logger.info("Error termination due to exception. Note that the number of end states " +
              "reported by JPF will *NOT* correspond to the number of paths that canopy reports " +
              "because JPF does not regard uncaught exceptions as yielding end states!");
          this.samplingAnalysisListener.pathTerminated(TerminationType.ERROR, this);
        } else if(depthLimitReached) {
          logger.info("Constraint hit termination. Note that the number of end " +
              "states reported by JPF will *NOT* correspond to the number of paths that canopy " +
              "reports because JPF does not regard uncaught exceptions as yielding end states!");
          logger.fine("Path terminated with constraint hit (depth limit reached)");
          this.samplingAnalysisListener.pathTerminated(TerminationType.CONSTRAINT_HIT, this);
        } else if(isEndState && !depthLimitReached && isNewState) {
          logger.fine("Path terminated successfully");
          this.samplingAnalysisListener.pathTerminated(TerminationType.SUCCESS, this);
        }

        // Perform backtracking
        if (!backtrack()) {
          // backtrack not possible, done
          break;
        }

        depthLimitReached = false;
        depth--;
        notifyStateBacktracked();
      }

      if (forward()) {
        depth++;
        notifyStateAdvanced();

        if (currentError != null){
          notifyPropertyViolated();

          if (hasPropertyTermination()) {
            break;
          }
          // for search.multiple_errors we go on and treat this as a new state
          // but hasPropertyTermination() will issue a backtrack request
        }

        if (depth >= depthLimit) {
          depthLimitReached = true;
          notifySearchConstraintHit("depth limit reached: " + depthLimit);
          continue;
        }

        if (!checkStateSpaceLimit()) {
          logger.info("Depth limit reached");
          notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
          // can't go on, we exhausted our memory
          break;
        }

      } else { // forward did not execute any instructions
        notifyStateProcessed();
      }
    }

    notifySearchFinished();
  }


  @Override
  public boolean supportsBacktrack () {
    return true;
  }

  private boolean isIncrementalSolvingEnabled(Config conf) {
    String[] listeners = conf.getStringArray("listener");
    if(listeners != null) {
      for (String listener : listeners) {
        if (listener.equals(IncrementalListener.class.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  private void notifyNewSample() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        if (listeners[i] instanceof SamplingListener)
          ((SamplingListener) listeners[i]).newSampleStarted(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during stateBacktracked() notification", t);
    }
  }
}
