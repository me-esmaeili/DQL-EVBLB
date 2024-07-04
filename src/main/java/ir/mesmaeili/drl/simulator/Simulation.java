package ir.mesmaeili.drl.simulator;

import ir.mesmaeili.drl.config.SimulationConfig;
import ir.mesmaeili.drl.model.Server;
import ir.mesmaeili.drl.model.Task;
import ir.mesmaeili.drl.statistic.SimulationStatisticResult;
import ir.mesmaeili.drl.util.VoronoiDiagram;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;

import java.util.*;

@Slf4j
@Getter
public class Simulation {
    private final List<Server> servers;
    private final Queue<Task> taskQueue;
    private final Scheduler scheduler;
    private final Random random;
    private final SimulationConfig config;
    private static final double totalSimulationTime = 20.; // in seconds
    private static final double taskPoissonMean = 10;
    private final SimulationStatisticResult simulationStatisticResult;

    public Simulation(SimulationConfig config) {
        this.config = config;
        this.servers = new ArrayList<>();
        this.taskQueue = new LinkedList<>();
        this.scheduler = new Scheduler();
        this.random = new Random();
        this.simulationStatisticResult = new SimulationStatisticResult(System.currentTimeMillis(), config);
        VoronoiDiagram voronoiDiagram = new VoronoiDiagram();
        List<Coordinate> points = voronoiDiagram.generatePoints(config.getServerCount(), config.getSpaceX(), config.getSpaceY());
        int i = 1;
        for (Coordinate point : points) {
            Server server = new Server(i++, point.getX(), point.getY(), config.getServerCount());
            servers.add(server);
            simulationStatisticResult.addServer(server);
        }
    }

    private void generateTasks() {
        int numberOfTasks = getPoissonRandom(taskPoissonMean);
        for (int i = 0; i < numberOfTasks; i++) {
            Task task = new Task();
            taskQueue.add(task);
            simulationStatisticResult.addTask(task);
        }
    }

    private int getPoissonRandom(double mean) {
        double L = Math.exp(-mean);
        int k = 0;
        double p = 1.0;
        do {
            p = p * random.nextDouble();
            k++;
        } while (p > L);
        return k - 1;
    }

    public void run() {
        double totalTime = 0.;
        long round = 1L;
        while (totalSimulationTime >= totalTime) {
            log.info("Start to round {} at time:{}", round, totalTime);
            generateTasks();
            log.info("Tasks are generated at time " + totalTime);
            scheduler.scheduleTasks(servers, taskQueue, config.getR_Delta());
            totalTime += config.getR_Delta();
            round++;
            try {
                Thread.sleep((long) (config.getR_Delta() * 1000)); // wait to next round execution
            } catch (InterruptedException e) {
                log.error("Error:", e);
            }
            log.info("finish round {} at time: {}", round, totalTime);
        }
    }
}