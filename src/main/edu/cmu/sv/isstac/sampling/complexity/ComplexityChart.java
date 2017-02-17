/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.sv.isstac.sampling.complexity;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;

import javax.swing.*;

/**
 * @author Kasper Luckow
 */
public class ComplexityChart extends ApplicationFrame {
  private static final long serialVersionUID = 1760145418311574070L;

  private XYSeries complexitySeries;
  public ComplexityChart() {
    this(10);
  }

  public ComplexityChart(int maxBufferSize) {
    super("Sampling live results");

    ChartPanel timeSeriesPanel = new ChartPanel(createTimeSeries());
    JPanel seriesPanel = new JPanel();
    seriesPanel.setBackground(Color.white);
    seriesPanel.setLayout(new GridLayout(1,2));
    seriesPanel.add(timeSeriesPanel);
    setContentPane(seriesPanel);
  }

  public JFreeChart createTimeSeries() {
    XYSeriesCollection rewardDataset = new XYSeriesCollection();
    complexitySeries = new XYSeries("Reward");
    rewardDataset.addSeries(complexitySeries);

    final int SAMPLING_SERIES_ID = 0;
    //construct the plot
    XYPlot plot = new XYPlot();
    plot.setDataset(SAMPLING_SERIES_ID, rewardDataset);

    //customize the plot with renderers and axis
    XYLineAndShapeRenderer xyLineRenderer1 = new XYLineAndShapeRenderer();
    xyLineRenderer1.setSeriesShapesVisible(0, false);
    plot.setRenderer(0, xyLineRenderer1);
    // fill paint
    // for first series
    XYLineAndShapeRenderer xyLineRenderer2 = new XYLineAndShapeRenderer();
    xyLineRenderer2.setSeriesShapesVisible(0, false);
    xyLineRenderer2.setSeriesFillPaint(0, Color.BLUE);
    plot.setRangeAxis(SAMPLING_SERIES_ID, new NumberAxis("Reward"));
    plot.setDomainAxis(new NumberAxis("Input size"));

    //Map the data to the appropriate axis
    plot.mapDatasetToRangeAxis(SAMPLING_SERIES_ID, SAMPLING_SERIES_ID);

    //generate the chart
    JFreeChart timeSeriesChart = new JFreeChart("Live Sampling Results", getFont(), plot, true);
    timeSeriesChart.setBorderPaint(Color.white);
    return timeSeriesChart;
  }

  public void update(long inputSize, long reward) {
    this.complexitySeries.add(inputSize, reward);
  }
}
