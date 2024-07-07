package ir.mesmaeili.lba.statistic;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import ir.mesmaeili.lba.result.SimulationMetricResult;
import ir.mesmaeili.lba.util.CsvUtils;
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
        Map<Double, List<Double>> LBFMap = new HashMap<>();
        Map<Double, List<Double>> averageResponseTimeMap = new HashMap<>();
        Map<Double, List<Double>> averageMakespanTimeMap = new HashMap<>();
        Map<Double, List<Double>> averageThroughputMap = new HashMap<>();

        // Collect metrics from each server
        this.getEdgeServers().forEach(server ->
                server.getMetrics().forEach((time, metric) -> {
                    averageQueueSizeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getQueueSize());
                    LBFMap.computeIfAbsent(time, k -> new ArrayList<>()).add(metric.getCpuUtilization());
                    averageResponseTimeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(MetricUtil.calculateAverageResponseTime(time, server));
                    averageMakespanTimeMap.computeIfAbsent(time, k -> new ArrayList<>()).add(MetricUtil.calculateAverageMakespanTime(time, server));
                    averageThroughputMap.computeIfAbsent(time, k -> new ArrayList<>()).add(MetricUtil.calculateThroughput(time, getDeltaT(), server));
                })
        );
        return new SimulationMetricResult(
                aggregateLBF(LBFMap),
                sumValues(averageThroughputMap),
                MetricUtil.calculateBlockingRate(edgeServers), // need to access total blocked and processed task list
                averageValues(averageQueueSizeMap),
                averageValues(averageResponseTimeMap),
                averageValues(averageMakespanTimeMap));
    }

    public void writeToCsv() {
        SimulationMetricResult result = toMetricResult();
        String dir = "result/";
        CsvUtils.writeMapToCsv(dir + "LBF.csv", result.getLBFOverTimeMap());
        CsvUtils.writeMapToCsv(dir + "throughput.csv", result.getThroughputOverTimeMap());
        CsvUtils.writeMapToCsv(dir + "blockingRate.csv", result.getBlockingRateOverTimeMap());
        CsvUtils.writeMapToCsv(dir + "averageQueueSize.csv", result.getAverageQueueSizeOverTimeMap());
        CsvUtils.writeMapToCsv(dir + "averageResponseTime.csv", result.getAverageResponseTimeOverTimeMap());
        CsvUtils.writeMapToCsv(dir + "averageMakespanTime.csv", result.getAverageMakespanTimeOverTimeMap());
    }

    private <T extends Number> Map<Double, Double> averageValues(Map<Double, List<T>> dataMap) {
        Map<Double, Double> result = new HashMap<>();
        dataMap.forEach((time, dataList) -> {
            double avg = dataList.stream().mapToDouble(Number::doubleValue).sum() / (this.totalRounds * dataList.size());
            result.put(time, avg);
        });
        return result;
    }

    private <T extends Number> Map<Double, Double> sumValues(Map<Double, List<T>> dataMap) {
        Map<Double, Double> result = new HashMap<>();
        dataMap.forEach((time, dataList) -> {
            double avg = dataList.stream().mapToDouble(Number::doubleValue).sum();
            result.put(time, avg);
        });
        return result;
    }

    public static Map<Double, Double> aggregateLBF(Map<Double, List<Double>> cpuUtilizationMap) {
        Map<Double, Double> result = new HashMap<>();
        cpuUtilizationMap.forEach((time, dataList) -> {
            double avg = MetricUtil.calculateLBF(dataList);
            result.put(time, avg);
        });
        return result;
    }
}
