package ir.mesmaeili.drl.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
public class Task {
    private final long id;
    private final double memory; // in MB
    private final double disk; // in GB
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

    public Task() {
        Random rand = new Random();
        this.id = Math.abs(rand.nextLong());
        this.memory = 100 + 100. * rand.nextInt(10);
        this.disk = 100 + 100. * rand.nextInt(15);
        this.cpu = 1000 + 100. * rand.nextInt(30);
    }
}