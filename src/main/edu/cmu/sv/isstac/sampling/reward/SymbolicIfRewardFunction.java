package edu.cmu.sv.isstac.sampling.reward;

import edu.cmu.sv.isstac.sampling.search.SamplingListener;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.DoubleCompareInstruction;
import gov.nasa.jpf.jvm.bytecode.FCMPG;
import gov.nasa.jpf.jvm.bytecode.FCMPL;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPEQ;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPGE;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPGT;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPLE;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPLT;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPNE;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class SymbolicIfRewardFunction extends PropertyListenerAdapter implements RewardFunction,
    SamplingListener {

  private long reward = 0;

  @Override
  public long computeReward(VM vm) {
    return reward;
  }

  public void instructionExecuted(VM vm, ThreadInfo currentThread,
                                  Instruction nextInstruction, Instruction executedInstruction) {
    if (isSymbolicIf(executedInstruction, currentThread)) {
      reward++;
    }
  }

  @Override
  public void newSampleStarted(Search samplingSearch) {
    this.reward = 0;
  }

  //frustrating to have to write something like this...
  private static boolean isSymbolicIf(Instruction ifInstr, ThreadInfo threadInfo) {
    //I'm not sure why this check is needed,
    //but for some reason top frame can be null!
    if(threadInfo.getTopFrame() == null) {
      return false;
    }
    StackFrame sf = threadInfo.getModifiableTopFrame();
    Object sym1 = null, sym2 = null;

    //Important check---otherwise core can throw an assertion error when trying to obtain the
    // operand attributes
    if(!sf.hasOperandAttr()) {
      return false;
    }
    if (ifInstr instanceof IfInstruction) {
      sym1 = sf.getOperandAttr(0);
      //short circuiting
      if (sym1 != null) return true;

      if (ifInstr instanceof IF_ICMPEQ ||
          ifInstr instanceof IF_ICMPGE ||
          ifInstr instanceof IF_ICMPGT ||
          ifInstr instanceof IF_ICMPLE ||
          ifInstr instanceof IF_ICMPLT ||
          ifInstr instanceof IF_ICMPNE) {
        sym1 = sf.getOperandAttr(1);
      }
    } else if (ifInstr instanceof DoubleCompareInstruction) {
      sym1 = sf.getOperandAttr(1);
      sym2 = sf.getOperandAttr(3);
    } else if (ifInstr instanceof FCMPG ||
        ifInstr instanceof FCMPL) { //symbolic information is attached to the same operands as IF_* and IF*
      sym1 = sf.getOperandAttr(0);
      sym2 = sf.getOperandAttr(1);
    }
    return (sym1 != null || sym2 != null);
  }
}