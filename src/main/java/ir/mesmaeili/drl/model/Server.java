package ir.mesmaeili.drl.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
public class Server {
    private final int id;
    private static final int MAX_QUEUE_SIZE = 200;
    private final double x;
    private final double y;
    private double memory;
    private double disk;
    private double cpu;
    private final Queue<Task> taskQueue;
    private final Queue<Task> blocked;

    private double alpha;
    private double beta;
    private double gamma;
    private double deltaT;

    private final List<Server> neighbors;
    private final double remainingPower;

    public Server(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.neighbors = new ArrayList<>();
        this.taskQueue = new LinkedList<>();
        this.blocked = new LinkedList<>();
        this.remainingPower = calculateRemainingPower(alpha, beta, gamma, deltaT);
    }

    public boolean addTask(Task task) {
        if (taskQueue.size() < MAX_QUEUE_SIZE) {
            taskQueue.add(task);
            return true;
        } else {
            return false;
        }
    }

    public double calculateRemainingPower(double alpha, double beta, double gamma, double deltaT) {
        double usedMemory = 0;
        double usedDisk = 0;
        double usedCpu = 0;

        for (Task task : taskQueue) {
            usedMemory += task.getMemory();
            usedDisk += task.getDisk();
            usedCpu += task.getCpu() / deltaT;
        }
        double normalizedMemory = alpha * (usedMemory / memory);
        double normalizedDisk = beta * (usedDisk / disk);
        double normalizedCpu = gamma * (usedCpu / cpu);

        return 1 - (normalizedMemory + normalizedDisk + normalizedCpu);
    }

    public void executeTasks(double R_Delta) {
        double currentTime = 0;
        double serverProcessingSpeedMHz = this.cpu * 1000; // to MHZ

        while (!taskQueue.isEmpty() && currentTime < R_Delta) {
            Task task = taskQueue.peek();
            if (canExecuteTask(task)) {
                log.info("Task {} executed by server {}", task.getId(), getId());
                double taskProcessingRequirementMHz = task.getCpu();
                double taskProcessingTimeSeconds = taskProcessingRequirementMHz / serverProcessingSpeedMHz;
                if (currentTime + taskProcessingTimeSeconds <= R_Delta) {
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
                blocked.add(task);  // send to cloud servers
            }
        }
    }

    private boolean canExecuteTask(Task task) {
        return this.memory <= task.getMemory() || this.disk <= task.getDisk();
    }

    public double calculateCpuUtilization(double deltaT) {
        double totalCpuUsage = taskQueue.stream().mapToDouble(Task::getCpu).sum();
        return Math.min(1, totalCpuUsage / (cpu * deltaT));
    }
}
