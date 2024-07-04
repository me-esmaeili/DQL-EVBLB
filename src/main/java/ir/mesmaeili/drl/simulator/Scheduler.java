package ir.mesmaeili.drl.simulator;

import ir.mesmaeili.drl.alg.LBAlgorithm;
import ir.mesmaeili.drl.config.SimulationConfig;
import ir.mesmaeili.drl.config.SimulationState;
import ir.mesmaeili.drl.model.EdgeServer;
import ir.mesmaeili.drl.model.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
public class Scheduler {
    private final Queue<Task> blockedQueue;
    private final LBAlgorithm lbAlgorithm;
    private final SimulationConfig simulationConfig;

    public Scheduler(SimulationConfig simulationConfig, LBAlgorithm lbAlgorithm) {
        this.lbAlgorithm = lbAlgorithm;
        this.simulationConfig = simulationConfig;
        this.blockedQueue = new LinkedList<>();
    }

    public void scheduleTasks(SimulationState simulationState) {
        lbAlgorithm.dispatchTasksOverServers(simulationState);

        // now execute tasks on servers
        for (EdgeServer edgeServer : simulationState.getEdgeServers()) {
            new Thread(() -> edgeServer.executeTasks(simulationConfig.getDeltaT())).start();
        }
    }

    public static double calculateLBF(List<EdgeServer> edgeServers, double averageCpuUtilization) {
        double sumSquaredDifferences = 0;
        for (EdgeServer edgeServer : edgeServers) {
            double ui = edgeServer.calculateCpuUtilization(edgeServer.getDeltaT());
            sumSquaredDifferences += Math.pow(ui - averageCpuUtilization, 2);
        }
        return Math.sqrt(sumSquaredDifferences / edgeServers.size());
    }
}

