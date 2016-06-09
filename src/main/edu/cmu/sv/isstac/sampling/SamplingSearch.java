package edu.cmu.sv.isstac.sampling;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.vm.RestorableVMState;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 * Based on a modified version of Simulation Search in jpf-core
 */
public class SamplingSearch extends Search {
  public SamplingSearch(Config config, VM vm) {
    super(config, vm);
  }

  @Override
  public void search () {
    depth = 1; // should it be 1 or 0?

    if(hasPropertyTermination()) {
      return;
    }

    // TODO: This is even cooler:
    // we can set the init state upon entering the target method.
    // Then each sample is drawn from here, which would be highly effective
    // if the target method is deep in the call chain.
    // It will however assume that the target method is only called ONCE
    RestorableVMState initState = vm.getRestorableState();

    notifySearchStarted();
    while(!done) {
      if((depth < depthLimit) && forward()) {
        notifyStateAdvanced();

        if(currentError != null){
          notifyPropertyViolated();
          if (hasPropertyTermination()) {
            return;
          }
        }

        depth++;

      } else {
        if(depth >= depthLimit) {
          notifySearchConstraintHit("depth limit reached: " + depthLimit);
        }
        checkPropertyViolation();
        depth = 1;
        vm.restoreState(initState);
        vm.resetNextCG();

        // Reset the variable counter for SPF
        BytecodeUtils.clearSymVarCounter();
      }
    }
    notifySearchFinished();
  }
}
