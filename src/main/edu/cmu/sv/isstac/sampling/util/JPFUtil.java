package edu.cmu.sv.isstac.sampling.util;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class JPFUtil {

  public static int getCurrentChoiceOfCG(ChoiceGenerator<?> cg) {
    //BIG FAT WARNING:
    //This is in general UNSAFE to do,
    //because there is NO guarantee that choices are selected
    //incrementally! However, there does not seem to be another
    //way of obtaining a lightweight representation of the path
    //i.e. a sequence of decisions (represented by ints)
    //I think it is safe for ThreadChoiceFromSet (currently our only nondeterministic choice)
    //and PCChoiceGenerator
    return cg.getProcessedNumberOfChoices() - 1;
  }

  public static int getLastChoiceOfPath(VM vm) {
    ChoiceGenerator<?> lastCg = vm.getChoiceGenerator();
    return JPFUtil.getCurrentChoiceOfCG(lastCg);
  }

  public static String clean(PathCondition pc) {
    //TODO: Review: is it correct to return true here?
    return (pc.header == null) ? "TRUE" : clean(pc.header.toString());
  }

  public static String clean(String constraintsString) {
    String cleanPC = constraintsString.replaceAll("\\s+", "");
    cleanPC = cleanPC.replaceAll("CONST_(\\d+)", "$1");
    cleanPC = cleanPC.replaceAll("CONST_-(\\d+)", "-$1");
    cleanPC = cleanPC.replaceAll("\\[", "LS");
    cleanPC = cleanPC.replaceAll("\\]", "RS");
    cleanPC = cleanPC.replaceAll("^\\s-^\\s", "");
    return cleanPC;
  }
}
