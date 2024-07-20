package ir.mesmaeili.lba.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerPerformanceMetric {
    private double cpuUtilization;
    private int queueSize;
}
