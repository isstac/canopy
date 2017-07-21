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

package edu.cmu.sv.isstac.sampling.exhaustive;

import edu.cmu.sv.isstac.sampling.JPFFactory;
import edu.cmu.sv.isstac.sampling.search.DepthSyncedDFSearch;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.DFSearch;
import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 */
public class JPFExhaustiveFactory implements JPFFactory {

  private final Class<? extends Search> searchClass;

  public JPFExhaustiveFactory() {
    searchClass = DepthSyncedDFSearch.class;
  }

  @Override
  public JPF buildInstance(Config jpfConfig) {
    //Substitute search object to use our sampler
    //There is no other way than using the string name of the class and rely
    //on the reflection in jpf-core...
    jpfConfig.setProperty("search.class", this.searchClass.getName());
    JPF jpf = new JPF(jpfConfig);
    return jpf;
  }
}
