package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.JPFFactory;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class BacktrackingDecorator implements Experiment {

  private final Experiment wrappee;
  private final boolean optimizeChoices;
  private final boolean useBacktracking;

  public BacktrackingDecorator(Experiment wrappee, boolean optimizeChoices, boolean
      useBacktracking) {
    this.wrappee = wrappee;
    this.optimizeChoices = optimizeChoices;
    this.useBacktracking = useBacktracking;
  }
  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed)
      throws BatchProcessorException {
    config.setProperty("symbolic.optimizechoices", Boolean.toString(optimizeChoices));
    config.setProperty("canopy.backtrackingsearch",
        Boolean.toString(useBacktracking));

    return this.wrappee.createAnalysisStrategy(config, seed);
  }

  @Override
  public JPFFactory getJPFFactory() {
    return this.wrappee.getJPFFactory();
  }

  @Override
  public String getName() {
    String wrappeeName = this.wrappee.getName();
    String newName = wrappeeName;
    if(wrappeeName.endsWith("]")) {
      newName = wrappeeName.substring(0, wrappeeName.lastIndexOf("]"));
    }
    newName += ";optchoices=" + this.optimizeChoices + ";backtrack=" + this.useBacktracking + "]";
    return newName;
  }
}
