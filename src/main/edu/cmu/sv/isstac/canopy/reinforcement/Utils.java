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

package edu.cmu.sv.isstac.canopy.reinforcement;

import edu.cmu.sv.isstac.canopy.Options;

/**
 * @author Kasper Luckow
 */
public class Utils {

  public static final String RL_CONF_PREFIX = Options.SAMPLING_CONF_PREFIX + ".rl";

  public static final String SAMPLES_PER_OPTIMIZATION = RL_CONF_PREFIX + ".samplesperoptimization";

  public static final String EPSILON = RL_CONF_PREFIX + ".epsilon";
  public static final String HISTORY = RL_CONF_PREFIX + ".history";

  public static final String USE_MODELCOUNTING = RL_CONF_PREFIX +
      ".modelcounting";

  // Some defaults
  public static final int DEFAULT_SAMPLES_PER_OPTIMIZATION = 100;
  public static final double DEFAULT_EPSILON = 0.5;
  public static final double DEFAULT_HISTORY = 0.5;

  public static final boolean DEFAULT_USE_MODELCOUNTING = false;
}
