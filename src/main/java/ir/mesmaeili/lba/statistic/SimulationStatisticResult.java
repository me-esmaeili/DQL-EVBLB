package ir.mesmaeili.lba.statistic;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import ir.mesmaeili.lba.result.SimulationMetricResult;
import ir.mesmaeili.lba.util.CSVUtils;
import ir.mesmaeili.lba.util.MetricUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class SimulationStatisticResult {
    private int totalRounds;
    private long startTime;
    private double DeltaT;
    private Queue<Task> tasks = new LinkedList<>();
    private List<EdgeServer> edgeServers = new ArrayList<>();

    public SimulationStatisticResult(long startTime, SimulationConfig config) {
        this.startTime = startTime;
        this.DeltaT = config.getDeltaT();
    }

    public void addTasks(Queue<Task> task) {
        tasks.addAll(task);
    }

    public void addServer(EdgeServer edgeServer) {
        edgeServers.add(edgeServer);
    }

    public SimulationMetricResult toMetricResult() {
        // Initialize maps to collect metrics
        Map<Double, List<Integer>> averageQueueSizeMap = new HashMap<>();
        Map<Double, List<Double>> averageCpuUtilizationMap = new HashMap<>();
        Map<Double, List<Integer>> averageBlockedTasksMap = new HashMap<>();
        Map<Double, List<Double>> averageResponseTimeMap = new HashMap<>();
        Map<Double, List<Double>> averageMakespanTimeMap = new HashMap<>();

        // Collect metrics from each server
        this.getEdgeServers().forEach(server ->
                server.getMetrics().forEach((time, metric) -> {
                    averageQueueSizeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getQueueSize());
                    averageCpuUtilizationMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getCpuUtilization());
                    averageBlockedTasksMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getBlockedTaskCount());
                    averageResponseTimeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(MetricUtil.calculateAverageResponseTime(time, server));
                    averageMakespanTimeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(MetricUtil.calculateAverageMakespanTime(time, server));
                })
        );

        return new SimulationMetricResult(
                averageQueueSizeMap,
                averageCpuUtilizationMap,
                averageBlockedTasksMap,
                averageResponseTimeMap,
                averageMakespanTimeMap);
    }

    public void writeToCsv() {
        SimulationMetricResult result = toMetricResult();
        String dir = "result/";
        CSVUtils.writeMapToCsv(dir + "averageQueueSize.csv", aggregateValues(result.getAverageQueueSizeMap()));
        CSVUtils.writeMapToCsv(dir + "averageLBF.csv", aggregateLBF(result.getAverageCpuUtilizationMap()));
        CSVUtils.writeMapToCsv(dir + "averageBlockedTasks.csv", aggregateValues(result.getAverageBlockedTasksMap()));
        CSVUtils.writeMapToCsv(dir + "averageResponseTime.csv", aggregateValues(result.getAverageResponseTimeMap()));
        CSVUtils.writeMapToCsv(dir + "averageMakespanTime.csv", aggregateValues(result.getAverageMakespanTimeMap()));
    }

    private <T extends Number> Map<Double, Double> aggregateValues(Map<Double, List<T>> dataMap) {
        Map<Double, Double> result = new HashMap<>();
        dataMap.forEach((time, dataList) -> {
            double avg = dataList.stream().mapToDouble(Number::doubleValue).sum() / (this.totalRounds * dataList.size());
            result.put(time, avg);
        });
        return result;
    }

    public static Map<Double, Double> aggregateLBF(Map<Double, List<Double>> cpuUtilizationMap) {
        Map<Double, Double> result = new HashMap<>();
        cpuUtilizationMap.forEach((time, dataList) -> {
            double avg = MetricUtil.calculateLBF(dataList, time);
            result.put(time, avg);
        });
        return result;
    }
}
