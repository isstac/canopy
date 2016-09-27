package edu.cmu.sv.isstac.sampling.structure;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public interface NodeFactory<T extends Node> {
  
  public T create(T parent, ChoiceGenerator<?> currentCG, int choice);
  
  public boolean isSupportedChoiceGenerator(ChoiceGenerator<?> cg);
}
