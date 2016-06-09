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
