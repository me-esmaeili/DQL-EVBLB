package ir.mesmaeili.lba.simulator;

import ir.mesmaeili.lba.algorithm.LBAlgorithm;
import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.EdgeServer;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Scheduler {
    private final LBAlgorithm lbAlgorithm;
    private final SimulationConfig simulationConfig;
    // to use only single thread for each server
    private final Map<EdgeServer, ExecutorService> serverExecutors = new HashMap<>();

    public Scheduler(SimulationConfig simulationConfig,
                     SimulationState simulationState,
                     LBAlgorithm lbAlgorithm) {
        this.lbAlgorithm = lbAlgorithm;
        this.simulationConfig = simulationConfig;
        // Initialize an ExecutorService for each EdgeServer
        for (EdgeServer server : simulationState.getEdgeServers()) {
            serverExecutors.put(server, Executors.newSingleThreadExecutor());
        }
    }

    public void scheduleTasks(SimulationState simulationState) {
        lbAlgorithm.dispatchTasksOverServers(simulationState);

        // calculated metrics with depend on dispatch tasks over algorithm e.g. LBF
        TaskCompleteListener listener = server -> server.calculateMetrics(simulationState.getCurrentRound(), simulationConfig);

        // now execute tasks on servers
        for (EdgeServer edgeServer : simulationState.getEdgeServers()) {
            ExecutorService executor = serverExecutors.get(edgeServer);
            executor.submit(new TaskExecutor(edgeServer, simulationConfig.getDeltaT(), simulationState.getCurrentSimulationTime(),
                    simulationState.getCurrentRound(), listener));
        }
    }

    public void finish() {
        shutdown();
    }

    private void shutdown() {
        // Shutdown all ExecutorServices
        for (ExecutorService executor : serverExecutors.values()) {
            executor.shutdown();
        }
    }
}

