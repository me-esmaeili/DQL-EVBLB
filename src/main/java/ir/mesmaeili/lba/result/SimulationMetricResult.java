package ir.mesmaeili.lba.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SimulationMetricResult {
    private Map<Integer, Double> LBFOverTimeMap = new HashMap<>();
    private Map<Integer, Double> throughputOverTimeMap = new HashMap<>();
    private Map<Integer, Double> blockingRateOverTimeMap = new HashMap<>();
    private Map<Integer, Double> averageQueueSizeOverTimeMap = new HashMap<>();
    private Map<Integer, Double> averageResponseTimeOverTimeMap = new HashMap<>();
    private Map<Integer, Double> averageMakespanTimeOverTimeMap = new HashMap<>();
}
