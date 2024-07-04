package ir.mesmaeili.drl.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Random;
import java.util.UUID;

@Getter
public class Task {
    private final UUID id;
    private final double memory;
    private final double disk;
    private final double cpu;

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
        id = UUID.randomUUID();
        arrivalTime = System.currentTimeMillis();
        memory = 100 + 100 * rand.nextInt(10);
        disk = 500 + 100 * rand.nextInt(15);
        cpu = 1000 + 100 * rand.nextInt(30);
    }
}