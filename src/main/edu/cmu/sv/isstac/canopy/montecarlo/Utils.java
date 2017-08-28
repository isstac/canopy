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

import edu.cmu.sv.isstac.canopy.Options;
import edu.cmu.sv.isstac.canopy.policies.CountWeightedSimulationPolicy;
import edu.cmu.sv.isstac.canopy.policies.SimulationPolicy;
import edu.cmu.sv.isstac.canopy.policies.UniformSimulationPolicy;
import edu.cmu.sv.isstac.canopy.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.canopy.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.canopy.quantification.SPFModelCounter;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class Utils {

  public static final String MC_CONF_PRFX = Options.SAMPLING_CONF_PREFIX + ".montecarlo";

  public static final String SIMULATION_POLICY = MC_CONF_PRFX + ".simulationpol";

  public static final String USE_MODELCOUNT_WEIGHTED_SIMULATION = MC_CONF_PRFX +
      ".weightedsampling";
  public static final boolean DEFAULT_USE_MODELCOUNT_WEIGHTED_SIMULATION = false;


  public static SimulationPolicy createSimulationPolicy(Config conf)
      throws ModelCounterCreationException {
    if(conf.hasValue(SIMULATION_POLICY)) {
      return conf.getInstance(SIMULATION_POLICY, SimulationPolicy.class);
    }

    long seed = Options.getSeed(conf);
    if(conf.getBoolean(USE_MODELCOUNT_WEIGHTED_SIMULATION,
        DEFAULT_USE_MODELCOUNT_WEIGHTED_SIMULATION)) {
      SPFModelCounter modelCounter = ModelCounterFactory.getInstance(conf);
      return new CountWeightedSimulationPolicy(modelCounter, seed);
    }
    else {
      return new UniformSimulationPolicy(seed);
    }
  }

}
