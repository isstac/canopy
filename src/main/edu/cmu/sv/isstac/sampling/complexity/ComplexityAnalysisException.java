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

package edu.cmu.sv.isstac.sampling.complexity;

/**
 * @author Kasper Luckow
 *
 */
class ComplexityAnalysisException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ComplexityAnalysisException(String msg) {
    super(msg);
  }

  public ComplexityAnalysisException(Throwable s) {
    super(s);
  }

  public ComplexityAnalysisException(String msg, Throwable s) {
    super(msg, s);
  }
}
