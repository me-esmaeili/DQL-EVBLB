package ir.mesmaeili.lba.model;

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
}
