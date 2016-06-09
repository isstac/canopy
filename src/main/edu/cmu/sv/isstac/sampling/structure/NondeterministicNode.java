package edu.cmu.sv.isstac.sampling.structure;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class NondeterministicNode extends Node {

  public NondeterministicNode(Node parent, ChoiceGenerator<?> cg, int choice) {
    super(parent, cg, choice);
  }

}
