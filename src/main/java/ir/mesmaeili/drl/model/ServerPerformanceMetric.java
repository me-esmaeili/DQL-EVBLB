package ir.mesmaeili.drl.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServerPerformanceMetric {
    @EqualsAndHashCode.Include
    private double cpuUtilization;
    private int queueSize;
    private int blockedTaskCount;
}
