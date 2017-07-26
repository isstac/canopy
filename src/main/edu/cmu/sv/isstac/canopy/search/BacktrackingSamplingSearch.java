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

package edu.cmu.sv.isstac.canopy.search;

import java.util.ArrayList;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.canopy.Options;
import edu.cmu.sv.isstac.canopy.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.canopy.exploration.NoPruningStrategy;
import edu.cmu.sv.isstac.canopy.exploration.PruningStrategy;
import edu.cmu.sv.isstac.canopy.exploration.TrieBasedPruningStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFListenerException;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.symbc.numeric.solvers.IncrementalListener;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.RestorableVMState;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 * This class has much in common with SamplingSearch. Make this more clean
 */
public class BacktrackingSamplingSearch extends Search {
  private static final Logger logger = JPFLogger.getLogger(BacktrackingSamplingSearch.class.getName());
  private final boolean incrementalSolving;

  private RestorableVMState initState;
  private PruningStrategy pruner;

  private SamplingAnalysisListener samplingAnalysisListener;

  public BacktrackingSamplingSearch(Config config, VM vm) {
    super(config, vm);

    // Set up pruner---if any
    // This is super ugly, but mentioned elsewhere, this seem to be the only way we can pass this
    // information to SamplingSearch, because we never get the possibility of instantiating it
    // ourselves; JPF does this automatically :/
    // WARNING: the choices strategy *MUST* be configured *before* the JPF object is created
    // since---in turn---this creates the SamplingSearch object
    if (Options.choicesStrategy instanceof PruningStrategy) {
      if (config.getBoolean("symbolic.optimizechoices", true)) {
        logger.info("PC Choice optimization is not set (option symbolic.optimizechoices). This " +
            "may or may not improve performance of the analysis");
      }

      logger.info("Search object configured with pruning");

      pruner = TrieBasedPruningStrategy.getInstance();
      pruner.reset();
    } else {

      logger.info("Search object configured with no pruning");
      // Create a dummy
      pruner = new NoPruningStrategy();
    }

    this.incrementalSolving = isIncrementalSolvingEnabled(config);
    String incSolving = ((!this.incrementalSolving) ? "*NOT* " : "") + "using incremental solving";
    logger.info(incSolving);
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

  @Override
  public void search() {
    depth = 0;
    boolean depthLimitReached = false;

    if (hasPropertyTermination()) {
      return;
    }

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

    // TODO: This is even cooler:
    // we can set the init state upon entering the target method.
    // Then each sample is drawn from here, which would be highly effective
    // if the target method is deep in the call chain.
    // It will however assume that the target method is only called ONCE
    this.initState = vm.getRestorableState();

    notifySearchStarted();
    notifyNewSample();

    while (!done) {
      boolean isIgnoredState = isIgnoredState();
      boolean isNewState = isNewState();
      boolean isEndState = isEndState();
      boolean hadBacktrackingRequest = checkAndResetBacktrackRequest();

      if (hadBacktrackingRequest || !isNewState || isIgnoredState) {
        if(hadBacktrackingRequest && this.vm.hasPendingException()) {
          logger.fine("Path terminated with error.");
          logger.info("Error termination due to exception. Note that the number of end states " +
              "reported by JPF will *NOT* correspond to the number of paths that canopy reports " +
              "because JPF does not regard uncaught exceptions as yielding end states!");
          this.samplingAnalysisListener.pathTerminated(TerminationType.ERROR, this);
        }
        if(hadBacktrackingRequest) {
          logger.fine("Pruning state from which jpf is backtracking");
        }
        if(isIgnoredState) {
          logger.fine("Pruning ignored state");
        }
        if(!isNewState) {
          logger.fine("Pruning non-new state");
        }

        pruner.performPruning(vm.getPath(), getVM().getChoiceGenerator());
        //All paths have been explored, so search finishes
        //Strictly we don't need this check here (it is also done later for a terminating
        // path---see below), but this is just to shortcircuit the search
        if (pruner.isFullyPruned()) {
          logger.info("Sym exe tree is fully explored due to pruning. Search finishes");
          break;
        }

        // Perform backtracking
        if (!backtrack()) {
          // backtrack not possible, done
          break;
        }

        ChoiceGenerator<?> nextCg = getVM().getChoiceGenerator();
        if (pruner instanceof ChoicesStrategy) {
          ChoicesStrategy choicesStrategy = (ChoicesStrategy) pruner;
          ArrayList<Integer> choices = choicesStrategy.getEligibleChoices(vm.getPath(), nextCg);
          if (choices.size() > 0) {
            //take the first eligible choice and advance the cg to it. We need to advance it
            // because, when we call cg.select in the listeners, the isDone flag will be set to
            // true, and therefore forward() will return false! This is a pretty messy way of
            // circumventing this problem, but imagine that choice 1 was explored (with cg.select)
            // for a cg. That choice turns out to be an ignored state after forward(). When
            // backtracking to the cg, isDone is set, and hasmorechoices will therefore return
            // false because there is no sensible way of advancing a state "back" to the unexplored
            // choice 0. We do this here.
            int c = choices.get(0);
            nextCg.reset();
            nextCg.advance(c);

          } else {
            //This happens when we are backtracking to a choicegenerator for which both choices
            // are already pruned. In that case we want to keep backtracking: call fo forward()
            // will later return false, thus forcing one more backtracking step.

            // It actually seems superfluous setting the isdone flag here. Later foward will
            // return false. This is just to be on the safe side.
            nextCg.setDone();
          }
        } else {
          // If we are not using pruning, then just advance the cg
          nextCg.reset();
          nextCg.advance();
        }

        depthLimitReached = false;
        depth--;
        notifyStateBacktracked();
      } else if (isEndState || depthLimitReached) {
        if(depthLimitReached) {
          logger.info("Constraint hit termination. Note that the number of end " +
              "states reported by JPF will *NOT* correspond to the number of paths that canopy " +
              "reports because JPF does not regard uncaught exceptions as yielding end states!");
          logger.fine("Path terminated with constraint hit (depth limit reached)");
          this.samplingAnalysisListener.pathTerminated(TerminationType.CONSTRAINT_HIT, this);
          logger.fine("Pruning depth limit reached state");
          pruner.performPruning(vm.getPath(), getVM().getChoiceGenerator());
        }

        if (isNewState && !depthLimitReached) {
          logger.fine("Path terminated successfully");
          this.samplingAnalysisListener.pathTerminated(TerminationType.SUCCESS, this);
          logger.fine("Pruning end state");
          pruner.performPruning(vm.getPath(), getVM().getChoiceGenerator());
        }
        //All paths have been explored, so search finishes
        if (pruner.isFullyPruned()) {
          logger.info("Sym exe tree is fully explored due to pruning. Search finishes");
          break;
        }

        //We start a new sample here by restoring the state, and resetting the depth
        resetJPFState();

        depthLimitReached = false;

        //Notify listeners that new round of sampling is started
        notifyNewSample();
      }

      if (forward()) {
        depth++;
        notifyStateAdvanced();

        if (currentError != null) {
          notifyPropertyViolated();
          if (hasPropertyTermination()) {
            logger.info("Property termination");

            checkPropertyViolation();
            resetJPFState();
          }
          // for search.multiple_errors we go on and treat this as a new state
          // but hasPropertyTermination() will issue a backtrack request
        }

        if (depth >= depthLimit) {
          logger.info("Depth limit reached");
          depthLimitReached = true;
          notifySearchConstraintHit("depth limit reached: " + depthLimit);
          continue;
        }

        if (!checkStateSpaceLimit()) {
          logger.info("State space limit reached");
          notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
          break;
        }

      } else {
        // forward did not execute any instructions
        notifyStateProcessed();
      }
    }
    notifySearchFinished();
  }

  private void resetJPFState() {
    depth = 0;
    vm.restoreState(initState);
    vm.resetNextCG();
    // Reset the variable counter for SPF
    BytecodeUtils.clearSymVarCounter();

    //reset incremental solver if used
    if(this.incrementalSolving) {
      Options.resetIncrementalSolver();
    }
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
