package edu.cmu.sv.isstac.sampling.mcts.quantification;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 */
public class PathUtil {

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
