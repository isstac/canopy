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

import edu.cmu.sv.isstac.sampling.quantification.PathUtil;
import gov.nasa.jpf.jvm.bytecode.IFEQ;
import gov.nasa.jpf.jvm.bytecode.IFGE;
import gov.nasa.jpf.jvm.bytecode.IFGT;
import gov.nasa.jpf.jvm.bytecode.IFLE;
import gov.nasa.jpf.jvm.bytecode.IFLT;
import gov.nasa.jpf.jvm.bytecode.IFNE;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPEQ;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPGE;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPGT;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPLE;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPLT;
import gov.nasa.jpf.jvm.bytecode.IF_ICMPNE;
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
import modelcounting.analysis.Analyzer;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.utils.BigRational;

/**
 * @author Kasper Luckow
 */
public class CountWeightedSimulationPolicy implements SimulationPolicy {

  private static final Logger LOGGER = JPFLogger.getLogger(
      CountWeightedSimulationPolicy.class.getName());

  private final LoadingCache<PathCondition, BigRational> countCache;
  private final Random rng;

  //TODO: consider getting rid of Random instance
  public CountWeightedSimulationPolicy(Analyzer runtimeAnalyzer, Random rng) {
    this.rng = rng;

    this.countCache = CacheBuilder.newBuilder()
        .build(new CacheLoader<PathCondition, BigRational>() {
      @Override
      public BigRational load(PathCondition pc) throws AnalysisException {
        if(pc.header == null) {
          return BigRational.ONE;
        }

        String pString = PathUtil.clean(pc);
        return runtimeAnalyzer.analyzeSpfPC(pString);
      }
    });
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

    LOGGER.info("simulation policy selected choice " + choice + " for condition at line " +
        currentCg.getInsn().getLineNumber());
    return choice;
  }

  private int makeWeightedChoice(PCChoiceGenerator pcCg, VM vm) {
    PathCondition pcBeforeChoice = pcCg.getCurrentPC();
    if (pcBeforeChoice == null) {
      pcBeforeChoice = new PathCondition();
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

    /* this is from jpf-reliability. I dont think the check is necessary
    if(pcAfterChoice == null || pcAfterChoice.header == null) {
			return 1;
		}
    */

    BigRational countBefore;
    BigRational countAfter;
    try {
      countBefore = this.countCache.get(pcBeforeChoice);
      countAfter = this.countCache.get(pcAfterChoice);

    } catch (ExecutionException e) {
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

    if (zeroComparators.containsKey(condInstr.getClass())) {
      // Single operand
      IntegerExpression sym_v = (IntegerExpression) sf.getOperandAttr();
      Preconditions.checkNotNull(sym_v);
      pcAfterChoice._addDet(zeroComparators.get(condInstr.getClass()), sym_v, 0);
    } else if (variableComparators.containsKey(condInstr.getClass())) {
      int v2 = ti.getModifiableTopFrame().peek();
      int v1 = ti.getModifiableTopFrame().peek(1);
      IntegerExpression sym_v1 = (IntegerExpression) sf.getOperandAttr(1);
      IntegerExpression sym_v2 = (IntegerExpression) sf.getOperandAttr(0);
      if (sym_v1 != null) {
        if (sym_v2 != null) { // both are symbolic values
          pcAfterChoice._addDet(variableComparators.get(condInstr.getClass()), sym_v1, sym_v2);
        } else {
          pcAfterChoice._addDet(variableComparators.get(condInstr.getClass()), sym_v1, v2);
        }
      } else {
        Preconditions.checkNotNull(sym_v2);
        pcAfterChoice._addDet(variableComparators.get(condInstr.getClass()), v1, sym_v2);
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

    String allowed = j.join(
        Iterables.transform(zeroComparators.keySet(),
            (Class<? extends IfInstruction> s) -> s.getName()));
    allowed += ", ";
    allowed += j.join(
        Iterables.transform(variableComparators.keySet(),
            (Class<? extends IfInstruction> s) -> s.getName()));

    String msg = "Got conditional instruction " + condInstr.getClass().getName() + " which is " +
        "not supported! Supported conditionals: " + allowed;
    return msg;
  }

  private final ImmutableMap<Class<? extends IfInstruction>, Comparator> zeroComparators = ImmutableMap
      .<Class<? extends IfInstruction>, Comparator>builder()
      .put(IFEQ.class, Comparator.EQ)
      .put(IFGT.class, Comparator.GT)
      .put(IFGE.class, Comparator.GE)
      .put(IFLE.class, Comparator.LE)
      .put(IFLT.class, Comparator.LT)
      .put(IFNE.class, Comparator.NE)
      .build();

  private final ImmutableMap<Class<? extends IfInstruction>, Comparator> variableComparators = ImmutableMap
      .<Class<? extends IfInstruction>, Comparator>builder()
      .put(IF_ICMPEQ.class, Comparator.EQ)
      .put(IF_ICMPGE.class, Comparator.GE)
      .put(IF_ICMPGT.class, Comparator.GT)
      .put(IF_ICMPLE.class, Comparator.LE)
      .put(IF_ICMPLT.class, Comparator.LT)
      .put(IF_ICMPNE.class, Comparator.NE)
      .build();
}
