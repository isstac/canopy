package edu.cmu.sv.isstac.sampling.reward;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.Options;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.jvm.ClassFile;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListener;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.VMListener;

/**
 * @author Kasper Luckow
 */
public class DepthRewardFunction implements RewardFunction, VMListener {

  private static final Logger LOGGER = JPFLogger.getLogger(DepthRewardFunction.class.getName());

  //TODO Maybe this is over-engineered and prematurely optimized:
  // If we are just targeting symbolic.method, then we simply use the
  // depth obtained from the vm object. If there is a measured method, then we iterate over the
  // CGs until we reach one that is
  private interface DepthComputation {
    long compute(Search search);
    public void methodEntered(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod);
  }

  //This is the "standard" depth computation that just relies on search object's depth
  private static class JPFDepthComputation implements DepthComputation {
    @Override
    public long compute(Search search) {
      return search.getDepth();
    }

    @Override
    public void methodEntered(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {
      // Ignore
    }
  }

  private static class MeasuredMethodDepthComputation implements DepthComputation {

    private final Set<String> measuredMethods;
    private static final int DEPTH_NOT_SET = -1;
    private int startDepth = DEPTH_NOT_SET;

    public MeasuredMethodDepthComputation(Set<String> measuredMethods) {
      this.measuredMethods = measuredMethods;
    }

    @Override
    public long compute(Search search) {
      if(startDepth == DEPTH_NOT_SET) {
        String msg = "Start depth has not been set. Maybe measured method has incorrectly been " +
            "set?";
        LOGGER.severe(msg);
        throw new RewardComputationException(msg);
      }

      int realDepth = search.getDepth() - startDepth;

      // reset start depth since it may be different from sample to sample
      this.startDepth = DEPTH_NOT_SET;
      return realDepth;
    }

    @Override
    public void methodEntered(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {
      String meth = enteredMethod.getBaseName();
      if(measuredMethods.contains(meth)) {
        if(startDepth != DEPTH_NOT_SET) {
          String msg = "Entered measured method twice. *Not* setting new start depth. This may " +
              "not be what you want!";
          LOGGER.warning(msg);
        }
        else {
          this.startDepth = vm.getSearch().getDepth();
        }
      }
    }
  }

  public static final String MEASURED_METHODS_CONF = Options.SAMPLING_CONF_PREFIX +
      ".measuredmethods";

  private final DepthComputation depthComputation;

  public DepthRewardFunction(Set<String> measuredMethods) {
    this.depthComputation = new MeasuredMethodDepthComputation(measuredMethods);
  }

  public DepthRewardFunction(Config jpfConfig) {
    if (jpfConfig.hasValue(MEASURED_METHODS_CONF)) {
      String[] measMeth = jpfConfig.getStringArray(MEASURED_METHODS_CONF);
      Set<String> measuredMethods = extractSimpleMethodNames(measMeth);
      this.depthComputation = new MeasuredMethodDepthComputation(measuredMethods);
    } else {
      //Just default to JPF's notion of depth
      this.depthComputation = new JPFDepthComputation();
    }
  }

  @Override
  public long computeReward(VM vm) {
    return depthComputation.compute(vm.getSearch());
  }


  @Override
  public void methodEntered(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {
    // Forward event to the actual depth computation
    this.depthComputation.methodEntered(vm, currentThread, enteredMethod);
  }

  private static Set<String> extractSimpleMethodNames(String[] jpfMethodSpecs) {

    //TODO: This also means that we do not distinguish between overloaded methods
    String[] processedMethods = new String[jpfMethodSpecs.length];
    System.arraycopy(jpfMethodSpecs, 0, processedMethods, 0, jpfMethodSpecs.length);
    for (int i = 0; i < jpfMethodSpecs.length; i++) {
      String meth = jpfMethodSpecs[i];
      int sigBegin = meth.indexOf('(');
      if (sigBegin >= 0)
        processedMethods[i] = meth.substring(0, sigBegin);
    }
    return new HashSet<>(Arrays.asList(processedMethods));
  }

  /*
   * Ignored callbacks from VMListener interface
   */

  @Override
  public void vmInitialized(VM vm) {

  }

  @Override
  public void executeInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {

  }

  @Override
  public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {

  }

  @Override
  public void threadStarted(VM vm, ThreadInfo startedThread) {

  }

  @Override
  public void threadBlocked(VM vm, ThreadInfo blockedThread, ElementInfo lock) {

  }

  @Override
  public void threadWaiting(VM vm, ThreadInfo waitingThread) {

  }

  @Override
  public void threadNotified(VM vm, ThreadInfo notifiedThread) {

  }

  @Override
  public void threadInterrupted(VM vm, ThreadInfo interruptedThread) {

  }

  @Override
  public void threadTerminated(VM vm, ThreadInfo terminatedThread) {

  }

  @Override
  public void threadScheduled(VM vm, ThreadInfo scheduledThread) {

  }

  @Override
  public void loadClass(VM vm, ClassFile cf) {

  }

  @Override
  public void classLoaded(VM vm, ClassInfo loadedClass) {

  }

  @Override
  public void objectCreated(VM vm, ThreadInfo currentThread, ElementInfo newObject) {

  }

  @Override
  public void objectReleased(VM vm, ThreadInfo currentThread, ElementInfo releasedObject) {

  }

  @Override
  public void objectLocked(VM vm, ThreadInfo currentThread, ElementInfo lockedObject) {

  }

  @Override
  public void objectUnlocked(VM vm, ThreadInfo currentThread, ElementInfo unlockedObject) {

  }

  @Override
  public void objectWait(VM vm, ThreadInfo currentThread, ElementInfo waitingObject) {

  }

  @Override
  public void objectNotify(VM vm, ThreadInfo currentThread, ElementInfo notifyingObject) {

  }

  @Override
  public void objectNotifyAll(VM vm, ThreadInfo currentThread, ElementInfo notifyingObject) {

  }

  @Override
  public void objectExposed(VM vm, ThreadInfo currentThread, ElementInfo fieldOwnerObject, ElementInfo exposedObject) {

  }

  @Override
  public void objectShared(VM vm, ThreadInfo currentThread, ElementInfo sharedObject) {

  }

  @Override
  public void gcBegin(VM vm) {

  }

  @Override
  public void gcEnd(VM vm) {

  }

  @Override
  public void exceptionThrown(VM vm, ThreadInfo currentThread, ElementInfo thrownException) {

  }

  @Override
  public void exceptionBailout(VM vm, ThreadInfo currentThread) {

  }

  @Override
  public void exceptionHandled(VM vm, ThreadInfo currentThread) {

  }

  @Override
  public void choiceGeneratorRegistered(VM vm, ChoiceGenerator<?> nextCG, ThreadInfo currentThread, Instruction executedInstruction) {

  }

  @Override
  public void choiceGeneratorSet(VM vm, ChoiceGenerator<?> newCG) {

  }

  @Override
  public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {

  }

  @Override
  public void choiceGeneratorProcessed(VM vm, ChoiceGenerator<?> processedCG) {

  }

  @Override
  public void methodExited(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {

  }
}
