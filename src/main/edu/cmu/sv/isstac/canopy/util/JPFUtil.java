/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.cmu.sv.isstac.canopy.util;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class JPFUtil {

  public static int getCurrentChoiceOfCG(ChoiceGenerator<?> cg) {
    //BIG FAT WARNING:
    //This is in general UNSAFE to do,
    //because there is NO guarantee that choices are selected
    //incrementally! However, there does not seem to be another
    //way of obtaining a lightweight representation of the path
    //i.e. a sequence of decisions (represented by ints)
    //I think it is safe for ThreadChoiceFromSet (currently our only nondeterministic choice)
    //and PCChoiceGenerator
    return cg.getProcessedNumberOfChoices() - 1;
  }

  public static int getLastChoiceOfPath(VM vm) {
    ChoiceGenerator<?> lastCg = vm.getChoiceGenerator();
    return JPFUtil.getCurrentChoiceOfCG(lastCg);
  }

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
