package ir.mesmaeili.drl.alg;

import ir.mesmaeili.drl.config.SimulationConfig;
import ir.mesmaeili.drl.config.SimulationState;
import ir.mesmaeili.drl.model.EdgeServer;
import ir.mesmaeili.drl.model.Task;
import ir.mesmaeili.drl.util.ServerNeighbors;
import ir.mesmaeili.drl.util.VoronoiUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;

import java.util.List;
import java.util.Queue;

@Slf4j
public class EVBLBAlgorithm implements LBAlgorithm {
    private final SimulationConfig simulationConfig;
    private SimulationState simulationState;
    private final EvblbConfig config;
    private final VoronoiUtils vd = new VoronoiUtils();

    // Constructor
    public EVBLBAlgorithm(SimulationConfig simulationConfig, EvblbConfig config) {
        this.simulationConfig = simulationConfig;
        this.config = config;
    }

    @Override
    public void dispatchTasksOverServers(SimulationState simulationState) {
        this.simulationState = simulationState;
        double Mc = getMaxCpuResource(simulationState.getEdgeServers());
        double Mm = getMaxMemResource(simulationState.getEdgeServers());
        double Md = getMaxDiskResource(simulationState.getEdgeServers());

        // Assign tasks to cloud server if they exceed max resources of edge servers
        for (Task task : simulationState.getTasks()) {
            if (task.getCpu() > Mc || task.getMemory() > Mm || task.getDisk() > Md) {
                assignToLeastLoadedCloudServer(task);
            }
        }
        // Assign remaining tasks to edge servers
        ServerNeighbors serverNeighbors;
        for (EdgeServer e_i : simulationState.getEdgeServers()) {
            serverNeighbors = new ServerNeighbors(e_i);
            List<EdgeServer> neighbors = serverNeighbors.findNeighbors(simulationState.getEdgeServers());
            EdgeServer e_k = getServerWithMaxRemainingResource(neighbors);
            assignTasksInRegionToServer(vd.getRegion(config.getVoronoiTessellation(), e_i), simulationState.getTasks(), e_k);
        }
    }

    private double getMaxCpuResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getCpu).max().getAsDouble();
    }

    private double getMaxMemResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getMemory).max().getAsDouble();
    }

    private double getMaxDiskResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getDisk).max().getAsDouble();
    }

    private void assignToLeastLoadedCloudServer(Task task) {
        this.simulationState.getCloudServer().addTask(task);
    }

    private EdgeServer getServerWithMaxRemainingResource(List<EdgeServer> servers) {
        if (servers.isEmpty()) {
            return null;
        }
        EdgeServer serverWithMaxResource = servers.get(0);
        double maxResource = serverWithMaxResource.calculateRemainingResource(
                config.getAlpha(),
                config.getBeta(),
                config.getGamma(),
                simulationConfig.getDeltaT());
        for (int i = 1; i < servers.size(); i++) {
            EdgeServer edgeServer = servers.get(i);
            double currentResource = edgeServer.calculateRemainingResource(
                    config.getAlpha(),
                    config.getBeta(),
                    config.getGamma(),
                    simulationConfig.getDeltaT());
            if (currentResource > maxResource) {
                serverWithMaxResource = edgeServer;
                maxResource = currentResource;
            }
        }
        return serverWithMaxResource;
    }

    private void assignTasksInRegionToServer(Geometry region,
                                             Queue<Task> allTasks,
                                             EdgeServer server) {
        List<Task> regionTasks = vd.getRegionTasks(region, allTasks);
        for (Task task : regionTasks) {
            server.addTask(task);
            log.info("Assign task {} in region {} to server {}",
                    task.getId(),
                    region.getBoundary(),
                    server.getId());
        }
    }
}
