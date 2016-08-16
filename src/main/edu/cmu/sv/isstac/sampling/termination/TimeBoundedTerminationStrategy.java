package edu.cmu.sv.isstac.sampling.termination;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import edu.cmu.sv.isstac.sampling.SamplingResult;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class TimeBoundedTerminationStrategy implements TerminationStrategy {

  private final Stopwatch stopwatch;
  private final TimeUnit timeUnit;
  private final long timeBound;
  
  public TimeBoundedTerminationStrategy(long timeBound, TimeUnit unit) {
    this.timeUnit = unit;
    this.timeBound = timeBound;
    this.stopwatch = Stopwatch.createStarted();
  }
  
  @Override
  public boolean terminate(VM vm, SamplingResult currentResult) {
    // Ugly that we rely on deprecated api because of messy guava dependencies
    return stopwatch.elapsed(timeUnit) >= timeBound;
  }
}
