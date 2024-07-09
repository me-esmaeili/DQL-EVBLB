package ir.mesmaeili.lba.simulator;

import ir.mesmaeili.lba.algorithm.LBAlgorithm;
import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import ir.mesmaeili.lba.statistic.SimulationStatisticResult;
import ir.mesmaeili.lba.util.VoronoiUtils;
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

    public Simulation(LBAlgorithm lbAlgorithm, SimulationConfig simulationConfig, SimulationState simulationState) {
        this.simulationConfig = simulationConfig;
        this.simulationStatisticResult = new SimulationStatisticResult(System.currentTimeMillis(), this.simulationConfig);
        this.random = new Random();
        this.simulationState = simulationState;
        this.voronoiUtils = new VoronoiUtils();

        for (int i = 1; i <= simulationConfig.getServerCount(); i++) {
            EdgeServer edgeServer = new EdgeServer(i, this.simulationConfig);
            this.simulationState.addServer(edgeServer);
            simulationStatisticResult.addServer(edgeServer);
        }
        lbAlgorithm.setServerLocations(this.simulationState.getEdgeServers());
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
        int numberOfTasks = SimulationConfig.getTaskCountUniformRandom();
        List<Coordinate> points = simulationState.getEdgeServers().stream().map(EdgeServer::getLocation).toList();
        // First, assign one task to each point
        for (int i = 0; i < Math.min(numberOfTasks, points.size()); i++) {
            Task task = new Task();
            // task arrival time in range [currentSimulationTime, currentSimulationTime+ U(0,DeltaT)
            double arrivalTime = currentSimulationTime + (random.nextDouble() * simulationConfig.getDeltaT());
            task.setArrivalTime(arrivalTime);
            task.setLocation(voronoiUtils.generateRndPoint(simulationConfig.getSpaceX(), simulationConfig.getSpaceY()));
            taskQueue.add(task);
        }
        // If there are more tasks left, distribute them randomly among the points
        for (int i = points.size(); i < numberOfTasks; i++) {
            Task task = new Task();
            double arrivalTime = random.nextDouble() * simulationConfig.getDeltaT();
            task.setArrivalTime(arrivalTime);
            Coordinate location = voronoiUtils.generateRndPoint(simulationConfig.getSpaceX(), simulationConfig.getSpaceY());
            task.setLocation(location);
            taskQueue.add(task);
        }
        return taskQueue;
    }
}
