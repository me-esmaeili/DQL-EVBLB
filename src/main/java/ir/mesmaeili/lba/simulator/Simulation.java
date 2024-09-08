package ir.mesmaeili.lba.simulator;

import ir.mesmaeili.lba.algorithm.LBAlgorithm;
import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Point;
import ir.mesmaeili.lba.model.Task;
import ir.mesmaeili.lba.statistic.SimulationStatisticResult;
import ir.mesmaeili.lba.util.VoronoiUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.PoissonDistribution;

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
    private final SimulationState simulationState;

    public Simulation(LBAlgorithm lbAlgorithm, SimulationConfig simulationConfig, SimulationState simulationState) {
        this.simulationConfig = simulationConfig;
        this.simulationStatisticResult = new SimulationStatisticResult(System.currentTimeMillis(), this.simulationConfig);
        this.simulationStatisticResult.setLbAlgorithm(lbAlgorithm);
        this.random = new Random();
        this.simulationState = simulationState;

        for (EdgeServer edgeServer : simulationConfig.getEdgeServers()) {
            this.simulationState.addServer(edgeServer);
            this.simulationStatisticResult.addServer(edgeServer);
        }
        this.scheduler = new Scheduler(simulationConfig, this.simulationState, lbAlgorithm);
    }

    public SimulationStatisticResult run() {
        float currentSimulationTime = 0f;
        int curentRound = 1;
        double totalSimulationTime = simulationConfig.getTotalSimulationTime();
        while (totalSimulationTime >= currentSimulationTime) {
            log.info("Start to round {} at time:{}", curentRound, currentSimulationTime);
            this.simulationState.setCurrentSimulationTime(currentSimulationTime); // store simulation time in range [0,totalRound*DeltaT]

            // generate tasks
            Queue<Task> tasks = generateTasks(currentSimulationTime);
            this.simulationState.addTasks(tasks);
            this.simulationState.setRoundTasks(tasks);
            simulationStatisticResult.addTasks(tasks);
            log.info("Tasks are generated at time " + currentSimulationTime);

            // schedule tasks over servers
            scheduler.scheduleTasks(this.simulationState);
            currentSimulationTime += simulationConfig.getDeltaT();
            curentRound++;
            try {
                Thread.sleep((long) (simulationConfig.getDeltaT() * 1000)); // wait to next round execution
            } catch (InterruptedException e) {
                log.error("Error:", e);
            }
            log.info("finish round {} at time: {}", curentRound, currentSimulationTime);
            this.simulationState.setCurrentRound(curentRound);
        }
        simulationStatisticResult.setTotalRounds(curentRound);
        scheduler.finish();
        simulationStatisticResult.setFinishTime(System.currentTimeMillis());
        return this.simulationStatisticResult;
    }

    private Queue<Task> generateTasks(double currentSimulationTime) {
        Queue<Task> taskQueue = new LinkedList<>();
        PoissonDistribution poisson = new PoissonDistribution(random.nextDouble() * simulationConfig.getDeltaT());
        int numberOfTasks = simulationConfig.getTaskCountUniformRandom();
        List<Point> points = simulationState.getEdgeServers().stream().map(EdgeServer::getLocation).toList();
        // First, assign one task to each point
        for (int i = 0; i < Math.min(numberOfTasks, points.size()); i++) {
            Task task = new Task(simulationConfig);
            // task arrival time in range [currentSimulationTime, currentSimulationTime+ U(0,DeltaT)
            double arrivalTime = currentSimulationTime + poisson.sample();
            task.setArrivalTime(arrivalTime);
            task.setLocation(VoronoiUtils.generateRndPoint(simulationConfig.getSpaceX(), simulationConfig.getSpaceY()));
            taskQueue.add(task);
        }
        // If there are more tasks left, distribute them randomly among the points
        for (int i = points.size(); i < numberOfTasks; i++) {
            Task task = new Task(simulationConfig);
            double arrivalTime = random.nextDouble() * simulationConfig.getDeltaT();
            task.setArrivalTime(arrivalTime);
            Point location = VoronoiUtils.generateRndPoint(simulationConfig.getSpaceX(), simulationConfig.getSpaceY());
            task.setLocation(location);
            taskQueue.add(task);
        }
        return taskQueue;
    }
}
