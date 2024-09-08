package ir.mesmaeili.lba.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ServerPerformanceMetric implements Serializable {
    private double cpuUtilization;
    private int queueSize;
}
