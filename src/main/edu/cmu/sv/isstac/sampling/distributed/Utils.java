/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
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

package edu.cmu.sv.isstac.sampling.distributed;

import edu.cmu.sv.isstac.sampling.Options;

/**
 * @author Kasper Luckow
 */
public class Utils {

  public static final String DISTRIBUTED_CONF_PRFX = Options.SAMPLING_CONF_PREFIX + ".dist";

  public static final String ANALYSIS_TYPE = DISTRIBUTED_CONF_PRFX + ".type";

  public static final String MASTER_CONF_PRFX = DISTRIBUTED_CONF_PRFX + ".master";
  public static final String MASTER_PORT_CONF = MASTER_CONF_PRFX + ".port";
  public static final int DEFAULT_MASTER_PORT = 1099;

  public static final String SERVICE_NAME = RMIMaster.class.getSimpleName();

  public static final String CLIENTS_NUM_CONF = MASTER_CONF_PRFX + ".clientsnum";
  public static final String DEFAULT_HOSTNAME = "127.0.0.1";
}
