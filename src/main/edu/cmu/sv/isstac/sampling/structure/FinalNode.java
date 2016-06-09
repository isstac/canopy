package edu.cmu.sv.isstac.sampling.structure;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class FinalNode extends Node {

  public FinalNode(Node parent, int choice) {
    super(parent, null, choice);
  }

}
