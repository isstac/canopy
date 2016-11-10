package edu.cmu.sv.isstac.sampling.reward;

import edu.cmu.sv.isstac.sampling.search.SamplingListener;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class IfRewardFunction extends PropertyListenerAdapter implements RewardFunction,
    SamplingListener {

  private long reward = 0;

  @Override
  public long computeReward(VM vm) {
    return reward;
  }

  public void instructionExecuted(VM vm, ThreadInfo currentThread,
                                  Instruction nextInstruction, Instruction executedInstruction) {
    if (executedInstruction instanceof IfInstruction) {
      reward++;
    }
  }

  @Override
  public void newSampleStarted(Search samplingSearch) {
    this.reward = 0;
  }
}