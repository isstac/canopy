package edu.cmu.sv.isstac.sampling.structure;

import com.google.common.base.Preconditions;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class PCNode extends Node {
  private final PathCondition pc;

  public PCNode(Node parent, ChoiceGenerator<?> cg, int choice) {
    super(parent, cg, choice);

    Preconditions.checkArgument(cg instanceof PCChoiceGenerator);

    //NOTE: This can be **very** expensive to compute for deep paths!
    this.pc = ((PCChoiceGenerator)cg).getCurrentPC().make_copy();
  }

  public PathCondition getPathCondition() {
    return this.pc;
  }

}
