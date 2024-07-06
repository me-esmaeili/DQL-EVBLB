package ir.mesmaeili.lba.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SimulationMetricResult {
    private Map<Double, List<Integer>> averageQueueSizeMap = new HashMap<>();
    private Map<Double, List<Double>> averageCpuUtilizationMap = new HashMap<>();
    private Map<Double, List<Integer>> averageBlockedTasksMap = new HashMap<>();
    private Map<Double, List<Double>> averageResponseTimeMap = new HashMap<>();
    private Map<Double, List<Double>> averageMakespanTimeMap = new HashMap<>();
}
