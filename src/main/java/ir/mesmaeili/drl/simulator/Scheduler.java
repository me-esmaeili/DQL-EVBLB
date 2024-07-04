package ir.mesmaeili.drl.simulator;

import ir.mesmaeili.drl.alg.LBAlgorithm;
import ir.mesmaeili.drl.config.SimulationConfig;
import ir.mesmaeili.drl.config.SimulationState;
import ir.mesmaeili.drl.model.EdgeServer;
import ir.mesmaeili.drl.model.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Scheduler {
    private final Queue<Task> blockedQueue;
    private final LBAlgorithm lbAlgorithm;
    private final SimulationConfig simulationConfig;
    private final SimulationState simulationState;
    private Map<EdgeServer, ExecutorService> serverExecutors = new HashMap<>();

    public Scheduler(SimulationConfig simulationConfig,
                     SimulationState simulationState,
                     LBAlgorithm lbAlgorithm) {
        this.lbAlgorithm = lbAlgorithm;
        this.simulationConfig = simulationConfig;
        this.simulationState = simulationState;
        this.blockedQueue = new LinkedList<>();
        // Initialize an ExecutorService for each EdgeServer
        for (EdgeServer server : simulationState.getEdgeServers()) {
            serverExecutors.put(server, Executors.newSingleThreadExecutor());
        }
    }

    public void scheduleTasks(SimulationState simulationState, double time) {
        lbAlgorithm.dispatchTasksOverServers(simulationState);
        TaskCompleteListener listener = server -> server.calculateMetrics(time);

        // now execute tasks on servers
        for (EdgeServer edgeServer : simulationState.getEdgeServers()) {
            ExecutorService executor = serverExecutors.get(edgeServer);
            executor.submit(new TaskExecutor(edgeServer, simulationConfig.getDeltaT(), listener));
        }
    }

    public void finish() {
        shutdown();
    }

    public void shutdown() {
        // Shutdown all ExecutorServices
        for (ExecutorService executor : serverExecutors.values()) {
            executor.shutdown();
        }
    }

    public static double calculateLBF(List<EdgeServer> edgeServers, double averageCpuUtilization) {
        double sumSquaredDifferences = 0;
        for (EdgeServer edgeServer : edgeServers) {
            double ui = edgeServer.getDiskCapacity();
            sumSquaredDifferences += Math.pow(ui - averageCpuUtilization, 2);
        }
        return Math.sqrt(sumSquaredDifferences / edgeServers.size());
    }
}

