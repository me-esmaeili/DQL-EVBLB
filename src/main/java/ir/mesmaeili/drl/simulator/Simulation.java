package ir.mesmaeili.drl.simulator;

import ir.mesmaeili.drl.alg.LBAlgorithm;
import ir.mesmaeili.drl.config.SimulationConfig;
import ir.mesmaeili.drl.config.SimulationState;
import ir.mesmaeili.drl.model.EdgeServer;
import ir.mesmaeili.drl.model.Task;
import ir.mesmaeili.drl.statistic.SimulationStatisticResult;
import ir.mesmaeili.drl.util.VoronoiUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

@Slf4j
@Getter
public class Simulation {
    private final Scheduler scheduler;
    private final Random random;
    private final SimulationConfig simulationConfig;
    private final SimulationStatisticResult simulationStatisticResult;
    private final VoronoiUtils voronoiUtils;
    private final SimulationState simulationState;
    private final List<Coordinate> points;

    public Simulation(LBAlgorithm lbAlgorithm, SimulationConfig simulationConfig) {
        this.simulationConfig = simulationConfig;
        this.simulationStatisticResult = new SimulationStatisticResult(System.currentTimeMillis(), this.simulationConfig);
        this.random = new Random();
        this.simulationState = new SimulationState();
        this.voronoiUtils = new VoronoiUtils();

        points = voronoiUtils.generatePoints(this.simulationConfig.getServerCount(), this.simulationConfig.getSpaceX(), this.simulationConfig.getSpaceY());
        int i = 1;
        for (Coordinate point : points) {
            EdgeServer edgeServer = new EdgeServer(i++, point, this.simulationConfig);
            simulationState.addServer(edgeServer);
            simulationStatisticResult.addServer(edgeServer);
        }
        this.scheduler = new Scheduler(simulationConfig, this.simulationState, lbAlgorithm);
    }

    public SimulationState run() {
        double totalTime = 0.;
        int round = 1;
        double totalSimulationTime = simulationConfig.getTotalSimulationTime();
        while (totalSimulationTime >= totalTime) {
            log.info("Start to round {} at time:{}", round, totalTime);
            simulationState.addTasks(generateTasks());
            log.info("Tasks are generated at time " + totalTime);
            scheduler.scheduleTasks(this.simulationState, totalTime);
            totalTime += simulationConfig.getDeltaT();
            round++;
            try {
                Thread.sleep((long) (simulationConfig.getDeltaT() * 1000)); // wait to next round execution
            } catch (InterruptedException e) {
                log.error("Error:", e);
            }
            log.info("finish round {} at time: {}", round, totalTime);
        }
        simulationState.setTotalRound(round);
        scheduler.shutdown();
        return this.simulationState;
    }

    private Queue<Task> generateTasks() {
        Queue<Task> taskQueue = new LinkedList<>();
        int numberOfTasks = getPoissonRandom(getSimulationConfig().getTaskPoissonMean());
        // First, assign one task to each point
        for (int i = 0; i < Math.min(numberOfTasks, this.points.size()); i++) {
            Task task = new Task();
            double arrivalTime = random.nextDouble() * simulationConfig.getDeltaT();
            task.setArrivalTime(arrivalTime);
            task.setLocation(points.get(i));
            taskQueue.add(task);
            simulationStatisticResult.addTask(task);
        }
        // If there are more tasks left, distribute them randomly among the points
        for (int i = points.size(); i < numberOfTasks; i++) {
            Task task = new Task();
            double arrivalTime = random.nextDouble() * simulationConfig.getDeltaT();
            task.setArrivalTime(arrivalTime);
            Coordinate location = voronoiUtils.generatePoint(simulationConfig.getSpaceX(), simulationConfig.getSpaceY());
            task.setLocation(location);
            taskQueue.add(task);
            simulationStatisticResult.addTask(task);
        }
        return taskQueue;
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
}
