package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import ir.mesmaeili.lba.util.EvblbBaseNeighborSelection;
import ir.mesmaeili.lba.util.VoronoiUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Slf4j
public class EvblbAlgorithm implements LBAlgorithm {
    private final SimulationConfig simulationConfig;
    private SimulationState simulationState;
    private final EvblbConfig config;
    private final VoronoiUtils vu = new VoronoiUtils();

    // Constructor
    public EvblbAlgorithm(SimulationConfig simulationConfig, EvblbConfig config) {
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
        NeighborSelector neighborSelector = getNeighborSelector();
        for (EdgeServer e_i : simulationState.getEdgeServers()) {
            List<EdgeServer> neighbors = neighborSelector.findNeighbors(e_i, simulationState.getEdgeServers());
            EdgeServer e_k = getServerWithMaxRemainingResource(neighbors);
            Geometry serverRegion = vu.getRegion(config.getVoronoiTessellation(), e_i);
            assignTasksInRegionToServer(serverRegion, simulationState.getTasks(), e_k);
        }
    }

    @Override
    public NeighborSelector getNeighborSelector() {
        return new EvblbBaseNeighborSelection();
    }

    @Override
    public void setServerLocations(List<EdgeServer> servers) {
        List<Coordinate> centers = vu.getVoronoiCenters(config.getVoronoiTessellation());
        if (servers.size() != centers.size()) {
            throw new RuntimeException("Mismatch Voronoi diagram and Server Count");
        }
        for (int i = 0; i < servers.size(); i++) {
            servers.get(i).setLocation(centers.get(i));
        }
    }

    private double getMaxCpuResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getProcessingCapacity).max().orElse(0);
    }

    private double getMaxMemResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getMemoryCapacity).max().orElse(0);
    }

    private double getMaxDiskResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getDiskCapacity).max().orElse(0);
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

    private List<Task> assignTasksInRegionToServer(Geometry region, Queue<Task> allTasks, EdgeServer server) {
        List<Task> allocatedTasks = new ArrayList<>();
        List<Task> regionTasks = vu.getRegionTasks(region, allTasks);
        for (Task task : regionTasks) {
            server.addTask(task, this.simulationState);
            allocatedTasks.add(task);
        }
        log.info("Assign {} tasks to related region in server {}", regionTasks.size(), server.getId());
        return allocatedTasks;
    }
}
