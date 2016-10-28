package edu.cmu.sv.isstac.sampling.exhaustive;

import java.util.ArrayList;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.search.TerminationType;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class ExhaustiveShell implements JPFShell {

  private final SamplingAnalysis samplingAnalysis;

  //ctor required for jpf shell
  public ExhaustiveShell(Config config) throws AnalysisCreationException, ModelCounterCreationException {

    // Create dummy strategy
    AnalysisStrategy strategy = new ExhaustiveStrategy();

    SamplingAnalysis.Builder analysisBuilder =
        new SamplingAnalysis.Builder();
    this.samplingAnalysis = analysisBuilder.build(config, strategy, new JPFExhaustiveFactory());
  }

  @Override
  public void start(String[] args) {
    this.samplingAnalysis.run();
  }
}
