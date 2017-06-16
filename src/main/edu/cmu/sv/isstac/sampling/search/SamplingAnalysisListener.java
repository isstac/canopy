package edu.cmu.sv.isstac.sampling.search;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import edu.cmu.sv.isstac.sampling.quantification.PathQuantifier;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.exploration.cache.StateCache;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import edu.cmu.sv.isstac.sampling.util.JPFUtil;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Transition;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class SamplingAnalysisListener extends PropertyListenerAdapter implements SamplingListener {

  private static final Logger logger = JPFLogger.getLogger(SamplingAnalysisListener.class.getName());

  private final RewardFunction rewardFunction;
  private final PathQuantifier pathQuantifier;

  private final TerminationStrategy terminationStrategy;

  private final ChoicesStrategy choicesStrategy;

  // The state cache is used for caching states such that we have minimal solver calls. This
  // comes at the expense of memory: the cache takes up size proportional to the size of the
  // symbolic execution tree
  private final StateCache stateCache;

  // Holds the largest rewards found (note: we assume a deterministic system!)
  // for succ, fail and grey. Maybe we only want to keep one of them?
  // In addition it holds various statistics about the exploration
  private SamplingResult result = new SamplingResult();

  // Observers are notified upon termination. We can put more fine grained
  // events if necessary, e.g. emit event after each sample.
  private Collection<AnalysisEventObserver> observers;

  // The analysis strategy to use, e.g., MCTS
  private final AnalysisStrategy analysisStrategy;

  public SamplingAnalysisListener(AnalysisStrategy analysisStrategy, RewardFunction rewardFunction,
                                  PathQuantifier pathQuantifier,
                                  TerminationStrategy terminationStrategy,
                                  ChoicesStrategy choicesStrategy,
                                  StateCache stateCache,
                                  Collection<AnalysisEventObserver> observers) {
    // Check input
    Preconditions.checkNotNull(analysisStrategy);
    Preconditions.checkNotNull(choicesStrategy);
    Preconditions.checkNotNull(stateCache);
    Preconditions.checkNotNull(rewardFunction);
    Preconditions.checkNotNull(pathQuantifier);
    Preconditions.checkNotNull(terminationStrategy);
    Preconditions.checkNotNull(observers);

    this.analysisStrategy = analysisStrategy;
    this.choicesStrategy = choicesStrategy;
    this.stateCache = stateCache;
    this.rewardFunction = rewardFunction;
    this.pathQuantifier = pathQuantifier;
    this.terminationStrategy = terminationStrategy;
    this.observers = observers;
  }

  @Override
  public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {

    // Get the eligible choices for this CG
    // based on the exploration strategy (e.g., pruning-based)
    ArrayList<Integer> eligibleChoices = choicesStrategy.getEligibleChoices(vm.getPath(), cg);

    // We use the analysis strategy to make the next choice
    this.analysisStrategy.makeStateChoice(vm, cg, eligibleChoices);
    if(cg instanceof PCChoiceGenerator) {
      // If the state cache contains the current state of the CG (i.e. the next choice to be
      // made) we can safely turn off the solver because it means that previously, there was a
      // path terminated with the state of this CG as a prefix, hence, by definition, the PC was
      // satisfiable and therefore we don't need to invoke the solver again
      // We will turn on the solver again as soon as we encounter a CG we have not seen before
      // according to the cache
      if(this.stateCache.isStateCached(vm)) {
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
    boolean hasBeenExplored = choicesStrategy.hasTerminatedPathBeenExplored(vm.getPath(),
        vm.getChoiceGenerator());

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
//    printPath(vm.getPath());
    stateCache.addState(vm);
  }

  private void printPath(gov.nasa.jpf.vm.Path path) {
    Iterator<Transition> iter = path.iterator();
    StringBuilder sb = new StringBuilder();
    while(iter.hasNext()) {
      ChoiceGenerator<?> cg = iter.next().getChoiceGenerator();
      int choice = JPFUtil.getCurrentChoiceOfCG(cg);
      sb.append(choice);
      if(iter.hasNext()) {
        sb.append(", ");
      }
    }
    System.out.println("State cache: " + sb.toString());
  }

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
  public void searchConstraintHit(Search search) {
    logger.severe("Search constraint hit.");
    pathTerminated(TerminationType.CONSTRAINT_HIT, search);
  }

  public Collection<AnalysisEventObserver> getEventObservers() {
    return this.observers;
  }
}
