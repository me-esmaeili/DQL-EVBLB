package ir.mesmaeili.drl.simulator;

import ir.mesmaeili.drl.model.EdgeServer;

public class TaskExecutor implements Runnable {
    private final EdgeServer edgeServer;
    private final double deltaT;
    private final TaskCompleteListener listener;

    public TaskExecutor(EdgeServer edgeServer, double deltaT, TaskCompleteListener listener) {
        this.edgeServer = edgeServer;
        this.deltaT = deltaT;
        this.listener = listener;
    }

    @Override
    public void run() {
        edgeServer.executeTasks(deltaT);
        listener.onTaskComplete(edgeServer);
    }
}