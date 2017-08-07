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

package edu.cmu.sv.isstac.canopy.analysis;

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
public class GenericLiveChart extends ApplicationFrame {
  private static final long serialVersionUID = 1760145418311574070L;
  private final String chartTitle;
  private final String xAxis;
  private final String yAxis;

  private XYSeries xySeries;

  public GenericLiveChart(String chartTitle, String xAxis, String yAxis) {
    super(chartTitle);
    this.chartTitle = chartTitle;
    this.xAxis = xAxis;
    this.yAxis = yAxis;

    ChartPanel timeSeriesPanel = new ChartPanel(createTimeSeries());
    JPanel seriesPanel = new JPanel();
    seriesPanel.setBackground(Color.white);
    seriesPanel.setLayout(new GridLayout(1,2));
    seriesPanel.add(timeSeriesPanel);
    setContentPane(seriesPanel);
  }

  public JFreeChart createTimeSeries() {
    XYSeriesCollection ccDataset = new XYSeriesCollection();
    xySeries = new XYSeries(yAxis);
    ccDataset.addSeries(xySeries);

    final int SAMPLING_SERIES_ID = 0;
    //construct the plot
    XYPlot plot = new XYPlot();
    plot.setDataset(SAMPLING_SERIES_ID, ccDataset);

    //customize the plot with renderers and axis
    XYLineAndShapeRenderer xyLineRenderer1 = new XYLineAndShapeRenderer();
    xyLineRenderer1.setSeriesShapesVisible(SAMPLING_SERIES_ID, true);
    plot.setRenderer(SAMPLING_SERIES_ID, xyLineRenderer1);
    // fill paint
    // for first series
    XYLineAndShapeRenderer xyLineRenderer2 = new XYLineAndShapeRenderer();
    xyLineRenderer2.setSeriesShapesVisible(0, false);
    xyLineRenderer2.setSeriesFillPaint(0, Color.BLUE);
    plot.setRangeAxis(SAMPLING_SERIES_ID, new NumberAxis(yAxis));
    plot.setDomainAxis(new NumberAxis(xAxis));

    //Map the data to the appropriate axis
    plot.mapDatasetToRangeAxis(SAMPLING_SERIES_ID, SAMPLING_SERIES_ID);

    //generate the chart
    JFreeChart timeSeriesChart = new JFreeChart(chartTitle, getFont(), plot, true);
    timeSeriesChart.setBorderPaint(Color.white);
    return timeSeriesChart;
  }

  public void update(long x, double y) {
    this.xySeries.add(x, y);
  }
}
