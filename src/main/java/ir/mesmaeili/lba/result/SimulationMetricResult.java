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
    private Map<Double, Double> LBFOverTimeMap = new HashMap<>();
    private Map<Double, Double> throughputOverTimeMap = new HashMap<>();
    private Map<Double, Double> blockingRateOverTimeMap = new HashMap<>();
    private Map<Double, Double> averageQueueSizeOverTimeMap = new HashMap<>();
    private Map<Double, Double> averageResponseTimeOverTimeMap = new HashMap<>();
    private Map<Double, Double> averageMakespanTimeOverTimeMap = new HashMap<>();
}
