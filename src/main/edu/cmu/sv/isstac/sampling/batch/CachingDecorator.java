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

package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.JPFFactory;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.exploration.cache.StateCache;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class CachingDecorator implements Experiment {

  private final Experiment wrappee;
  private final Class<? extends StateCache> cacheCls;

  public CachingDecorator(Experiment wrappee, Class<? extends StateCache> cacheCls) {
    this.wrappee = wrappee;

    this.cacheCls = cacheCls;
  }
  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed)
      throws BatchProcessorException {
    config.setProperty(Options.STATE_CACHE, cacheCls.getName());

    return this.wrappee.createAnalysisStrategy(config, seed);
  }

  @Override
  public JPFFactory getJPFFactory() {
    return this.wrappee.getJPFFactory();
  }

  @Override
  public String getName() {
    String wrappeeName = this.wrappee.getName();
    String newName = wrappeeName;
    if(wrappeeName.endsWith("]")) {
      newName = wrappeeName.substring(0, wrappeeName.lastIndexOf("]"));
    }
    newName += ";stateCache=" + cacheCls.getSimpleName() + "]";
    return newName;
  }
}
