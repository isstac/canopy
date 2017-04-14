/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.sv.isstac.sampling.sidechannel;

import edu.cmu.sv.isstac.sampling.Options;

/**
 * @author Kasper Luckow
 */
public class Utils {

  public static final String SIDECHANNEL_CONF_PRFX = Options.SAMPLING_CONF_PREFIX + ".sidechannel";
  public static final String CHANNEL_CAPACITY_CONF_PRFX = SIDECHANNEL_CONF_PRFX +
      ".channelcapacity";
  public static final String CHANNEL_CAPACITY_K_CONF_PRFX = CHANNEL_CAPACITY_CONF_PRFX + ".k";
  public static final String VISUALIZE = CHANNEL_CAPACITY_CONF_PRFX + ".visualize";
  public static final boolean VISUALIZE_DEFAULT = true;
}
