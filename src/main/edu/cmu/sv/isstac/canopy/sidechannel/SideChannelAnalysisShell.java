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

package edu.cmu.sv.isstac.canopy.sidechannel;

import java.awt.*;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.canopy.AnalysisCreationException;
import edu.cmu.sv.isstac.canopy.JPFSamplerFactory;
import edu.cmu.sv.isstac.canopy.SamplingAnalysis;
import edu.cmu.sv.isstac.canopy.analysis.GenericLiveChart;
import edu.cmu.sv.isstac.canopy.montecarlo.MonteCarloStrategy;
import edu.cmu.sv.isstac.canopy.policies.SimulationPolicy;
import edu.cmu.sv.isstac.canopy.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.canopy.termination.TerminationStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class SideChannelAnalysisShell implements JPFShell {

  public static Logger logger = JPFLogger.getLogger(SideChannelAnalysisShell.class.getName());

  private final SamplingAnalysis samplingAnalysis;

  //ctor required for jpf shell
  public SideChannelAnalysisShell(Config config) throws AnalysisCreationException, ModelCounterCreationException {

    SimulationPolicy simulationPolicy = edu.cmu.sv.isstac.canopy.montecarlo.Utils
        .createSimulationPolicy(config);

    ChannelCapacityListener ccListener = new ChannelCapacityListener();

    SamplingAnalysis.Builder samplingAnalysisBuilder = new SamplingAnalysis.Builder();

    samplingAnalysisBuilder.addEventObserver(ccListener);

    boolean visualize = config.getBoolean(Utils.VISUALIZE,
        Utils.VISUALIZE_DEFAULT);

    if(visualize) {
      GenericLiveChart chart = new GenericLiveChart("Channel Capacity Live Chart",
          "Sample #", "Channel Capacity");
      //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      chart.setPreferredSize(new Dimension(1024, 768));
      chart.pack();
      chart.setVisible(true);
      samplingAnalysisBuilder.addEventObserver(new ChannelCapacityChartUpdater(chart, ccListener));
    }

    if(config.hasValue(Utils.CHANNEL_CAPACITY_K_CONF_PRFX)) {
      double k = config.getDouble(Utils.CHANNEL_CAPACITY_K_CONF_PRFX);
      TerminationStrategy kboundedTermination = new CapacityBoundedTerminationStrategy(k, ccListener);
      samplingAnalysisBuilder.addTerminationStrategy(kboundedTermination);
    }

    this.samplingAnalysis = samplingAnalysisBuilder.build(config, new MonteCarloStrategy
        (simulationPolicy), new JPFSamplerFactory());
  }

  @Override
  public void start(String[] args) {
    samplingAnalysis.run();
  }

}
