package ir.mesmaeili.lba.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Getter
@Setter
public class SimulationConfig {
    private final static Random rand = new Random();
    @NotNull
    private int serverCount = 200;
    @NotNull
    private static List<Integer> serverMemoryCapacityRange = Arrays.asList(4, 8, 16, 32, 64);
    @NotNull
    private static List<Integer> serverDiskCapacityRange = Arrays.asList(500, 1000, 2000, 4000);
    @NotNull
    private int BSCount = 1000;
    @NotNull
    private int serverMaxQueueSize = 200;
    @NotNull
    private int spaceX;
    @NotNull
    private int spaceY;
    @NotNull
    private double deltaT;
    @NotNull
    private float totalSimulationTime; // in seconds
    @NotNull
    private static Pair<Integer, Integer> taskUniformRange = Pair.of(100, 200);

    public static double getRandomServerMemoryInMB() {
        return 1000. * (serverMemoryCapacityRange).get(rand.nextInt(serverMemoryCapacityRange.size() - 1));
    }

    public static double getRandomServerDiskInGB() {
        return serverDiskCapacityRange.get(rand.nextInt(serverDiskCapacityRange.size() - 1));
    }

    public static double getRandomServerCpuInMhz() {
        return 1000 + 100. * rand.nextInt(40);
    }

    public static double getRandomTaskMemoryInMB() {
        return 100 + 100. * rand.nextInt(5);
    }

    public static double getRandomTaskDiskInMB() {
        return 100 + 100. * rand.nextInt(10);
    }

    public static double getRandomTaskCpuInMhz() {
        return 100 + 100. * rand.nextInt(10);
    }

    public static int getTaskCountUniformRandom() {
        return taskUniformRange.getKey() + rand.nextInt(taskUniformRange.getValue() - taskUniformRange.getKey() + 1);
    }
}
