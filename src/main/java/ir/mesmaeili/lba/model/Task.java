package ir.mesmaeili.lba.model;

import ir.mesmaeili.lba.config.SimulationConfig;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {
    private static long counter = 1;
    @EqualsAndHashCode.Include
    private final long id;
    private final double memory; // in MB
    private final double disk; // in GB
    private final double cpu; // in MHZ
    @Getter
    @Setter
    private double remainingCpu = 0.; // in MHZ is used to store remaining for next round of LB excution
    @Getter
    @Setter
    private double arrivalTime;
    @Getter
    @Setter
    private double processStartTime;
    @Getter
    @Setter
    private double finishTime;
    @Getter
    @Setter
    private Coordinate location;

    public Task() {
        this.id = generateId();
        this.memory = SimulationConfig.getRandomTaskMemoryInMB();
        this.disk = SimulationConfig.getRandomTaskDiskInMB();
        this.cpu = SimulationConfig.getRandomTaskCpuInMhz();
    }

    public double getResponseTime() {
        return new BigDecimal(processStartTime - arrivalTime).doubleValue();
    }

    public double getMakespanTime() {
        return new BigDecimal(finishTime - arrivalTime).doubleValue();
    }

    public synchronized static long generateId() {
        return counter++;
    }
}