package edu.cmu.sv.isstac.sampling.quantification;

import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public interface PathQuantifier {
  public long quantifyPath(VM vm);
}
