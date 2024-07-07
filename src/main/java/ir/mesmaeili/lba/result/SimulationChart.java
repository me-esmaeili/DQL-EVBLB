package ir.mesmaeili.lba.result;

import ir.mesmaeili.lba.statistic.SimulationStatisticResult;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.Map;

public class SimulationChart {
    public void generateCharts(SimulationStatisticResult result) {
        // Initialize series for the charts
        XYSeries queueSizeSeries = new XYSeries("Average Queue Size");
        XYSeries blockingRate = new XYSeries("Blocking Rate");
        XYSeries cpuUtilizationSeries = new XYSeries("Load Balancing Factor(LBF)");
        XYSeries responseTimeSeries = new XYSeries("Average Response Time");
        XYSeries makeSpanTimeSeries = new XYSeries("Average Makespan Time");
        XYSeries throughputTimeSeries = new XYSeries("Average Throughput Time");

        SimulationMetricResult metricResult = result.toMetricResult();

        // Calculate and add data to series
        addDataToSeries(metricResult.getAverageQueueSizeOverTimeMap(), queueSizeSeries);
        addDataToSeries(metricResult.getLBFOverTimeMap(), cpuUtilizationSeries);
        addDataToSeries(metricResult.getBlockingRateOverTimeMap(), blockingRate);
        addDataToSeries(metricResult.getAverageResponseTimeOverTimeMap(), responseTimeSeries);
        addDataToSeries(metricResult.getAverageMakespanTimeOverTimeMap(), makeSpanTimeSeries);
        addDataToSeries(metricResult.getThroughputOverTimeMap(), throughputTimeSeries);

        // Draw the charts
        drawChart(queueSizeSeries, "Average Queue Size Over Time", "Time", "Queue Size");
        drawChart(cpuUtilizationSeries, "Average LBF Over Time", "Time", "LBF");
        drawChart(blockingRate, "Blocking Rate Over Time", "Time", "Blocking Rate");
        drawChart(responseTimeSeries, "Average Response Time Over Time", "Time", "Response Time");
        drawChart(makeSpanTimeSeries, "Average Makespan Time Over Time", "Time", "Makespan Time");
        drawChart(throughputTimeSeries, "Average Throughput Time Over Time", "Time", "Throughput(#/s)");
    }

    private <T extends Number> void addDataToSeries(Map<Double, Double> dataMap, XYSeries series) {
        dataMap.forEach((time, data) -> series.add((double) time, data));
    }

    private void drawChart(XYSeries series, String chartTitle, String xAxisLabel, String yAxisLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                chartTitle,
                xAxisLabel,
                yAxisLabel,
                dataset
        );
        JFrame frame = new JFrame("Chart Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
    }
}
