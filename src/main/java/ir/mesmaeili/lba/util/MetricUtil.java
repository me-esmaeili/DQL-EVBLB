package ir.mesmaeili.lba.util;

import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;

import java.util.List;

public class MetricUtil {
    public static double calculateLBF(List<Double> cpuUtilization, double time) {
        double averageCpuUtilization = cpuUtilization.stream().mapToDouble(d -> d).average()
                .orElse(0);
        double sumSquaredDifferences = 0;
        for (Double serverUtilization : cpuUtilization) {
            sumSquaredDifferences += Math.pow(serverUtilization - averageCpuUtilization, 2);
        }
        return Math.sqrt(sumSquaredDifferences / cpuUtilization.size());
    }

    public static double calculateAverageResponseTime(double time, EdgeServer server) {
        if (server.getRoundProcessedTaskQueue().containsKey(NumberUtil.round(time, 2))) {
            return server.getRoundProcessedTaskQueue().get(NumberUtil.round(time, 2)).stream()
                    .filter(task -> task.getProcessStartTime() > 0)
                    .mapToDouble(Task::getResponseTime)
                    .average()
                    .orElse(0);
        } else {
            return 0.;
        }
    }

    public static double calculateAverageMakespanTime(double time, EdgeServer server) {
        if (server.getRoundProcessedTaskQueue().containsKey(NumberUtil.round(time, 2))) {
            return server.getRoundProcessedTaskQueue().get(NumberUtil.round(time, 2)).stream()
                    .filter(task -> task.getFinishTime() > 0)
                    .mapToDouble(Task::getMakespanTime)
                    .average()
                    .orElse(0);
        } else {
            return 0.;
        }
    }
}
