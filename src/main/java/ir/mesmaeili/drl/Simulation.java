package ir.mesmaeili.drl;

import ir.mesmaeili.drl.model.Server;
import ir.mesmaeili.drl.model.Task;
import ir.mesmaeili.drl.util.VoronoiDiagram;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;
import org.locationtech.jts.geom.Coordinate;

import java.util.*;

@Slf4j
@Getter
public class Simulation {
    private final List<Server> servers;
    private final Queue<Task> taskQueue;
    private final Scheduler scheduler;
    private final Random random;
    private final int spaceX;
    private final int spaceY;
    private final double R_Delta;  // in seconds
    private static final double totalSimulationTime = 20.; // in seconds
    private static final double taskPoissonMean = 10;
    private SimulationStatisticResult simulationStatisticResult;

    public Simulation(int serverCount, double R_Delta, int spaceX, int spaceY) {
        this.servers = new ArrayList<>();
        this.taskQueue = new LinkedList<>();
        this.scheduler = new Scheduler();
        this.random = new Random();
        this.simulationStatisticResult = new SimulationStatisticResult();
        this.spaceX = spaceX;
        this.spaceY = spaceY;
        this.R_Delta = R_Delta;
        VoronoiDiagram voronoiDiagram = new VoronoiDiagram();
        List<Coordinate> points = voronoiDiagram.generatePoints(serverCount, spaceX, spaceY);
        int i = 1;
        for (Coordinate point : points) {
            Server server = new Server(i++, point.getX(), point.getY());
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
            scheduler.scheduleTasks(servers, taskQueue, R_Delta);
            totalTime += R_Delta;
            round++;
            try {
                Thread.sleep((long) (R_Delta * 1000)); // wait to next round execution
            } catch (InterruptedException e) {
                log.error("Error:", e);
            }
            log.info("finish round {} at time: {}", round, totalTime);
        }
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        log.info("Start simulation at {}", new Date());
        Simulation simulation = new Simulation(10, 3.0, 100, 100);
        simulation.run();
        log.info("Finish simulation at {}", new Date());
    }
}
