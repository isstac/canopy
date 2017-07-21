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

package edu.cmu.sv.isstac.sampling.complexity;

import edu.cmu.sv.isstac.sampling.Options;

/**
 * @author Kasper Luckow
 */
public class Utils {

  public static final String COMPLEXITY_CONF_PRFX = Options.SAMPLING_CONF_PREFIX + ".complexity";

  public static final String ANALYSIS_TYPE = COMPLEXITY_CONF_PRFX + ".type";
  public static final String INPUT_RANGE = COMPLEXITY_CONF_PRFX + ".inputrange";
  public static final String INPUT_INCREMENT = COMPLEXITY_CONF_PRFX + ".inputincrement";
  public static final int INPUT_INCREMENT_DEFAULT = 1;

  public static final String OUTPUT_DIR = COMPLEXITY_CONF_PRFX + ".outputdir";
  public static final String DEFAULT_OUTPUT_DIR = COMPLEXITY_CONF_PRFX + "./";

  public static final String VISUALIZE = COMPLEXITY_CONF_PRFX + ".visualize";
  public static final boolean VISUALIZE_DEFAULT = true;

}
