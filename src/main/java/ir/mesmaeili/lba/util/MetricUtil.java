package ir.mesmaeili.lba.util;

import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricUtil {
    public static double calculateLBF(List<Double> cpuUtilization) {
        double averageCpuUtilization = cpuUtilization.stream().mapToDouble(d -> d).average().orElse(0);
        double sumSquaredDifferences = cpuUtilization.stream().mapToDouble(serverUtilization -> Math.pow(serverUtilization - averageCpuUtilization, 2)).sum();
        return Math.sqrt(sumSquaredDifferences / cpuUtilization.size());
    }

    public static double calculateAverageResponseTime(double time, EdgeServer server) {
        if (server.getRoundProcessedTaskQueue().containsKey(NumberUtil.round(time, 2))) {
            return server.getRoundProcessedTaskQueue().get(NumberUtil.round(time, 2)).stream()
                    .mapToDouble(Task::getResponseTime)
                    .average()
                    .orElse(0);
        } else {
            return 0.;
        }
    }

    public static double calculateThroughput(double time, double deltaT, EdgeServer server) {
        if (server.getRoundProcessedTaskQueue().containsKey(NumberUtil.round(time, 2))) {
            return server.getRoundProcessedTaskQueue().get(NumberUtil.round(time, 2)).size() / deltaT;
        } else {
            return 0.;
        }
    }

    public static double calculateAverageMakespanTime(double time, EdgeServer server) {
        if (server.getRoundProcessedTaskQueue().containsKey(NumberUtil.round(time, 2))) {
            return server.getRoundProcessedTaskQueue().get(NumberUtil.round(time, 2)).stream()
                    .mapToDouble(Task::getMakespanTime)
                    .average()
                    .orElse(0);
        } else {
            return 0.;
        }
    }

    public static Map<Double, Double> calculateBlockingRate(List<EdgeServer> edgeServers) {
        Map<Double, Double> blockingRate = new HashMap<>();
        Map<Double, Double> totalBlockedTasks = new HashMap<>();
        Map<Double, Double> totalProcessTasks = new HashMap<>();
        for (EdgeServer server : edgeServers) {
            server.getRoundBlockedTaskQueue().forEach((time, value) -> totalBlockedTasks.merge(time, value.size() * 1.0, Double::sum));
            server.getRoundProcessedTaskQueue().forEach((time, value) -> totalProcessTasks.merge(time, value.size() * 1.0, Double::sum));
        }

        // computer rate in every time point
        for (Double key : totalProcessTasks.keySet()) {
            Double blocked = totalBlockedTasks.get(key);
            Double processed = totalProcessTasks.get(key);
            if (blocked != null && processed != null) {
                double total = blocked + processed;
                if (total != 0) {
                    blockingRate.put(key, blocked / total);
                }
            } else {
                blockingRate.put(key, 0.);
            }
        }
        return blockingRate;
    }
}
