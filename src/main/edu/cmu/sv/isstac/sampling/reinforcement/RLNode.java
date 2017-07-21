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

package edu.cmu.sv.isstac.sampling.reinforcement;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.reward.Reward;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeAdapter;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class RLNode extends NodeAdapter {
  private static final Logger logger = JPFLogger.getLogger(RLNode.class.getName());

  private Map<Integer, Double> choice2prob = new HashMap<>();

  public RLNode(Node parent, ChoiceGenerator<?> cg, int choice) {
    super(parent, cg, choice);

    //Set uniform initial probability for all choices
    double prob = 1/(double)getTotalChoicesNum();
    for(int c = 0; c < this.getTotalChoicesNum(); c++) {
      choice2prob.put(c, prob);
    }
  }


  public double getProbabilitySum(List<Integer> choices) {
    // God, streams are beautiful
    return choices.stream().mapToDouble(choice -> choice2prob.get(choice)).sum();
  }

  public double getChoiceProbability(int choice) {
    return choice2prob.get(choice);
  }

  public void setChoiceProbability(int choice, double probability) {
    choice2prob.put(choice, probability);
  }

  public double getChoiceQuality(int choice) {
    long visitCount = this.getVisitedNum();
    assert visitCount > 0;

    double quality;
    // This is important:
    // If the parent does *not* have a child node for a particular choice (i.e. the subtree
    // rooted at the choice has never been sampled), then the quality for that choice is the initial
    // probability of being selected i.e. 1/numChoices. This is the same in jpf-reliability
    if(this.hasChildForChoice(choice)) {
      RLNode child = (RLNode)this.getChild(choice);
      logger.fine("assuming succ reward *ONLY* for quality calculation (might want to make " +
          "this optional)");
      quality = child.getReward().getSucc() / (double)visitCount;
    } else {
      quality = 1 / (double)this.getTotalChoicesNum();
    }
    return quality;
  }
}
