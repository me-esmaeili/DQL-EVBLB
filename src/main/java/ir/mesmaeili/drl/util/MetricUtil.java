package ir.mesmaeili.drl.util;

import ir.mesmaeili.drl.model.EdgeServer;

import java.util.List;

public class MetricUtil {
    public static double calculateLBF(List<Double> cpuUtilization, double time) {
        double averageCpuUtilization = cpuUtilization.stream().mapToDouble(d -> d).average().getAsDouble();
        double sumSquaredDifferences = 0;
        for (Double serverUtilization : cpuUtilization) {
            sumSquaredDifferences += Math.pow(serverUtilization - averageCpuUtilization, 2);
        }
        return Math.sqrt(sumSquaredDifferences / cpuUtilization.size());
    }

    public static double calculateAverageResponseTime(EdgeServer server) {
        return server.getTaskQueue().stream()
                .filter(task -> task.getProcessStartTime() > 0)
                .mapToDouble(task -> task.getProcessStartTime() - task.getArrivalTime())
                .average()
                .orElse(0);
    }

    public static double calculateAverageMakespanTime(EdgeServer server) {
        return server.getTaskQueue().stream()
                .filter(task -> task.getFinishTime() > 0)
                .mapToDouble(task -> task.getFinishTime() - task.getArrivalTime())
                .average()
                .orElse(0);
    }
}
