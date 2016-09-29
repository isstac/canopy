package edu.cmu.sv.isstac.sampling.policies;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.utils.BigRational;

/**
 * @author Kasper Luckow
 */
public class CountWeightedSimulationPolicy implements SimulationPolicy {

  private static final Logger LOGGER = JPFLogger.getLogger(
      CountWeightedSimulationPolicy.class.getName());

  private final SPFModelCounter modelCounter;
  private final Random rng;

  public CountWeightedSimulationPolicy(SPFModelCounter modelCounter, long seed) {
    this.modelCounter = modelCounter;
    this.rng = new Random(seed);
  }

  @Override
  public int selectChoice(VM vm, ChoiceGenerator<?> currentCg, ArrayList<Integer> eligibleChoices) {
    int choice;
    //If---due to pruning---only one choice is available, just select it...
    if(eligibleChoices.size() == 1) {
      choice = eligibleChoices.get(0);
    } else {
      if (currentCg instanceof PCChoiceGenerator) {
        PCChoiceGenerator pcCg = (PCChoiceGenerator) currentCg;
        choice = makeWeightedChoice(pcCg, vm);
      } else {
        //This would be the case for e.g. threadchoicefromset (non-deterministic choices)
        //We will resolve those situations by issuing a warning and simply
        //select an eligible choice uniformly. Note that the analysis result is likely not
        //useful *at all*

        String msg = "The analysis assumes choicegenerators of type " +
            PCChoiceGenerator.class.getName() + ". Instead, it found " + currentCg.getClass()
            .getName() + " which will simply be resolved by uniformly selecting an eligible choice." +
            " NOTE: This is likely not a wanted situation!";
        LOGGER.warning(msg);

        int idx = rng.nextInt(eligibleChoices.size());
        choice = eligibleChoices.get(idx);
      }
    }

    LOGGER.fine("Count weighted policy selected " + "choice " + choice +
        "for condition at line " + currentCg.getInsn().getLineNumber());
    return choice;
  }

  private int makeWeightedChoice(PCChoiceGenerator pcCg, VM vm) {
    PathCondition pcBeforeChoice = null;

    // We need to obtain the PC from the **PREVIOUS** PC because at this point, the input
    // choicegenerator (pcCg), has not been updated yet.
    PCChoiceGenerator prevPCCg = pcCg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
    if(prevPCCg == null) {
      pcBeforeChoice = new PathCondition();
    } else {
      pcBeforeChoice = prevPCCg.getCurrentPC();
      if(pcBeforeChoice == null) {
        pcBeforeChoice = new PathCondition();
      }
    }

    //TODO: I'm not sure if this pc will *always* correspond to
    //taking choice 0 or 1
    //TODO: either take the conditional instruction from the pccg or
    //just get it from the vm instance
    PathCondition pcAfterChoice = buildPCAfterChoice(pcBeforeChoice, vm,
        (IfInstruction) pcCg.getInsn());

    //TODO: check if it is correct to continue with choice 0
    //The rationale for selecting 1 if it pcAfterChoice does not simplify is that
    //the pc we are simplifying is for choice 1
    if (!pcAfterChoice.simplify()) {
      return 0;
    }

    BigRational countBefore;
    BigRational countAfter;
    try {
      countBefore = this.modelCounter.analyzeSpfPC(pcBeforeChoice);
      countAfter = this.modelCounter.analyzeSpfPC(pcAfterChoice);
    } catch (AnalysisException e) {
      LOGGER.severe(e.getMessage());
      throw new SimulationPolicyException(e);
    }

    if(countBefore.equals(BigRational.ZERO)) {
      return 0;
    }

    BigRational dice = new BigRational(this.rng.nextDouble());

    // compute the conditional probability of the next choice given the previous PC
    BigRational conditionalProbability = getConditionalProbability(countBefore, countAfter);

    if(conditionalProbability.equals(BigRational.ONE)) {
      return 1;
    }
    if(conditionalProbability.equals(BigRational.ZERO)) {
      return 0;
    }

    if(dice.compareTo(conditionalProbability) > 0) {
      return 0;
    } else {
      return 1;
    }
  }

  private BigRational getConditionalProbability(BigRational countBefore, BigRational countAfter) {
    return countAfter.div(countBefore);
  }

  private PathCondition buildPCAfterChoice(PathCondition previousPC, VM vm, IfInstruction
      condInstr) {
    ThreadInfo ti = vm.getCurrentThread();
    StackFrame sf = ti.getTopFrame();

    PathCondition pcAfterChoice = previousPC.make_copy();
    BranchInfo branchInfo = bytecodeToComparator.get(condInstr.getByteCode());
    if(branchInfo == null) {
      String msg = getExceptionMsg(condInstr);
      LOGGER.severe(msg);
      throw new SimulationPolicyException(msg);
    }

    if(branchInfo.type == BRANCH_TYPE.ZERO) {
      // Single operand
      IntegerExpression sym_v = (IntegerExpression) sf.getOperandAttr();
      Preconditions.checkNotNull(sym_v);
      pcAfterChoice._addDet(branchInfo.comparator, sym_v, 0);
    } else if(branchInfo.type == BRANCH_TYPE.VARIABLE) {
      int v2 = ti.getModifiableTopFrame().peek();
      int v1 = ti.getModifiableTopFrame().peek(1);
      IntegerExpression sym_v1 = (IntegerExpression) sf.getOperandAttr(1);
      IntegerExpression sym_v2 = (IntegerExpression) sf.getOperandAttr(0);
      if(sym_v1 != null) {
        if(sym_v2 != null) { // both are symbolic values
          pcAfterChoice._addDet(branchInfo.comparator, sym_v1, sym_v2);
        } else {
          pcAfterChoice._addDet(branchInfo.comparator, sym_v1, v2);
        }
      } else {
        Preconditions.checkNotNull(sym_v2);
        pcAfterChoice._addDet(branchInfo.comparator, v1, sym_v2);
      }
    } else {
      String msg = getExceptionMsg(condInstr);
      LOGGER.severe(msg);
      throw new SimulationPolicyException(msg);
    }

    return pcAfterChoice;
  }

  private String getExceptionMsg(IfInstruction condInstr) {
    Joiner j = Joiner.on(", ");
    //TODO: Only show bytecodes---no mnemonics
    String allowed = j.join(bytecodeToComparator.keySet());
    String msg = "Got conditional instruction " + condInstr.getClass().getName() + " which is " +
        "not supported! Supported conditionals: " + allowed;
    return msg;
  }

  private final ImmutableMap<Integer, BranchInfo> bytecodeToComparator =
      ImmutableMap
      .<Integer, BranchInfo>builder()
      .put(0x99, BranchInfo.create(BRANCH_TYPE.ZERO, Comparator.EQ)) // IFEQ
      .put(0x9D, BranchInfo.create(BRANCH_TYPE.ZERO, Comparator.GT)) // IFGT
      .put(0x9C, BranchInfo.create(BRANCH_TYPE.ZERO, Comparator.GE)) // IFGE
      .put(0x9E, BranchInfo.create(BRANCH_TYPE.ZERO, Comparator.LE)) // IFLE
      .put(0x9B, BranchInfo.create(BRANCH_TYPE.ZERO, Comparator.LT)) // IFLT
      .put(0x9A, BranchInfo.create(BRANCH_TYPE.ZERO, Comparator.NE)) // IFNE
      .put(0x9F, BranchInfo.create(BRANCH_TYPE.VARIABLE, Comparator.EQ)) // IF_ICMPEQ
      .put(0xA2, BranchInfo.create(BRANCH_TYPE.VARIABLE, Comparator.GE)) // IF_ICMPGE
      .put(0xA3, BranchInfo.create(BRANCH_TYPE.VARIABLE, Comparator.GT)) // IF_ICMPGT
      .put(0xA4, BranchInfo.create(BRANCH_TYPE.VARIABLE, Comparator.LE)) // IF_ICMPLE
      .put(0xA1, BranchInfo.create(BRANCH_TYPE.VARIABLE, Comparator.LT)) // IF_ICMPLT
      .put(0xA0, BranchInfo.create(BRANCH_TYPE.VARIABLE, Comparator.NE)) // IF_ICMPNE
      .build();

  private static class BranchInfo {
    public BRANCH_TYPE type;
    public Comparator comparator;
    private BranchInfo(BRANCH_TYPE type, Comparator comp) {
      this.type = type;
      this.comparator = comp;
    }
    public static BranchInfo create(BRANCH_TYPE type, Comparator comp) {
      return new BranchInfo(type, comp);
    }
  }

  private enum BRANCH_TYPE {
    ZERO,
    VARIABLE
  }
}
