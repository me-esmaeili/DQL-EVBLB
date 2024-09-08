package ir.mesmaeili.lba.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Point;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Getter
@Setter
public class SimulationConfig implements Serializable {
    private final static Random rand = new Random();
    private int serverCount = 200;
    private List<Point> serverLocations = new ArrayList<>();
    private List<EdgeServer> edgeServers = new ArrayList<>();
    private List<Integer> serverMemoryCapacityRange = Arrays.asList(4, 8, 16, 32, 64);
    private List<Integer> serverDiskCapacityRange = Arrays.asList(500, 1000, 2000, 4000);

    private float poisonDskSamplingMinDistance; // Minimum distance between points
    private int BSCount = 1000;
    private int serverMaxQueueSize = 200;
    private int spaceX;
    private int spaceY;
    private double deltaT;
    private float totalSimulationTime; // in seconds
    private ImmutablePair<Integer, Integer> taskUniformRange = new ImmutablePair<>(100, 200);

    public void addServer(EdgeServer server) {
        edgeServers.add(server);
    }

    @JsonIgnore
    public double getRandomServerMemoryInMB() {
        return 1000. * (serverMemoryCapacityRange).get(rand.nextInt(serverMemoryCapacityRange.size() - 1));
    }

    @JsonIgnore
    public double getRandomServerDiskInGB() {
        return serverDiskCapacityRange.get(rand.nextInt(serverDiskCapacityRange.size() - 1));
    }

    @JsonIgnore
    public double getRandomServerCpuInMhz() {
        return 1000 + 100. * rand.nextInt(40);
    }

    @JsonIgnore
    public double getRandomTaskMemoryInMB() {
        return 400 + 100. * rand.nextDouble(1);
    }

    @JsonIgnore
    public double getRandomTaskDiskInMB() {
        return 500 + 100. * rand.nextDouble(1);
    }

    @JsonIgnore
    public double getRandomTaskCpuInMhz() {
        return 500 + 100. * rand.nextDouble(5);
    }

    @JsonIgnore
    public int getTaskCountUniformRandom() {
        return taskUniformRange.getKey() + rand.nextInt(taskUniformRange.getValue() - taskUniformRange.getKey() + 1);
    }
}