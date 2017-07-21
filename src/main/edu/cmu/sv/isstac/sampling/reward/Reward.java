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

package edu.cmu.sv.isstac.sampling.reward;

/**
 * @author Kasper Luckow
 *
 */
public class Reward {
  private long succ, grey, fail;
  public Reward() {
    succ = grey = fail = 0;
  }
  
  public long getSucc() {
    return this.succ;
  }
  
  public void incrementSucc(long reward) {
    this.succ += reward;
  }
  
  public void setSucc(long reward) {
    this.succ = reward;
  }
  
  public long getFail() {
    return this.fail;
  }
  
  public void incrementFail(long reward) {
    this.fail += reward;
  }
  
  public void setFail(long reward) {
    this.fail = reward;
  }
  
  public long getGrey() {
    return this.grey;
  }
  
  public void incrementGrey(long reward) {
    this.grey += reward;
  }
  
  public void setGrey(long reward) {
    this.grey = reward;
  }
}
