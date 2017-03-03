package edu.cmu.sv.isstac.sampling.search;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import edu.cmu.sv.isstac.sampling.quantification.PathQuantifier;
import edu.cmu.sv.isstac.sampling.reward.DepthRewardFunction;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public final class SamplingAnalysisListener extends PropertyListenerAdapter implements SamplingListener {

  private static final Logger logger = JPFLogger.getLogger(SamplingAnalysisListener.class.getName());

  private final RewardFunction rewardFunction;
  private final PathQuantifier pathQuantifier;

  private final TerminationStrategy terminationStrategy;

  private final ChoicesStrategy choicesStrategy;

  // Holds the largest rewards found (note: we assume a deterministic system!)
  // for succ, fail and grey. Maybe we only want to keep one of them?
  // In addition it holds various statistics about the exploration
  private SamplingResult result = new SamplingResult();

  // Observers are notified upon termination. We can add more fine grained
  // events if necessary, e.g. emit event after each sample.
  private Collection<AnalysisEventObserver> observers;

  // The analysis strategy to use, e.g., MCTS
  private final AnalysisStrategy analysisStrategy;

  public SamplingAnalysisListener(AnalysisStrategy analysisStrategy, RewardFunction rewardFunction,
                                  PathQuantifier pathQuantifier,
                                  TerminationStrategy terminationStrategy,
                                  ChoicesStrategy choicesStrategy,
                                  Collection<AnalysisEventObserver> observers) {

    this.analysisStrategy = analysisStrategy;
    this.choicesStrategy = choicesStrategy;
    // Check input
    Preconditions.checkNotNull(rewardFunction);
    Preconditions.checkNotNull(pathQuantifier);
    Preconditions.checkNotNull(terminationStrategy);
    Preconditions.checkNotNull(observers);

    this.rewardFunction = rewardFunction;
    this.pathQuantifier = pathQuantifier;
    this.terminationStrategy = terminationStrategy;
    this.observers = observers;
  }

  @Override
  public final void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {

    // Get the eligible choices for this CG
    // based on the exploration strategy (e.g., pruning-based)
    ArrayList<Integer> eligibleChoices = choicesStrategy.getEligibleChoices(cg);

    // We use the analysis strategy to make the next choice
    this.analysisStrategy.makeStateChoice(vm, cg, eligibleChoices);
    if(cg instanceof PCChoiceGenerator) {
      // We shouldn't have to generate the path all the time, but unfortunately,
      // PCChoicegenerators do not have a unique id we can use. For each sample,
      // pcchoicegenerators are also replaced so we cannot check references.
      // The current way of caching could be very inefficient for deep paths, potentially defying
      // its purpose
      if (cache.contains(new Choice(new Path(cg),
          ((PCChoiceGenerator)cg).getNextChoice()))) {
        PathCondition.setReplay(true);
      } else {
        PathCondition.setReplay(false);
      }
    }
  }

  @Override
  public void searchStarted(Search search) {
    for(AnalysisEventObserver obs : this.observers) {
      obs.analysisStarted(search);
    }
  }
  @Override
  public void searchFinished(Search search) {
    // Notify observers with termination event
    for(AnalysisEventObserver obs : this.observers) {
      obs.analysisDone(result);
    }
  }

  private void pathTerminated(TerminationType termType, Search search) {
    VM vm = search.getVM();

    // First, let's check if we have seen this path before. We will inform the analysis strategy
    // and the event observers with this information
    Path terminatedPath = new Path(vm.getChoiceGenerator());
    boolean hasBeenExplored = choicesStrategy.hasTerminatedPathBeenExplored(terminatedPath);

    //Increment the number of samples we have performed
    result.incNumberOfSamples();

    // Compute reward based on reward function
    long reward = rewardFunction.computeReward(vm);

    // We compute the volume of the path here.
    // This can e.g. be based on model counting
    // The path volume is used in the backup phase,
    // i.e. the number of times asa node has been visited,
    // corresponds to the (additive) volume of the path(s)
    // with that node as origin
    long pathVolume = this.pathQuantifier.quantifyPath(vm);
    assert pathVolume > 0;

    long numberOfSamples = result.getNumberOfSamples();

    // The reward that will actually be propagated
    long amplifiedReward = reward * pathVolume;

    logger.fine("Sample #: " + numberOfSamples + ", reward: " + reward + ", path volume: " +
        pathVolume + ", amplified reward: " + amplifiedReward);

    // Check if the reward obtained is greater than
    // previously observed for this event (succ, fail, grey)
    // and update the best result accordingly

    SamplingResult.ResultContainer bestResult;
    switch(termType) {
      case SUCCESS:
        bestResult = result.getMaxSuccResult();
        break;
      case ERROR:
        bestResult = result.getMaxFailResult();
        break;
      case CONSTRAINT_HIT:
        bestResult = result.getMaxGreyResult();
        break;
      default:
        throw new SamplingException("Unknown result type " + termType);
    }


    // Notify observers with sample done event
    for(AnalysisEventObserver obs : this.observers) {
      obs.sampleDone(vm.getSearch(), numberOfSamples, reward, pathVolume,
          bestResult, hasBeenExplored);
    }

    if(reward > bestResult.getReward()) {
      bestResult.setReward(reward);
      bestResult.setSampleNumber(result.getNumberOfSamples());

      Path path = new Path(vm.getChoiceGenerator());
      bestResult.setPath(path);

      // Supposedly getPC defensively (deep) copies the current PC
      PathCondition pc = PathCondition.getPC(vm);
      bestResult.setPathCondition(pc);
    }

    this.analysisStrategy.pathTerminated(termType, reward, pathVolume,
        amplifiedReward, search, hasBeenExplored);

    // Check if we should terminate the search
    // based on the result obtained
    // searchFinished will be called later
    if(terminationStrategy.terminate(vm, this.result)) {
      vm.getSearch().terminate();
    }

    //Update cache
    PCChoiceGenerator[] pcs = vm.getChoiceGeneratorsOfType(PCChoiceGenerator.class);
    for(int i = pcs.length  - 1; i >= 0; i--) {
      PCChoiceGenerator cg = pcs[i];
      Choice c = new Choice(new Path(cg), cg.getNextChoice());
      if(!cache.contains(c)) {
        cache.add(c);
      } else {
        // This is a small trick and an optimization. Note that we are adding the CGs to the
        // cache starting from the *end* of the path. If the path
        // of the current cg is in the cache, then, by definition, we must have added
        // any prefix of the CG to the cache as well, so we can break here
        break;
      }
    }
  }

  private static class Choice {
    final Path path;
    final int choice;

    public Choice(Path path, int choice) {
      this.path = path;
      this.choice = choice;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (obj == this) {
        return true;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      return Objects.equal(path, ((Choice)obj).path) && Objects.equal(choice, ((Choice)obj)
          .choice);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.path, this.choice);
    }
  }

  Set<Choice> cache = new HashSet<>();

  @Override
  public void newSampleStarted(Search samplingSearch) {
    this.analysisStrategy.newSampleStarted(samplingSearch);
  }

  @Override
  public void stateAdvanced(Search search) {
    if(search.isEndState()) {
      logger.fine("Successful termination.");
      pathTerminated(TerminationType.SUCCESS, search);
    }
  }

  @Override
  public void exceptionThrown(VM vm, ThreadInfo currentThread, ElementInfo thrownException) {
    logger.fine("Property violation/exception thrown.");
    pathTerminated(TerminationType.ERROR, vm.getSearch());
  }

  @Override
  public void searchConstraintHit(Search search) {
    logger.fine("Search constraint hit.");
    pathTerminated(TerminationType.CONSTRAINT_HIT, search);
  }

}
