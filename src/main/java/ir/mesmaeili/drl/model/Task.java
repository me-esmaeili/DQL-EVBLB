package ir.mesmaeili.drl.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Random;
import java.util.UUID;

@Getter
public class Task {
    private final long id;
    private final double memory; // in MB
    private final double disk; // in GB
    private final double cpu; // in MHZ

    @Getter
    private Long arrivalTime;
    @Getter
    @Setter
    private Long processStartTime;
    @Getter
    @Setter
    private Long finishTime;

    public Task() {
        Random rand = new Random();
        this.id = Math.abs(rand.nextLong());
        this.arrivalTime = System.currentTimeMillis();
        this.memory = 100 + 100. * rand.nextInt(10);
        this.disk = 100 + 100. * rand.nextInt(15);
        this.cpu = 1000 + 100. * rand.nextInt(30);
    }
}