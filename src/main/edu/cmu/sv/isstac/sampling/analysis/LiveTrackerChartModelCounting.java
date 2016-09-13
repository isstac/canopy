package edu.cmu.sv.isstac.sampling.analysis;

import com.google.common.base.Stopwatch;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
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
 *
 */
public class LiveTrackerChartModelCounting extends ApplicationFrame {

  private static final long serialVersionUID = 1760145418311574070L;

  private SimpleHistogramDataset histogram;
  private XYSeries samplingSeries;
  private XYSeries volumeSeries;

  private ValueMarker avgMarker = new ValueMarker(0);
  private ValueMarker maxMarker = new ValueMarker(0);

  private static final String maxRewardTxt = "Max reward observed: ";
  private final Label maxRewardTxtLabel = new Label(maxRewardTxt);
  private static final String maxRewardSampleTxt = "Sample # for max reward: ";
  private final Label maxRewardSampleTxtLabel = new Label(maxRewardSampleTxt);
  private Stopwatch maxRewardStopWatch;
  private static final String maxRewardWallClockTxt = "Wall clock time for max reward: ";
  private final Label maxRewardWallClockTxtLabel = new Label(maxRewardWallClockTxt);

  private static final String throughputTxt = "Throughput [#samples/s]: ";
  private final Label throughputTxtLabel = new Label(throughputTxt);
  private static final String avgThroughputTxt = "Avg. throughput [#samples/s]: ";
  private final Label avgThroughputTxtLabel = new Label(avgThroughputTxt);

  private static final DecimalFormat avgFormat = new DecimalFormat("#.##");
  private static final String avgRewardSampleTxt = "Avg. reward: ";
  private final Label avgRewardTxtLabel = new Label(avgRewardSampleTxt);

  private final int maxBufferSize;
  private int bufferSize;
  private long xBuf[];
  private long yBuf[];
  private long pathVolumeBuf[];

  private long maxReward = 0;
  private double rollingAvg = 0;

  private Set<Long> binsUsed = new HashSet<>();


  // For throughput calculation
  private Stopwatch stopwatch;
  private double rollingThroughputAvg = 0.0;
  private long throughputSamplesNum = 0;

  public LiveTrackerChartModelCounting() {
    this(10);
  }

  public LiveTrackerChartModelCounting(int maxBufferSize) {
    super("Sampling live results");
    this.maxBufferSize = maxBufferSize;
    this.xBuf = new long[maxBufferSize];
    this.yBuf = new long[maxBufferSize];
    this.pathVolumeBuf = new long[maxBufferSize];
    this.bufferSize = 0;
    
    ChartPanel timeSeriesPanel = new ChartPanel(createTimeSeries());
    ChartPanel histogramPanel = new ChartPanel(createHistogram());
    
    JPanel container = new JPanel();
    JPanel seriesPanel = new JPanel();
    JPanel labelPanel = new JPanel();
    labelPanel.setBackground(Color.white);
    labelPanel.setLayout(new GridLayout(6,1));
    
    seriesPanel.setLayout(new GridLayout(1,2));
    container.setLayout(new BorderLayout());
    
    labelPanel.add(maxRewardTxtLabel);
    labelPanel.add(maxRewardSampleTxtLabel);
    labelPanel.add(maxRewardWallClockTxtLabel);
    labelPanel.add(avgRewardTxtLabel);
    labelPanel.add(throughputTxtLabel);
    labelPanel.add(avgThroughputTxtLabel);
    
    seriesPanel.add(timeSeriesPanel);
    seriesPanel.add(histogramPanel);
    container.add(labelPanel, BorderLayout.SOUTH);
    container.add(seriesPanel, BorderLayout.CENTER);
    setContentPane(container);

    this.stopwatch = Stopwatch.createStarted();
    this.maxRewardStopWatch = Stopwatch.createStarted();
  }
  
  public JFreeChart createTimeSeries() {
    XYSeriesCollection rewardDataset = new XYSeriesCollection();
    samplingSeries = new XYSeries("Reward");
    rewardDataset.addSeries(samplingSeries);

    XYSeriesCollection pathVolumeDataset = new XYSeriesCollection();
    volumeSeries = new XYSeries("Path volume");
    pathVolumeDataset.addSeries(volumeSeries);

    final int SAMPLING_SERIES_ID = 0;
    final int VOLUME_SERIES_ID = 1;
    //construct the plot
    XYPlot plot = new XYPlot();
    plot.setDataset(SAMPLING_SERIES_ID, rewardDataset);
    plot.setDataset(1, pathVolumeDataset);

    //customize the plot with renderers and axis
    XYLineAndShapeRenderer xyLineRenderer1 = new XYLineAndShapeRenderer();
    xyLineRenderer1.setSeriesShapesVisible(0, false);
    plot.setRenderer(0, xyLineRenderer1);
    // fill paint
    // for first series
    XYLineAndShapeRenderer xyLineRenderer2 = new XYLineAndShapeRenderer();
    xyLineRenderer2.setSeriesShapesVisible(0, false);
    xyLineRenderer2.setSeriesFillPaint(0, Color.BLUE);
    plot.setRenderer(VOLUME_SERIES_ID, xyLineRenderer2);
    plot.setRangeAxis(SAMPLING_SERIES_ID, new NumberAxis("Reward"));
    plot.setRangeAxis(VOLUME_SERIES_ID, new NumberAxis("Volume"));
    plot.setDomainAxis(new NumberAxis("Number of Samples"));

    //Map the data to the appropriate axis
    plot.mapDatasetToRangeAxis(SAMPLING_SERIES_ID, SAMPLING_SERIES_ID);
    plot.mapDatasetToRangeAxis(VOLUME_SERIES_ID, VOLUME_SERIES_ID);

    //generate the chart
    JFreeChart timeSeriesChart = new JFreeChart("Live Sampling Results", getFont(), plot, true);
    timeSeriesChart.setBorderPaint(Color.white);
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
  
  public void update(long x, long reward, long pathVolume) {
    if(bufferSize >= maxBufferSize) {
      updateCharts(xBuf, yBuf, pathVolumeBuf);
      long elapsedTime = this.stopwatch.elapsed(TimeUnit.MILLISECONDS);
      this.stopwatch.reset().start();

      //Compute throughput
      double throughput = bufferSize / ((double)elapsedTime / 1000);
      throughputSamplesNum++;
      rollingThroughputAvg -= rollingThroughputAvg / throughputSamplesNum;
      rollingThroughputAvg += throughput / (double)throughputSamplesNum;

      throughputTxtLabel.setText(throughputTxt + " " + avgFormat.format(throughput));
      avgThroughputTxtLabel.setText(avgThroughputTxt + " " + avgFormat.format
          (rollingThroughputAvg));
      bufferSize = 0;
    } else {
      xBuf[bufferSize] = x;
      yBuf[bufferSize] = reward;
      pathVolumeBuf[bufferSize] = pathVolume;
      bufferSize++;
    }
  }

  //TODO: buffering is not working atm -- seems a bit complicated
  private void updateCharts(long[] x, long[] y, long[] pathVolume) {
    for(int i = 0; i < x.length; i++) {
      long samplesNum = x[i];
      long reward = y[i];
      long volume = pathVolume[i];
      
      //Crazy we have to do this...
      if(!binsUsed.contains(reward)) {
        histogram.addBin(new SimpleHistogramBin(reward, reward+1, true, false));
        binsUsed.add(reward);
      }
      histogram.addObservation(reward);
      
      if(reward > maxReward) {
        this.maxRewardTxtLabel.setText(maxRewardTxt + reward);
        this.maxRewardSampleTxtLabel.setText(maxRewardSampleTxt + samplesNum);
        this.maxRewardWallClockTxtLabel.setText(maxRewardWallClockTxt + this.maxRewardStopWatch
            .elapsed(TimeUnit.SECONDS) + "s");
        maxReward = reward;
      }
      this.samplingSeries.add(samplesNum, reward);
      this.volumeSeries.add(samplesNum, volume);
      rollingAvg -= rollingAvg/samplesNum;
      rollingAvg += reward/(double)samplesNum;
    }
    avgRewardTxtLabel.setText(avgRewardSampleTxt + avgFormat.format(rollingAvg)); 
    maxMarker.setValue(maxReward);
    avgMarker.setValue(rollingAvg);
  }
}
