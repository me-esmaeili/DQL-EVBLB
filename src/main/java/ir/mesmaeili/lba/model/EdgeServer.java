package ir.mesmaeili.lba.model;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.util.NumberUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EdgeServer {
    @EqualsAndHashCode.Include
    private final int id;
    private final Coordinate location;
    private final double memoryCapacity; // in MB
    private final double diskCapacity; // in GB
    private final double processingCapacity; // in MHZ
    private final Queue<Task> taskQueue;
    private final Queue<Task> blockedQueue;
    private final SimulationConfig simulationConfig;
    private final Map<Double, List<Task>> roundProcessedTaskQueue;

    private final List<EdgeServer> neighbors;
    private final Map<Double, ServerPerformanceMetric> metrics;

    public EdgeServer(int id, Coordinate location, SimulationConfig simulationConfig) {
        this.id = id;
        this.location = location;
        this.simulationConfig = simulationConfig;
        this.memoryCapacity = SimulationConfig.getRandomServerMemoryInMB();
        this.diskCapacity = SimulationConfig.getRandomServerDiskInGB();
        this.processingCapacity = SimulationConfig.getRandomServerCpuInMhz();
        this.neighbors = new ArrayList<>();
        this.taskQueue = new LinkedList<>();
        this.blockedQueue = new LinkedList<>();
        this.metrics = new HashMap<>();
        this.roundProcessedTaskQueue = new HashMap<>();
    }

    public void addTask(Task task) {
        if (taskQueue.size() < simulationConfig.getServerMaxQueueSize()) {
            taskQueue.add(task);
        } else {
            blockedQueue.add(task);
        }
    }

    public double calculateRemainingResource(double alpha, double beta, double gamma, double deltaT) {
        double usedMemory = 0.;
        double usedDisk = 0.;
        double usedCpu = 0.;
        for (Task task : taskQueue) {
            usedMemory += task.getMemory();
            usedDisk += task.getDisk();
            usedCpu += task.getCpu() / deltaT;
        }
        double normalizedMemory = alpha * (usedMemory / memoryCapacity);
        double normalizedDisk = beta * (usedDisk / diskCapacity);
        double normalizedCpu = gamma * (usedCpu / processingCapacity);

        return 1 - (normalizedMemory + normalizedDisk + normalizedCpu);
    }


    public void executeTasks(double deltaT, double currentSimulationTime) {
        BigDecimal currentTime = BigDecimal.ZERO;
        BigDecimal deltaTBig = BigDecimal.valueOf(deltaT);
        BigDecimal currentSimulationTimeBig = BigDecimal.valueOf(currentSimulationTime);

        while (!taskQueue.isEmpty() && currentTime.compareTo(deltaTBig) < 0) {
            Task task = taskQueue.peek();
            if (canExecuteTask(task)) {
                log.info("Task " + task.getId() + " executed by server " + getId());

                // to prevent lost precise, we use BigDecimal
                BigDecimal taskProcessingRequirementMHz = BigDecimal.valueOf(task.getCpu());
                BigDecimal processingCapacityBig = BigDecimal.valueOf(processingCapacity);
                BigDecimal taskProcessingTimeSeconds = taskProcessingRequirementMHz.divide(processingCapacityBig, 2, RoundingMode.HALF_UP);

                if (currentTime.add(taskProcessingTimeSeconds).compareTo(deltaTBig) <= 0) {
                    taskQueue.poll();
                    BigDecimal taskStartProcessingTime = currentSimulationTimeBig.add(currentTime);
                    task.setProcessStartTime(taskStartProcessingTime.doubleValue());
                    currentTime = currentTime.add(taskProcessingTimeSeconds); // proceed time
                    task.setFinishTime(taskStartProcessingTime.add(taskProcessingTimeSeconds).longValue());
                    roundProcessedTaskQueue.computeIfAbsent(NumberUtil.round(currentSimulationTime, 2), k -> new ArrayList<>()).add(task);
                } else {
                    break;
                }
            } else {
                log.info("Task " + task.getId() + " send to Cloud to execute");
                blockedQueue.add(task);
            }
        }
    }

    private boolean canExecuteTask(Task task) {
        return this.memoryCapacity >= task.getMemory() ||
                (this.diskCapacity - this.taskQueue.stream().mapToDouble(Task::getDisk).sum()) >= task.getDisk();
    }

    private double calculateCpuUtilization(double deltaT) {
        double totalCpuUsage = taskQueue.stream().mapToDouble(Task::getCpu).sum();
        return Math.min(1, totalCpuUsage / (processingCapacity * deltaT));
    }

    public void calculateMetrics(double currentSimulationTime) {
        ServerPerformanceMetric metric = new ServerPerformanceMetric();
        metric.setCpuUtilization(calculateCpuUtilization(simulationConfig.getDeltaT()));
        metric.setBlockedTaskCount(blockedQueue.size());
        metric.setQueueSize(taskQueue.size());
        this.metrics.put(currentSimulationTime, metric);
    }
}