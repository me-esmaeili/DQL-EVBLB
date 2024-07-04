package ir.mesmaeili.drl.model;

import ir.mesmaeili.drl.config.SimulationConfig;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;

import java.util.Random;

@Getter
public class Task {
    @NotNull
    private final long id;
    @NotNull
    private final double memory; // in MB
    @NotNull
    private final double disk; // in GB
    @NotNull
    private final double cpu; // in MHZ

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
        Random rand = new Random();
        this.id = Math.abs(rand.nextLong());
        this.memory = SimulationConfig.getRandomTaskMemoryInMB();
        this.disk = SimulationConfig.getRandomTaskDiskInMB();
        this.cpu = SimulationConfig.getRandomTaskCpuInMhz();
    }
}