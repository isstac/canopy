package edu.cmu.sv.isstac.sampling.structure;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class PCNode extends Node {

  public PCNode(Node parent, ChoiceGenerator<?> cg, int choice) {
    super(parent, cg, choice);
  }

}
