package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import ir.mesmaeili.lba.util.VoronoiUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;

import java.util.*;

@Slf4j
public class EvblbAlgorithm implements LBAlgorithm {
    protected final SimulationConfig simulationConfig;
    protected SimulationState simulationState;
    protected final EvblbConfig config;

    // Constructor
    public EvblbAlgorithm(SimulationConfig simulationConfig, EvblbConfig config) {
        this.simulationConfig = simulationConfig;
        this.config = config;
    }

    @Override
    public synchronized void dispatchTasksOverServers(SimulationState simulationState) {
        this.simulationState = simulationState;
        double Mc = getMaxCpuResource(simulationState.getEdgeServers());
        double Mm = getMaxMemResource(simulationState.getEdgeServers());
        double Md = getMaxDiskResource(simulationState.getEdgeServers());

        // Assign tasks to cloud server if they exceed max resources of edge servers
        for (Task task : simulationState.getRoundTasks()) {
            if (task.getCpu() > Mc || task.getMemory() > Mm || task.getDisk() > Md) {
                assignToLeastLoadedCloudServer(task);
            }
        }
        // Assign remaining tasks to edge servers
        NeighborSelector neighborSelector = getNeighborSelector();
        for (EdgeServer e_i : simulationState.getEdgeServers()) {
            // find server neighbors
            Set<EdgeServer> neighbors = neighborSelector.findNeighbors(e_i, simulationState.getEdgeServers(), config.getPSI());

            // find optimal neighbor server
            EdgeServer e_k = findOptimalNeighbor(neighbors);
            log.info("Select neighbor Server {} to send tasks from source Server {}", e_k.getId(), e_i.getId());

            // find all tasks of source server
            Geometry serverRegion = VoronoiUtils.getRegion(config.getVoronoiTessellation(), e_i);

            // send all source servers to selected neighbor server to be executed
            assignTasksInRegionToServer(serverRegion, simulationState.getRoundTasks(), e_k);
        }
    }

    @Override
    public NeighborSelector getNeighborSelector() {
        return new EvblbBaseNeighborSelection();
    }

    @Override
    public EdgeServer findOptimalNeighbor(Collection<EdgeServer> servers) {
        if (servers.isEmpty()) {
            return null;
        }
        EdgeServer serverWithMaxResource = null;
        double maxResource = Double.NEGATIVE_INFINITY;

        for (EdgeServer edgeServer : servers) {
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

    @Override
    public String resultDirPath() {
        return "/evblb";
    }

    protected double getMaxCpuResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getProcessingCapacity).max().orElse(0);
    }

    protected double getMaxMemResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getMemoryCapacity).max().orElse(0);
    }

    protected double getMaxDiskResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getDiskCapacity).max().orElse(0);
    }

    protected void assignToLeastLoadedCloudServer(Task task) {
        this.simulationState.getCloudServer().addTask(task);
    }

    protected void assignTasksInRegionToServer(Geometry region, Queue<Task> allTasks, EdgeServer server) {
        List<Task> regionTasks = VoronoiUtils.getRegionTasks(region, allTasks);
        for (Task task : regionTasks) {
            server.addTask(task, this.simulationState, this.simulationConfig);
        }
        log.info("Assign {} tasks to related region in server {}", regionTasks.size(), server.getId());
    }
}
