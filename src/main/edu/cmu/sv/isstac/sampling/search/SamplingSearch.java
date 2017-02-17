package edu.cmu.sv.isstac.sampling.search;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.exploration.NoPruningStrategy;
import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.PruningStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFListenerException;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.symbc.numeric.PCParser;
import gov.nasa.jpf.symbc.numeric.solvers.IncrementalListener;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.RestorableVMState;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 * This class has much in common with BacktrackSamplingSearch. Make this more clean
 */
public class SamplingSearch extends Search {
  private static final Logger logger = JPFLogger.getLogger(SamplingSearch.class.getName());

  private RestorableVMState initState;
  private PruningStrategy pruner;
  private final boolean incrementalSolving;

  public SamplingSearch(Config config, VM vm) {
    super(config, vm);

    // Set up pruner---if any
    // This is super ugly, but mentioned elsewhere, this seem to be the only way we can pass this
    // information to SamplingSearch, because we never get the possibility of instantiating it
    // ourselves; JPF does this automatically :/
    // WARNING: the choices strategy *MUST* be configured *before* the JPF object is created
    // since---in turn---this creates the SamplingSearch object
    if(Options.choicesStrategy instanceof PruningStrategy) {
      if(!config.getBoolean("symbolic.optimizechoices", true)) {
        logger.warning("PC Choice optimization not set (option symbolic.optimizechoices). " +
            "Sampling may proceed to explore ignored states. They are not regarded as terminated " +
            "paths, but they can influence decisions if they are based on collecting data during " +
            "sampling and not on actual terminating paths. Also, for MCTS performance is reduced " +
            "significantly");
      }

      logger.info("Search object configured with pruning");

      pruner = PruningChoicesStrategy.getInstance();
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
  public void search () {
    depth = 0;
    boolean depthLimitReached = false;

    if(hasPropertyTermination()) {
      return;
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
      boolean checkAndResetBacktrackRequest = checkAndResetBacktrackRequest();
      boolean isIgnoredState = isIgnoredState();

      if (checkAndResetBacktrackRequest() || !isNewState() || isEndState() || isIgnoredState
          || depthLimitReached) {
        if(isIgnoredState) {
          String msg = "Sampled an ignored state! Pruning this path. This issue can be solved by";
          logger.severe(msg);
        }

        logger.fine("Sample terminated");
        pruner.performPruning(getVM().getChoiceGenerator());

        //All paths have been explored, so search finishes
        if(pruner.isFullyPruned()) {
          logger.info("Sym exe tree is fully explored due to pruning. Search finishes");
          break;
        }

        //notifySampleTerminated();

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

    if(this.incrementalSolving) {
      Options.resetIncrementalSolver();
    }
  }

  private void notifyNewSample() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        if (listeners[i] instanceof SamplingListener)
          ((SamplingListener)listeners[i]).newSampleStarted(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during stateBacktracked() notification", t);
    }
  }
}
