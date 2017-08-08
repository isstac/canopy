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

package edu.cmu.sv.isstac.canopy.montecarlo;

import static edu.cmu.sv.isstac.canopy.structure.CGClassification.isNondeterministicChoice;
import static edu.cmu.sv.isstac.canopy.structure.CGClassification.isPCNode;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.canopy.AnalysisStrategy;
import edu.cmu.sv.isstac.canopy.policies.SimulationPolicy;
import edu.cmu.sv.isstac.canopy.search.TerminationType;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;;

/**
 * @author Kasper Luckow
 *
 */
public class MonteCarloStrategy implements AnalysisStrategy {
    private static final Logger logger = JPFLogger.getLogger(edu.cmu.sv.isstac.canopy.montecarlo
        .MonteCarloStrategy.class.getName());

    private final SimulationPolicy simulationPolicy;

    public MonteCarloStrategy(SimulationPolicy simulationPolicy) {
      this.simulationPolicy = simulationPolicy;
    }

    @Override
    public void makeStateChoice(VM vm, ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices) {
      if(isPCNode(cg) || isNondeterministicChoice(cg)) {

        // If empty, we entered an invalid state
        if(eligibleChoices.isEmpty()) {
          String msg = "Entered invalid state: No eligible choices";
          logger.severe(msg);
          throw new MonteCarloAnalysisException(msg);
        }

        // Select a choice according to the simulation
        // strategy, e.g., randomized selection
        int choice = simulationPolicy.selectChoice(vm, cg, eligibleChoices);
        cg.select(choice);
      } else {
        String msg = "Unexpected CG: " + cg.getClass().getName();
        logger.severe(msg);
//      throw new MonteCarloAnalysisException(msg);
      }
    }

    @Override
    public void newSampleStarted(Search samplingSearch) {

    }

    @Override
    public void pathTerminated(TerminationType termType, long reward, long pathVolume, long
        amplifiedReward, Search searchState, boolean hasBeenExploredBefore) {
      // We don't do anything when a sample terminates
    }
}
