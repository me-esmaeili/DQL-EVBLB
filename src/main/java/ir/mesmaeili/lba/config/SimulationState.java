package ir.mesmaeili.lba.config;

import ir.mesmaeili.lba.model.CloudServer;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Getter
@Setter
public class SimulationState {
    private int currentRound = 0;
    private float currentSimulationTime = 0;
    private List<EdgeServer> edgeServers = new ArrayList<>();
    private Queue<Task> tasks = new LinkedList<>();
    private CloudServer cloudServer = new CloudServer();

    public void addServer(EdgeServer server) {
        edgeServers.add(server);
    }

    public void addTasks(Queue<Task> task) {
        tasks.addAll(task);
    }
}
