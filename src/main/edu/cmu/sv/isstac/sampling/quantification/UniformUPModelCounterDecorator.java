package edu.cmu.sv.isstac.sampling.quantification;

import org.antlr.runtime.RecognitionException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.util.PathUtil;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.JPFLogger;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.analysis.exceptions.EmptyDomainException;
import modelcounting.domain.Domain;
import modelcounting.domain.Problem;
import modelcounting.domain.ProblemSetting;
import modelcounting.domain.UsageProfile;
import modelcounting.domain.exceptions.InvalidUsageProfileException;
import modelcounting.utils.BigRational;
import sidechannel.util.SymbolicVariableCollector;

/**
 * @author Kasper Luckow
 * I really dislike that symbolic variables are represented by strings...
 */
public class UniformUPModelCounterDecorator implements SPFModelCounter {
  private static final Logger LOGGER = JPFLogger.getLogger(UniformUPModelCounterDecorator.class
      .getName());

  private final Config config;
  private Map<Set<String>, SPFModelCounter> modelCounters = new HashMap<>();

  public UniformUPModelCounterDecorator(Config config) {
    this.config = config;
  }

  private SPFModelCounter getModelCounterInstance(Set<String> symbolicVars) throws ModelCounterCreationException {
    SPFModelCounter modelCounter = modelCounters.get(symbolicVars);
    if(modelCounter == null) {
      ProblemSetting problemSetting = createUniformProblemSetting(symbolicVars);

      modelCounter = ModelCounterFactory.createModelCounterWithProblemSettings(this.config,
          problemSetting);
      this.modelCounters.put(symbolicVars, modelCounter);
    }
    return modelCounter;
  }

  private ProblemSetting createUniformProblemSetting(Set<String> symbolicVars) throws ModelCounterCreationException {
    //Assumption is that we **ONLY** have integer symbolic vars!
    int minInt = Integer.parseInt(this.config.getProperty("symbolic.min_int", String.valueOf(Integer
        .MIN_VALUE)));
    int maxInt = Integer.parseInt(this.config.getProperty("symbolic.max_int", String.valueOf(Integer
        .MAX_VALUE)));

    Domain.Builder domainBldr = new Domain.Builder();
    UsageProfile.Builder upBldr = new UsageProfile.Builder();

    // Build domain and usage profile
    Iterator<String> symVarIter = symbolicVars.iterator();
    StringBuilder usageScenarioBuilder = new StringBuilder();
    while(symVarIter.hasNext()) {
      String symVar = symVarIter.next();
      domainBldr.addVariable(symVar, minInt, maxInt);
      usageScenarioBuilder.append(symVar).append(" == ").append(symVar);
      if(symVarIter.hasNext()) {
        usageScenarioBuilder.append(" && ");
      }
    }

    // get domain
    Domain domain = domainBldr.build();

    // add scenario to usage profile
    UsageProfile up;
    try {
      upBldr.addScenario(usageScenarioBuilder.toString(), BigRational.ONE);
      // build usage profile
      up = upBldr.build();
    } catch (RecognitionException | InvalidUsageProfileException e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
      throw new ModelCounterCreationException(e);
    }
    // Finally, we are done..
    return new ProblemSetting(domain, up);
  }

  private Set<String> extractSymbolicVariables(PathCondition pc) {
    //TODO: SymbolicVariableCollector should be extracted/hoisted from its current package
    HashSet<String> vars = new HashSet<>();
    SymbolicVariableCollector symVarCollector = new SymbolicVariableCollector(vars);
    symVarCollector.collectVariables(pc);
    return vars;
  }

  @Override
  public BigRational analyzeSpfPC(PathCondition pc) throws AnalysisException {
    Set<String> symVars = extractSymbolicVariables(pc);
    SPFModelCounter modelCounter;
    try {
      modelCounter = getModelCounterInstance(symVars);
    } catch (ModelCounterCreationException e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
      throw new AnalysisException(e);
    }
    String pString = PathUtil.clean(pc);
    return modelCounter.analyzeSpfPC(pString);
  }

  @Override
  public BigRational countPointsOfPC(PathCondition pc) throws AnalysisException {
    Set<String> symVars = extractSymbolicVariables(pc);
    SPFModelCounter modelCounter;
    try {
      modelCounter = getModelCounterInstance(symVars);
    } catch (ModelCounterCreationException e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
      throw new AnalysisException(e);
    }
    String pString = PathUtil.clean(pc);
    return modelCounter.countPointsOfPC(pString);
  }

  @Override
  public BigRational analyzeSpfPC(String pc) throws AnalysisException {
    throw unsupported();
  }

  @Override
  public BigRational analyzeSetOfSpfPC(Set<String> pcs) throws AnalysisException {
    throw unsupported();
  }

  @Override
  public BigRational countPointsOfPC(String pc) throws AnalysisException {
    throw unsupported();
  }

  @Override
  public BigRational countPointsOfSetOfPCs(Set<String> pcs) throws AnalysisException {
    throw unsupported();
  }

  @Override
  public BigRational getDomainSize() throws AnalysisException {
    throw unsupported();
  }

  @Override
  public Set<Problem> excludeFromDomain(String pc) throws AnalysisException, EmptyDomainException {
    throw unsupported();
  }

  @Override
  public Set<Problem> excludeFromDomain(Set<String> pcs) throws AnalysisException, EmptyDomainException {
    throw unsupported();
  }

  @Override
  public void terminate() {
    for(SPFModelCounter mc : this.modelCounters.values()) {
      mc.terminate();
    }
  }

  private static final AnalysisException unsupported() throws AnalysisException {
    String msg = "Not supported because we cannot (easily) extract the symbolic variable names from a PC string---maybe in the " +
        "future if there is a need";
    LOGGER.severe(msg);
    return new AnalysisException(new UnsupportedOperationException(msg));
  }
}
