package ir.mesmaeili.drl.model;

import ir.mesmaeili.drl.config.SimulationConfig;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;

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

    private final List<EdgeServer> neighbors;
    private final Map<Double, ServerPerformanceMetric> metrics = new HashMap<>();

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

    public void executeTasks(double DeltaT) {
        double currentTime = 0;

        while (!taskQueue.isEmpty() && currentTime < DeltaT) {
            Task task = taskQueue.peek();
            if (canExecuteTask(task)) {
                log.info("Task {} executed by server {}", task.getId(), getId());
                double taskProcessingRequirementMHz = task.getCpu();
                double taskProcessingTimeSeconds = taskProcessingRequirementMHz / processingCapacity;
                if (currentTime + taskProcessingTimeSeconds <= DeltaT) {
                    taskQueue.poll();
                    long taskStartProcessingTime = System.currentTimeMillis();
                    task.setProcessStartTime(taskStartProcessingTime);
                    currentTime += taskProcessingTimeSeconds; // proceed time
                    task.setFinishTime((long) ((taskStartProcessingTime * 1000) + taskProcessingTimeSeconds));
                } else {
                    break;
                }
            } else {
                log.info("Task {} send to Cloud to execute", task.getId());
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

    public void calculateMetrics(double time) {
        ServerPerformanceMetric metric = new ServerPerformanceMetric();
        metric.setCpuUtilization(calculateCpuUtilization(simulationConfig.getDeltaT()));
        metric.setBlockedTaskCount(blockedQueue.size());
        metric.setQueueSize(taskQueue.size());
        this.metrics.put(time, metric);
    }
}
