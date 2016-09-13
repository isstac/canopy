package edu.cmu.sv.isstac.sampling.analysis;

import com.google.common.base.Stopwatch;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

/**
 * @author Kasper Luckow
 * Either remove this class or make a factory that creates charts base don whether model counting
 * is used or not
 */
@Deprecated
public class LiveTrackerChart extends ApplicationFrame {
  
  private static final long serialVersionUID = 1760145418311574070L;
  
  private SimpleHistogramDataset histogram;
  private XYSeries samplingSeries;
  
  private ValueMarker avgMarker = new ValueMarker(0);
  private ValueMarker maxMarker = new ValueMarker(0);
  
  private static final String maxRewardTxt = "Max reward observed: ";
  private final Label maxRewardTxtLabel = new Label(maxRewardTxt);
  private static final String maxRewardSampleTxt = "Sample # for max reward: ";
  private final Label maxRewardSampleTxtLabel = new Label(maxRewardSampleTxt);
  private Stopwatch stopwatch;
  private static final String maxRewardWallClockTxt = "Wall clock time for max reward: ";
  private final Label maxRewardWallClockTxtLabel = new Label(maxRewardWallClockTxt);

  private static final DecimalFormat avgFormat = new DecimalFormat("#.##");
  private static final String avgRewardSampleTxt = "Average reward: ";
  private final Label avgRewardTxtLabel = new Label(avgRewardSampleTxt);
  
  private final int maxBufferSize;
  private int bufferSize;
  private long xBuf[];
  private long yBuf[];
  
  private long maxReward = 0;
  private double rollingAvg = 0;
  
  private Set<Long> binsUsed = new HashSet<>();
  
  public LiveTrackerChart() {
    this(10);
  }
  
  public LiveTrackerChart(int maxBufferSize) {
    super("Sampling live results");
    this.maxBufferSize = maxBufferSize;
    this.xBuf = new long[maxBufferSize];
    this.yBuf = new long[maxBufferSize];
    this.bufferSize = 0;
    
    ChartPanel timeSeriesPanel = new ChartPanel(createTimeSeries());
    ChartPanel histogramPanel = new ChartPanel(createHistogram());
    
    JPanel container = new JPanel();
    JPanel seriesPanel = new JPanel();
    JPanel labelPanel = new JPanel();
    labelPanel.setBackground(Color.white);
    labelPanel.setLayout(new GridLayout(3,1));
    
    seriesPanel.setLayout(new GridLayout(1,2));
    container.setLayout(new BorderLayout());
    
    labelPanel.add(maxRewardTxtLabel);
    labelPanel.add(maxRewardSampleTxtLabel);
    labelPanel.add(maxRewardWallClockTxtLabel);
    labelPanel.add(avgRewardTxtLabel);
    
    seriesPanel.add(timeSeriesPanel);
    seriesPanel.add(histogramPanel);
    container.add(labelPanel, BorderLayout.SOUTH);
    container.add(seriesPanel, BorderLayout.CENTER);
    setContentPane(container);

    stopwatch = Stopwatch.createStarted();
  }
  
  public JFreeChart createTimeSeries() {
    XYSeriesCollection dataset = new XYSeriesCollection();
    samplingSeries = new XYSeries("Reward");
    dataset.addSeries(samplingSeries);
    JFreeChart timeSeriesChart = ChartFactory.createXYLineChart("Live Sampling Results",
        "Number of Samples",
        "Reward",
        dataset,
        PlotOrientation.VERTICAL, 
        true, 
        true, 
        false);

    timeSeriesChart.setBackgroundPaint(Color.WHITE);
    avgMarker.setPaint(Color.green);
    maxMarker.setPaint(Color.blue);
    
    timeSeriesChart.getXYPlot().addRangeMarker(avgMarker);
    timeSeriesChart.getXYPlot().addRangeMarker(maxMarker);
    return timeSeriesChart;
  }
  
  public JFreeChart createHistogram() {
    histogram = new SimpleHistogramDataset("Rewards");
    histogram.setAdjustForBinSize(true);
    JFreeChart histogramChart = ChartFactory.createHistogram("Live Reward Frequency",
        "Reward", 
        "Frequency", 
        histogram,
        PlotOrientation.VERTICAL, 
        true, 
        true, 
        false);
    
    return histogramChart;
  }
  
  public void update(long x, long y) {
    if(bufferSize >= maxBufferSize) {
      updateCharts(xBuf, yBuf);
      bufferSize = 0;
    } else {
      xBuf[bufferSize] = x;
      yBuf[bufferSize++] = y;
    }
  }

  //TODO: buffering is not working atm -- seems a bit complicated
  private void updateCharts(long[] x, long[] y) {
    for(int i = 0; i < x.length; i++) {
      long samplesNum = x[i];
      long reward = y[i];
      
      //Crazy we have to do this...
      if(!binsUsed.contains(reward)) {
        histogram.addBin(new SimpleHistogramBin(reward, reward+1, true, false));
        binsUsed.add(reward);
      }
      histogram.addObservation(reward);
      
      if(reward > maxReward) {
        this.maxRewardTxtLabel.setText(maxRewardTxt + reward);
        this.maxRewardSampleTxtLabel.setText(maxRewardSampleTxt + samplesNum);
        this.maxRewardWallClockTxtLabel.setText(maxRewardWallClockTxt + this.stopwatch.elapsed
            (TimeUnit.SECONDS) + "s");
        maxReward = reward;
      }
      this.samplingSeries.add(samplesNum, reward);
      rollingAvg -= rollingAvg/samplesNum;
      rollingAvg += reward/(double)samplesNum;
    }
    avgRewardTxtLabel.setText(avgRewardSampleTxt + avgFormat.format(rollingAvg)); 
    maxMarker.setValue(maxReward);
    avgMarker.setValue(rollingAvg);
  }
}
