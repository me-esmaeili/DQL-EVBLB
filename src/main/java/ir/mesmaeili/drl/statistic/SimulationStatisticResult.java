package ir.mesmaeili.drl.statistic;

import ir.mesmaeili.drl.config.SimulationConfig;
import ir.mesmaeili.drl.model.EdgeServer;
import ir.mesmaeili.drl.model.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SimulationStatisticResult {
    private long startTime;
    private double R_Delta;
    private List<Task> Tasks = new ArrayList<>();
    private List<EdgeServer> edgeServers = new ArrayList<>();

    public SimulationStatisticResult(long startTime, SimulationConfig config) {
        this.R_Delta = config.getR_Delta();
    }

    public void addTask(Task task) {
        Tasks.add(task);
    }

    public void addServer(EdgeServer edgeServer) {
        edgeServers.add(edgeServer);
    }
}
