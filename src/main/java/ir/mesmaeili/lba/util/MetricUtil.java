package ir.mesmaeili.lba.util;

import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricUtil {
    public static double calculateLBF(List<Double> cpuUtilization) {
        double avgCpuUtilization = cpuUtilization.stream().mapToDouble(d -> d).average().orElse(0);
        double sumSquaredDifferences = cpuUtilization.stream().mapToDouble(u -> Math.pow(u - avgCpuUtilization, 2)).sum();
        return Math.sqrt(sumSquaredDifferences / cpuUtilization.size());
    }

    public static double calculateAverageResponseTime(Integer time, EdgeServer server) {
        if (server.getRoundProcessedTaskQueue().containsKey(time)) {
            return server.getRoundProcessedTaskQueue().get(time).stream()
                    .mapToDouble(Task::getResponseTime)
                    .average()
                    .orElse(0);
        } else {
            return 0.;
        }
    }

    public static double calculateThroughput(Integer time, double deltaT, EdgeServer server) {
        if (server.getRoundProcessedTaskQueue().containsKey(time)) {
            return server.getRoundProcessedTaskQueue().get((time)).size() / deltaT;
        } else {
            return 0.;
        }
    }

    public static double calculateAverageMakespanTime(Integer time, EdgeServer server) {
        if (server.getRoundProcessedTaskQueue().containsKey(time)) {
            return server.getRoundProcessedTaskQueue().get((time)).stream()
                    .mapToDouble(Task::getMakespanTime)
                    .average()
                    .orElse(0);
        } else {
            return 0.;
        }
    }

    public static Map<Integer, Double> calculateBlockingRate(List<EdgeServer> edgeServers) {
        Map<Integer, Double> blockingRate = new HashMap<>();
        Map<Integer, Double> totalBlockedTasks = new HashMap<>();
        Map<Integer, Double> totalProcessTasks = new HashMap<>();
        for (EdgeServer server : edgeServers) {
            server.getRoundBlockedTaskQueue().forEach((time, value) -> totalBlockedTasks.merge(time, value.size() * 1.0, Double::sum));
            server.getRoundProcessedTaskQueue().forEach((time, value) -> totalProcessTasks.merge(time, value.size() * 1.0, Double::sum));
        }

        // computer rate in every round point
        for (Integer round : totalProcessTasks.keySet()) {
            Double blocked = totalBlockedTasks.get(round);
            Double processed = totalProcessTasks.get(round);
            if (blocked != null && processed != null) {
                double total = blocked + processed;
                if (total != 0) {
                    blockingRate.put(round, blocked / total);
                }
            } else {
                blockingRate.put(round, 0.);
            }
        }
        return blockingRate;
    }
}
