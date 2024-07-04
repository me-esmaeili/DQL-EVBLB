package ir.mesmaeili.drl.result;

import ir.mesmaeili.drl.config.SimulationState;
import ir.mesmaeili.drl.model.EdgeServer;
import ir.mesmaeili.drl.model.ServerPerformanceMetric;
import ir.mesmaeili.drl.model.Task;
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
        XYSeries queueSizeSeries = new XYSeries("Average Queue Size");
        XYSeries blockedTasksSeries = new XYSeries("Average Blocked Tasks");
        XYSeries cpuUtilizationSeries = new XYSeries("Average LBF");
        XYSeries responseTimeSeries = new XYSeries("Average Response Time");

        Map<Double, List<Integer>> averageQueueSizeMap = new HashMap<>();
        Map<Double, List<Double>> averageCpuUtilizationMap = new HashMap<>();
        Map<Double, List<Integer>> averageBlockedTasksMap = new HashMap<>();
        Map<Double, List<Double>> averageResponseTimeMap = new HashMap<>();

        for (int i = 0; i < simulationState.getTotalRound(); i++) {
            for (EdgeServer server : simulationState.getEdgeServers()) {
                int taskCount = 0;
                for (Map.Entry<Double, ServerPerformanceMetric> entry : server.getMetrics().entrySet()) {
                    Double time = entry.getKey();
                    ServerPerformanceMetric metric = entry.getValue();
                    averageQueueSizeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getQueueSize());
                    averageCpuUtilizationMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getCpuUtilization());
                    averageBlockedTasksMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getBlockedTaskCount());

                    double totalResponseTime = 0;
                    for (Task task : server.getTaskQueue()) {
                        if (task.getFinishTime() > 0) {
                            totalResponseTime += task.getFinishTime() - task.getArrivalTime();
                            taskCount++;
                        }
                    }
                    double averageResponseTime = taskCount > 0 ? totalResponseTime / taskCount : 0;
                    averageResponseTimeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(averageResponseTime);
                }
            }
        }

        int totalCount = simulationState.getTotalRound() * simulationState.getEdgeServers().size();
        for (Map.Entry<Double, List<Integer>> queueMap : averageQueueSizeMap.entrySet()) {
            double avg = queueMap.getValue().stream().mapToDouble(d -> d).sum() / totalCount;
            queueSizeSeries.add((double) queueMap.getKey(), avg);
        }

        for (Map.Entry<Double, List<Integer>> queueMap : averageBlockedTasksMap.entrySet()) {
            double avg = queueMap.getValue().stream().mapToDouble(d -> d).sum() / totalCount;
            blockedTasksSeries.add((double) queueMap.getKey(), avg);
        }

        for (Map.Entry<Double, List<Double>> queueMap : averageCpuUtilizationMap.entrySet()) {
            double avg = queueMap.getValue().stream().mapToDouble(d -> d).sum() / totalCount;
            cpuUtilizationSeries.add((double) queueMap.getKey(), avg);
        }

        for (Map.Entry<Double, List<Double>> queueMap : averageResponseTimeMap.entrySet()) {
            double avg = queueMap.getValue().stream().mapToDouble(d -> d).sum() / totalCount;
            responseTimeSeries.add((double) queueMap.getKey(), avg);
        }

        drawChart(queueSizeSeries, "Average Queue Size Over Time", "Time", "Queue Size");
        drawChart(blockedTasksSeries, "Average Blocked Tasks Over Time", "Time", "Blocked Tasks");
        drawChart(cpuUtilizationSeries, "Average LBF Over Time", "Time", "LBF");
        drawChart(responseTimeSeries, "Average Response Time Over Time", "Time", "Response Time");
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
