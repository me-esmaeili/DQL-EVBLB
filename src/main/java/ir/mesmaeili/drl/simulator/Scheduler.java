package ir.mesmaeili.drl.simulator;

import ir.mesmaeili.drl.model.EdgeServer;
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

    public void scheduleTasks(List<EdgeServer> edgeServers, Queue<Task> taskQueue, double R_Delta) {
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            boolean taskScheduled = false;
            for (EdgeServer edgeServer : edgeServers) {
                if (edgeServer.addTask(task)) {
                    log.info("Assign task {} to server {}", task.getId(), edgeServer.getId());
                    taskScheduled = true;
                    break;
                }
            }
            if (!taskScheduled) {
                log.info("task {} has been blocked", task.getId());
                blockedQueue.add(task);
            }
        }

        for (EdgeServer edgeServer : edgeServers) {
            new Thread(() -> edgeServer.executeTasks(R_Delta)).start();
        }
    }

    public static double calculateLBF(List<EdgeServer> edgeServers, double averageCpuUtilization) {
        double sumSquaredDifferences = 0;
        for (EdgeServer edgeServer : edgeServers) {
            double ui = edgeServer.calculateCpuUtilization(edgeServer.getDeltaT());
            sumSquaredDifferences += Math.pow(ui - averageCpuUtilization, 2);
        }
        return Math.sqrt(sumSquaredDifferences / edgeServers.size());
    }
}

