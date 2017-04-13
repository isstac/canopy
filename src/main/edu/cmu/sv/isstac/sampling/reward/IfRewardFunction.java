package edu.cmu.sv.isstac.sampling.reward;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.sv.isstac.sampling.search.SamplingListener;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class IfRewardFunction extends PropertyListenerAdapter implements RewardFunction,
    SamplingListener {

  private long cost = 0;
  private Map<ChoiceGenerator<?>, Long> costMap = new HashMap<>();

  @Override
  public long computeReward(VM vm) {
    return cost;
  }

  public void instructionExecuted(VM vm, ThreadInfo currentThread,
                                  Instruction nextInstruction, Instruction executedInstruction) {
    if (executedInstruction instanceof IfInstruction) {
      cost++;
    }
  }

  @Override
  public void newSampleStarted(Search samplingSearch) {
    cost = 0;
    costMap.clear();
  }

  @Override
  public void stateBacktracked(Search search) {
    this.cost = this.costMap.get(search.getVM().getChoiceGenerator());
  }

  @Override
  public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
    this.costMap.put(currentCG, this.cost);
  }
}