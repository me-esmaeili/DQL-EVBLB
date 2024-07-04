package ir.mesmaeili.drl.result;

import ir.mesmaeili.drl.config.SimulationState;
import ir.mesmaeili.drl.model.EdgeServer;
import ir.mesmaeili.drl.util.MetricUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationChart {
    public void generateCharts(SimulationState simulationState) {
        // Initialize series for the charts
        XYSeries queueSizeSeries = new XYSeries("Average Queue Size");
        XYSeries blockedTasksSeries = new XYSeries("Average Blocked Tasks");
        XYSeries cpuUtilizationSeries = new XYSeries("LBF");
        XYSeries responseTimeSeries = new XYSeries("Average Response Time");
        XYSeries makeSpanTimeSeries = new XYSeries("Average Makespan Time");

        // Initialize maps to collect metrics
        Map<Double, List<Integer>> averageQueueSizeMap = new HashMap<>();
        Map<Double, List<Double>> averageCpuUtilizationMap = new HashMap<>();
        Map<Double, List<Integer>> averageBlockedTasksMap = new HashMap<>();
        Map<Double, List<Double>> averageResponseTimeMap = new HashMap<>();
        Map<Double, List<Double>> averageMakespanTimeMap = new HashMap<>();

        // Collect metrics from each server
        simulationState.getEdgeServers().forEach(server ->
                server.getMetrics().forEach((time, metric) -> {
                    averageQueueSizeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getQueueSize());
                    averageCpuUtilizationMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getCpuUtilization());
                    averageBlockedTasksMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getBlockedTaskCount());
                    averageResponseTimeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(calculateAverageResponseTime(server));
                    averageMakespanTimeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(calculateAverageMakespanTime(server));
                })
        );

        // Calculate and add data to series
        addDataToSeries(averageQueueSizeMap, queueSizeSeries, simulationState.getTotalRound());
        addLBFDataToSeries(averageCpuUtilizationMap, cpuUtilizationSeries, simulationState.getTotalRound());
        addDataToSeries(averageBlockedTasksMap, blockedTasksSeries, simulationState.getTotalRound());
        addDataToSeries(averageResponseTimeMap, responseTimeSeries, simulationState.getTotalRound());
        addDataToSeries(averageMakespanTimeMap, makeSpanTimeSeries, simulationState.getTotalRound());

        // Draw the charts
        drawChart(queueSizeSeries, "Average Queue Size Over Time", "Time", "Queue Size");
        drawChart(cpuUtilizationSeries, "Average LBF Over Time", "Time", "LBF");
        drawChart(blockedTasksSeries, "Average Blocked Tasks Over Time", "Time", "Blocked Tasks");
        drawChart(responseTimeSeries, "Average Response Time Over Time", "Time", "Response Time");
        drawChart(makeSpanTimeSeries, "Average Makespan Time Over Time", "Time", "Makespan Time");
    }

    private double calculateAverageResponseTime(EdgeServer server) {
        return server.getTaskQueue().stream()
                .filter(task -> task.getProcessStartTime() > 0)
                .mapToDouble(task -> task.getProcessStartTime() - task.getArrivalTime())
                .average()
                .orElse(0);
    }

    private double calculateAverageMakespanTime(EdgeServer server) {
        return server.getTaskQueue().stream()
                .filter(task -> task.getFinishTime() > 0)
                .mapToDouble(task -> task.getFinishTime() - task.getArrivalTime())
                .average()
                .orElse(0);
    }

    private <T extends Number> void addDataToSeries(Map<Double, List<T>> dataMap, XYSeries series, int totalRounds) {
        dataMap.forEach((time, dataList) -> {
            double avg = dataList.stream().mapToDouble(Number::doubleValue).sum() / (totalRounds * dataList.size());
            series.add((double) time, avg);
        });
    }

    private void addLBFDataToSeries(Map<Double, List<Double>> cpuUtilizationMap, XYSeries series, int totalRounds) {
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
