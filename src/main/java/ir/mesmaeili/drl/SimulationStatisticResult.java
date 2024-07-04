package ir.mesmaeili.drl;

import ir.mesmaeili.drl.model.Server;
import ir.mesmaeili.drl.model.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SimulationStatisticResult {
    private int startTime;
    private double R_Delta;
    private List<Task> Tasks = new ArrayList<>();
    private List<Server> servers = new ArrayList<>();

    public void addTask(Task task) {
        Tasks.add(task);
    }

    public void addServer(Server server) {
        servers.add(server);
    }
}
