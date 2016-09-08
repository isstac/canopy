package edu.cmu.sv.isstac.sampling.quantification;

import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class ConcretePathQuantifier implements PathQuantifier {

  @Override
  public long quantifyPath(VM vm) {
    return 1;
  }
}
