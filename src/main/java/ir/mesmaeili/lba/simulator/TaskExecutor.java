package ir.mesmaeili.lba.simulator;

import ir.mesmaeili.lba.model.EdgeServer;

public class TaskExecutor implements Runnable {
    private final EdgeServer edgeServer;
    private final double deltaT;
    private final double currentSimulationTime;
    private final int currentSimulationRound;
    private final TaskCompleteListener listener;

    public TaskExecutor(EdgeServer edgeServer, double deltaT, double currentSimulationTime, int currentSimulationRound, TaskCompleteListener listener) {
        this.edgeServer = edgeServer;
        this.deltaT = deltaT;
        this.currentSimulationTime = currentSimulationTime;
        this.currentSimulationRound = currentSimulationRound;
        this.listener = listener;
    }

    @Override
    public void run() {
        edgeServer.executeTasks(deltaT, currentSimulationTime, currentSimulationRound);
        listener.onTaskComplete(edgeServer);
    }
}