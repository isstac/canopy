package edu.cmu.sv.isstac.sampling.reward;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class DepthRewardFunction implements RewardFunction {

  @Override
  public long computeReward(VM vm) {
    int depth = vm.getSearch().getDepth();


    return depth;
  }
}
