package ir.mesmaeili.lba.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EdgeServer implements Serializable {
    @EqualsAndHashCode.Include
    private int id;
    @Getter
    @Setter
    private Point location;
    private double memoryCapacity; // in MB
    private double diskCapacity; // in GB
    private double processingCapacity; // in MHZ
    @JsonIgnore
    private final Queue<Task> taskQueue = new LinkedList<>();
    @JsonIgnore
    private final Queue<Task> cloudQueue = new LinkedList<>();
    @JsonIgnore
    private final Map<Integer, List<Task>> roundProcessedTaskQueue = new HashMap<>();
    @JsonIgnore
    private final Map<Integer, List<Task>> roundBlockedTaskQueue = new HashMap<>();
    @JsonIgnore
    private final Map<Integer, ServerPerformanceMetric> metrics = new HashMap<>();

    public EdgeServer(int id, SimulationConfig simulationConfig) {
        this.id = id;
        this.memoryCapacity = simulationConfig.getRandomServerMemoryInMB();
        this.diskCapacity = simulationConfig.getRandomServerDiskInGB();
        this.processingCapacity = simulationConfig.getRandomServerCpuInMhz();
    }

    public void addTask(Task task, SimulationState simulationState, SimulationConfig simulationConfig) {
        if (taskQueue.size() < simulationConfig.getServerMaxQueueSize()) {
            taskQueue.add(task);
            log.info("Add task {} to server {} in simulation time {} with queue size {}",
                    task.getId(), this.id, simulationState.getCurrentSimulationTime(), taskQueue.size());
        } else {
            log.info("Could not add task {} to server {} in simulation time {}",
                    task.getId(), this.id, simulationState.getCurrentSimulationTime());
            roundBlockedTaskQueue.computeIfAbsent(simulationState.getCurrentRound(), k -> new ArrayList<>()).add(task);
        }
    }

    public double calculateRemainingResource(double alpha, double beta, double gamma, double deltaT) {
        double taskNormalizedResource = taskQueue.stream().mapToDouble(task -> (alpha * task.getMemory() +
                beta * task.getDisk() +
                gamma * (task.getCpu() / deltaT))).sum();
        double serverNormalizedResource = alpha * memoryCapacity + beta * diskCapacity + gamma * processingCapacity;
        return serverNormalizedResource - taskNormalizedResource;
    }

    public synchronized void executeTasks(double deltaT, double currentSimulationTime, int currentRound) {
        BigDecimal currentTime = BigDecimal.ZERO;
        BigDecimal deltaTBig = BigDecimal.valueOf(deltaT);
        BigDecimal currentSimulationTimeBig = BigDecimal.valueOf(currentSimulationTime);

        while (!taskQueue.isEmpty() && currentTime.compareTo(deltaTBig) < 0) {
            Task task = taskQueue.peek();
            if (canExecuteTask(task)) {
                log.info("Task {} executed by server {}, at simulation time {}", task.getId(), getId(), currentSimulationTime);

                // to prevent lost precision, we use BigDecimal
                BigDecimal taskProcessingRequirementMHz = task.getRemainingCpu() == 0
                        ? BigDecimal.valueOf(task.getCpu())
                        : BigDecimal.valueOf(task.getRemainingCpu());
                BigDecimal processingCapacityBig = BigDecimal.valueOf(processingCapacity);
                BigDecimal taskProcessingTimeSeconds = taskProcessingRequirementMHz.divide(processingCapacityBig, 2, RoundingMode.HALF_UP);

                // Check if the task can be fully processed within the remaining DeltaT time
                if (currentTime.add(taskProcessingTimeSeconds).compareTo(deltaTBig) <= 0) {
                    taskQueue.poll();
                    BigDecimal taskStartProcessingTime = currentSimulationTimeBig.add(currentTime).add(BigDecimal.valueOf(task.getArrivalTime()));
                    task.setProcessStartTime(taskStartProcessingTime.doubleValue());
                    currentTime = currentTime.add(taskProcessingTimeSeconds); // proceed time
                    task.setFinishTime(taskStartProcessingTime.add(taskProcessingTimeSeconds).doubleValue());
                    roundProcessedTaskQueue.computeIfAbsent(currentRound, k -> new ArrayList<>()).add(task);
                    task.setRemainingCpu(0); // Task is fully processed, set remaining CPU to 0
                } else {
                    // Calculate the remaining time for DeltaT and reduce the remaining CPU of the task accordingly
                    BigDecimal remainingDeltaT = deltaTBig.subtract(currentTime);
                    BigDecimal reducedCpuRequirement = taskProcessingRequirementMHz.multiply(remainingDeltaT).divide(taskProcessingTimeSeconds, 2, RoundingMode.HALF_UP);
                    task.setRemainingCpu(task.getRemainingCpu() - reducedCpuRequirement.doubleValue());
                    currentTime = deltaTBig; // Update the current time to DeltaT as we have reached the end of this round
                }
            } else {
                taskQueue.poll();
                log.info("Task {} send to Cloud to execute", task.getId());
                cloudQueue.add(task);
            }
        }
    }


    private boolean canExecuteTask(Task task) {
        return this.memoryCapacity >= task.getMemory() ||
                (this.diskCapacity - this.taskQueue.stream().mapToDouble(Task::getDisk).sum()) >= task.getDisk();
    }

    public double calculateCpuUtilization(double deltaT) {
        double totalCpuUsage = taskQueue.stream().mapToDouble(Task::getCpu).sum();
        return Math.min(1, totalCpuUsage / (processingCapacity * deltaT));
    }

    public void calculateMetrics(int currentRound, SimulationConfig simulationConfig) {
        ServerPerformanceMetric metric = new ServerPerformanceMetric();
        metric.setCpuUtilization(calculateCpuUtilization(simulationConfig.getDeltaT()));
        metric.setQueueSize(taskQueue.size());
        log.info("Server {}: Cpu Utilization is {}, Queue Size is {} at round {}",
                this.getId(), metric.getCpuUtilization(), metric.getQueueSize(), currentRound);
        this.metrics.put(currentRound, metric);
    }

    @JsonIgnore
    public double getCpuUsage() {
        return Math.min(1, taskQueue.stream().map(Task::getCpu).mapToDouble(d -> d).sum() / processingCapacity) * 100;
    }

    @JsonIgnore
    public double getMemoryUsage() {
        return Math.min(1, taskQueue.stream().map(Task::getMemory).mapToDouble(d -> d).sum() / memoryCapacity) * 100;
    }

    @JsonIgnore
    public double getDiskUsage() {
        return Math.min(1, taskQueue.stream().map(Task::getDisk).mapToDouble(d -> d).sum() / diskCapacity) * 100;
    }
}
