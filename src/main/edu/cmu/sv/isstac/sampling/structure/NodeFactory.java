package edu.cmu.sv.isstac.sampling.structure;

import java.util.logging.Logger;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;

/**
 * @author Kasper Luckow
 *
 */
public interface NodeFactory {
  
  public Node create(Node parent, ChoiceGenerator<?> currentCG, int choice);
  
  public boolean isSupportedChoiceGenerator(ChoiceGenerator<?> cg);
}
