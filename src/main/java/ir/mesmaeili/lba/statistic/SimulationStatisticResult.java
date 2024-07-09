package ir.mesmaeili.lba.statistic;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import ir.mesmaeili.lba.result.SimulationMetricResult;
import ir.mesmaeili.lba.util.CsvUtils;
import ir.mesmaeili.lba.util.MetricUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
@Setter
public class SimulationStatisticResult {
    private SimulationConfig config;
    private long startTime;
    private long finishTime;
    private int totalRounds;
    private double totalSimulationTime;
    private double DeltaT;
    private Queue<Task> tasks = new LinkedList<>();
    private List<EdgeServer> edgeServers = new ArrayList<>();

    public SimulationStatisticResult(long startTime, SimulationConfig config) {
        this.config = config;
        this.startTime = startTime;
        this.DeltaT = config.getDeltaT();
        this.totalSimulationTime = config.getTotalSimulationTime();
    }

    public void addTasks(Queue<Task> tasks) {
        this.tasks.addAll(tasks);
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
        String simulationState = String.format("DeltaT-%.2f-SimulationTime-%.2f", getDeltaT(), getTotalSimulationTime());
        CsvUtils.writeMapToCsv(dir + String.format("LBF-%s.csv", simulationState), result.getLBFOverTimeMap());
        CsvUtils.writeMapToCsv(dir + String.format("throughput-%s.csv", simulationState), result.getThroughputOverTimeMap());
        CsvUtils.writeMapToCsv(dir + String.format("blockingRate-%s.csv", simulationState), result.getBlockingRateOverTimeMap());
        CsvUtils.writeMapToCsv(dir + String.format("averageQueueSize-%s.csv", simulationState), result.getAverageQueueSizeOverTimeMap());
        CsvUtils.writeMapToCsv(dir + String.format("averageResponseTime-%s.csv", simulationState), result.getAverageResponseTimeOverTimeMap());
        CsvUtils.writeMapToCsv(dir + String.format("averageMakespanTime-%s.csv", simulationState), result.getAverageMakespanTimeOverTimeMap());
    }

    public void printReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n-------------- Simulation Report --------------\n");
        sb.append(String.format("Simulation start time: %d\n", startTime));
        sb.append(String.format("Simulation round count: %d\n", startTime));
        sb.append(String.format("Simulation DeltaT : %d\n", startTime));
        sb.append(String.format("Simulation finish time: %d\n\n", startTime));

        sb.append("-------------- Servers --------------\n");
        sb.append(String.format("Total Server count : %d\n", edgeServers.size()));
        sb.append(String.format("Maximum Server queue size : %d\n", config.getServerMaxQueueSize()));
        sb.append(String.format("Avg Server CPU :  %.2f MHz\n",
                edgeServers.stream().map(EdgeServer::getProcessingCapacity).mapToDouble(c -> c).average().orElse(0.)));
        sb.append(String.format("Total network processing power:  %.2f MHz\n",
                edgeServers.stream().map(EdgeServer::getProcessingCapacity).mapToDouble(c -> c).sum()));
        sb.append(String.format("Avg Server MEMORY :  %.2f MB\n",
                edgeServers.stream().map(EdgeServer::getMemoryCapacity).mapToDouble(c -> c).average().orElse(0.)));
        sb.append(String.format("Avg Server DISK :  %.2fin GB\n\n",
                edgeServers.stream().map(EdgeServer::getDiskCapacity).mapToDouble(c -> c).average().orElse(0.)));

        sb.append("-------------- Tasks --------------\n");
        List<Task> processedServers = edgeServers.stream().flatMap(s -> s.getRoundProcessedTaskQueue().values().stream())
                .flatMap(Collection::stream).toList();
        List<Task> blockedServers = edgeServers.stream().flatMap(s -> s.getRoundBlockedTaskQueue().values().stream())
                .flatMap(Collection::stream).toList();

        sb.append(String.format("Total Task count : %d\n", tasks.size()));
        sb.append(String.format("Avg Task CPU : %.2f MHz\n",
                tasks.stream().map(Task::getCpu).mapToDouble(c -> c).average().orElse(0.)));
        sb.append(String.format("Avg Task MEMORY : %.2f MB\n",
                tasks.stream().map(Task::getMemory).mapToDouble(c -> c).average().orElse(0.)));
        sb.append(String.format("Avg Task DISK : %.2f GB\n\n",
                tasks.stream().map(Task::getDisk).mapToDouble(c -> c).average().orElse(0.)));

        sb.append(String.format("Total completed Task count : %d\n", processedServers.size()));
        sb.append(String.format("Avg completed Task CPU : %.2f MHz\n",
                processedServers.stream().map(Task::getCpu).mapToDouble(c -> c).average().orElse(0.)));
        sb.append(String.format("Total required processing to process completed Tasks : %.2f MHz\n\n",
                processedServers.stream().map(Task::getCpu).mapToDouble(c -> c).sum()));

        sb.append(String.format("Total blocked Task count : %d\n", blockedServers.size()));
        sb.append(String.format("Avg blocked Task CPU : %.2f MHz\n",
                blockedServers.stream().map(Task::getCpu).mapToDouble(c -> c).average().orElse(0.)));

        log.info(sb.toString());
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
