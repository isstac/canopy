package edu.cmu.sv.isstac.sampling.structure;

/**
 * @author Kasper Luckow
 *
 */
public class FinalNode extends NodeAdapter {

  public FinalNode(Node parent, int choice) {
    super(parent, null, choice);
  }

}
