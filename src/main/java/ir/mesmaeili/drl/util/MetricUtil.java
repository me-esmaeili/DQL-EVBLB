package ir.mesmaeili.drl.util;

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
}
