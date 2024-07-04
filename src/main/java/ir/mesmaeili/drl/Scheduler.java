package ir.mesmaeili.drl;

import ir.mesmaeili.drl.model.Server;
import ir.mesmaeili.drl.model.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
public class Scheduler {

    private final Queue<Task> blockedQueue;

    public Scheduler() {
        blockedQueue = new LinkedList<>();
    }

    public void scheduleTasks(List<Server> servers, Queue<Task> taskQueue, double R_Delta) {
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            boolean taskScheduled = false;
            for (Server server : servers) {
                if (server.addTask(task)) {
                    log.info("Assign task {} to server {}", task.getId(), server.getId());
                    taskScheduled = true;
                    break;
                }
            }
            if (!taskScheduled) {
                log.info("task {} has been blocked", task.getId());
                blockedQueue.add(task);
            }
        }

        for (Server server : servers) {
            new Thread(() -> server.executeTasks(R_Delta)).start();
        }
    }

    public static double calculateLBF(List<Server> servers, double averageCpuUtilization) {
        double sumSquaredDifferences = 0;
        for (Server server : servers) {
            double ui = server.calculateCpuUtilization(server.getDeltaT());
            sumSquaredDifferences += Math.pow(ui - averageCpuUtilization, 2);
        }
        return Math.sqrt(sumSquaredDifferences / servers.size());
    }
}

