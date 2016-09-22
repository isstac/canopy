package edu.cmu.sv.isstac.sampling.search;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFListenerException;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.RestorableVMState;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 * Based on a modified version of DFSearch in jpf-core
 */
public class SamplingSearch extends Search {
  private final Logger logger = JPFLogger.getLogger(SamplingSearch.class.getName());

  private RestorableVMState initState;
  private PruningChoicesStrategy pruner;

  public SamplingSearch(Config config, VM vm) {
    super(config, vm);
    pruner = PruningChoicesStrategy.getInstance();
    pruner.reset();
  }

  @Override
  public void search () {
    depth = 0;
    boolean depthLimitReached = false;

    if(hasPropertyTermination()) {
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
      if (checkAndResetBacktrackRequest() || !isNewState() || isEndState() || isIgnoredState()
          || depthLimitReached) {
        logger.fine("Sample terminated");
        pruner.performPruning(getVM().getChoiceGenerator());

        //All paths have been explored, so search finishes
        if(pruner.isFullyPruned()) {
          logger.info("Sym exe tree is fully explored---search finishes");
          break;
        }

        notifySampleTerminated();

        //We start a new sample here by restoring the state, and resetting the depth
        resetJPFState();

        depthLimitReached = false;
        logger.fine("Starting new sample");

        //Notify listeners that new round of sampling is started
        notifyNewSample();
      }

      if (forward()) {
        depth++;
        notifyStateAdvanced();

        if (currentError != null){
          notifyPropertyViolated();
          if (hasPropertyTermination()) {
            logger.info("Property termination");

            checkPropertyViolation();
            resetJPFState();
            //break;
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

      } else { // forward did not execute any instructions
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

  private void notifySampleTerminated() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        if (listeners[i] instanceof SamplingSearchListener)
          ((SamplingSearchListener)listeners[i]).sampleTerminated(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during stateBacktracked() notification", t);
    }
  }

  private void notifyNewSample() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        if (listeners[i] instanceof SamplingSearchListener)
          ((SamplingSearchListener)listeners[i]).newSampleStarted(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during stateBacktracked() notification", t);
    }
  }
}
