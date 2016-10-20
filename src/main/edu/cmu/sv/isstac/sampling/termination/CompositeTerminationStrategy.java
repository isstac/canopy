package edu.cmu.sv.isstac.sampling.termination;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class CompositeTerminationStrategy implements TerminationStrategy {
  private final Collection<TerminationStrategy> terminationStrategies;

  public CompositeTerminationStrategy(TerminationStrategy ...terminationStrategies) {
    this.terminationStrategies = new HashSet<>();
    for(TerminationStrategy strategy : terminationStrategies) {
      this.terminationStrategies.add(strategy);
    }
  }

  @Override
  public boolean terminate(VM vm, SamplingResult currentResult) {
    return terminationStrategies.stream()
        .anyMatch(terminationStrategy -> terminationStrategy
            .terminate(vm, currentResult));
  }
}