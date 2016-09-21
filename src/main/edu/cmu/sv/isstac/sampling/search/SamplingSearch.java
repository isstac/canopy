package edu.cmu.sv.isstac.sampling.search;

import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFListenerException;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.vm.RestorableVMState;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 * Based on a modified version of DFSearch in jpf-core
 */
public class SamplingSearch extends Search {
  public SamplingSearch(Config config, VM vm) {
    super(config, vm);

  }

  @Override
  public void search () {
    depth = 0; // should it be 1 or 0?
    boolean depthLimitReached = false;

    if(hasPropertyTermination()) {
      return;
    }

    // TODO: This is even cooler:
    // we can set the init state upon entering the target method.
    // Then each sample is drawn from here, which would be highly effective
    // if the target method is deep in the call chain.
    // It will however assume that the target method is only called ONCE
    RestorableVMState initState = vm.getRestorableState();

    PruningChoicesStrategy pruner = PruningChoicesStrategy.getInstance();

    notifySearchStarted();
    notifyNewSample();

    while (!done) {
      if (checkAndResetBacktrackRequest() || !isNewState() || isEndState() || isIgnoredState()
          || depthLimitReached) {
        pruner.performPruning(getVM().getChoiceGenerator());
        notifySampleTerminated();

        //We start a new sample here by restoring the state, and resetting the depth
        depth = 0;
        vm.restoreState(initState);
        vm.resetNextCG();

        // Reset the variable counter for SPF
        BytecodeUtils.clearSymVarCounter();

        depthLimitReached = false;

        //Notify listeners that new round of sampling is started
        notifyNewSample();
      }

      if (forward()) {
        depth++;
        notifyStateAdvanced();

        if (currentError != null){
          notifyPropertyViolated();

          if (hasPropertyTermination()) {
            checkPropertyViolation();
            //  done = true;
            depth = 0;
            vm.restoreState(initState);
            vm.resetNextCG();
            // Reset the variable counter for SPF
            BytecodeUtils.clearSymVarCounter();
            //break;
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
          notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
          break;
        }

      } else { // forward did not execute any instructions
        notifyStateProcessed();
      }
    }
    notifySearchFinished();
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
