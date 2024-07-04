package ir.mesmaeili.drl.config;

import ir.mesmaeili.drl.model.CloudServer;
import ir.mesmaeili.drl.model.EdgeServer;
import ir.mesmaeili.drl.model.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Getter
@Setter
public class SimulationState {
    private int currentRound;
    private int currentTime;
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
