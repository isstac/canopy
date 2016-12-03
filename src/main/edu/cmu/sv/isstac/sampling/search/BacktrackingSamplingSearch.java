package edu.cmu.sv.isstac.sampling.search;

import java.util.ArrayList;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.NoPruningStrategy;
import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.PruningStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFListenerException;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
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

  private RestorableVMState initState;
  private PruningStrategy pruner;

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

      pruner = PruningChoicesStrategy.getInstance();
      pruner.reset();
    } else {

      logger.info("Search object configured with no pruning");
      // Create a dummy
      pruner = new NoPruningStrategy();
    }
  }

  @Override
  public void search() {
    depth = 0;
    boolean depthLimitReached = false;

    if (hasPropertyTermination()) {
      return;
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

      if (checkAndResetBacktrackRequest() || !isNewState || isIgnoredState) {
        pruner.performPruning(getVM().getChoiceGenerator());

        if (!backtrack()) {
          // backtrack not possible, done
          break;
        }

        ChoiceGenerator<?> nextCg = getVM().getChoiceGenerator();
        if (pruner instanceof ChoicesStrategy) {
          ChoicesStrategy choicesStrategy = (ChoicesStrategy) pruner;
          ArrayList<Integer> choices = choicesStrategy.getEligibleChoices(nextCg);
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
            throw new SamplingException("Choices strategy returned zero choices");
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
        if (isNewState) {
          logger.fine("Pruning end state");
          pruner.performPruning(getVM().getChoiceGenerator());
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
