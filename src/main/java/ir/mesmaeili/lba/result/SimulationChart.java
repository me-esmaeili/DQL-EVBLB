package ir.mesmaeili.lba.result;

import ir.mesmaeili.lba.statistic.SimulationStatisticResult;
import ir.mesmaeili.lba.util.MetricUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class SimulationChart {
    public void generateCharts(SimulationStatisticResult result) {
        // Initialize series for the charts
        XYSeries queueSizeSeries = new XYSeries("Average Queue Size");
        XYSeries blockedTasksSeries = new XYSeries("Average Blocked Tasks");
        XYSeries cpuUtilizationSeries = new XYSeries("Load Balancing Factor(LBF)");
        XYSeries responseTimeSeries = new XYSeries("Average Response Time");
        XYSeries makeSpanTimeSeries = new XYSeries("Average Makespan Time");

        SimulationMetricResult metricResult = result.toMetricResult();

        // Calculate and add data to series
        addDataToSeries(metricResult.getAverageQueueSizeMap(), queueSizeSeries, result.getTotalRounds());
        addLBFDataToSeries(metricResult.getAverageCpuUtilizationMap(), cpuUtilizationSeries, result.getTotalRounds());
        addDataToSeries(metricResult.getAverageBlockedTasksMap(), blockedTasksSeries, result.getTotalRounds());
        addDataToSeries(metricResult.getAverageResponseTimeMap(), responseTimeSeries, result.getTotalRounds());
        addDataToSeries(metricResult.getAverageMakespanTimeMap(), makeSpanTimeSeries, result.getTotalRounds());

        // Draw the charts
        drawChart(queueSizeSeries, "Average Queue Size Over Time", "Time", "Queue Size");
        drawChart(cpuUtilizationSeries, "Average LBF Over Time", "Time", "LBF");
        drawChart(blockedTasksSeries, "Average Blocked Tasks Over Time", "Time", "Blocked Tasks");
        drawChart(responseTimeSeries, "Average Response Time Over Time", "Time", "Response Time");
        drawChart(makeSpanTimeSeries, "Average Makespan Time Over Time", "Time", "Makespan Time");
    }

    private <T extends Number> void addDataToSeries(Map<Double, List<T>> dataMap, XYSeries series, int totalRounds) {
        dataMap.forEach((time, dataList) -> {
            double avg = dataList.stream().mapToDouble(Number::doubleValue).sum() / (totalRounds * dataList.size());
            series.add((double) time, avg);
        });
    }

    public static void addLBFDataToSeries(Map<Double, List<Double>> cpuUtilizationMap, XYSeries series, int totalRounds) {
        cpuUtilizationMap.forEach((time, dataList) -> {
            double avg = MetricUtil.calculateLBF(dataList, time);
            series.add((double) time, avg);
        });
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
