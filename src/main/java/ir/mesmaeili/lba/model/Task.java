package ir.mesmaeili.lba.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ir.mesmaeili.lba.config.SimulationConfig;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task implements Serializable {
    private static long counter = 1;
    @EqualsAndHashCode.Include
    private final long id;
    private final double memory; // in MB
    private final double disk; // in GB
    private final double cpu; // in MHZ
    @Getter
    @Setter
    private double remainingCpu = 0.; // in MHZ is used to store remaining for next round of LB execution
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
    private Point location;

    public Task(SimulationConfig simulationConfig) {
        this.id = generateId();
        this.memory = simulationConfig.getRandomTaskMemoryInMB();
        this.disk = simulationConfig.getRandomTaskDiskInMB();
        this.cpu = simulationConfig.getRandomTaskCpuInMhz();
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