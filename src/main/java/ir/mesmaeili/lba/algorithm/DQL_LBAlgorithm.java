package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;

import java.util.Date;
import java.util.List;

@Slf4j
public class DQL_LBAlgorithm extends EvblbAlgorithm {
    private final DeepQLearning deepQLearning;
    private final DQLNeighborSelection neighborSelection;

    public DQL_LBAlgorithm(SimulationConfig simulationConfig,
                           EvblbConfig config,
                           int lbfCount,
                           int radiusCount) {
        super(simulationConfig, config);
        this.deepQLearning = new DeepQLearning(lbfCount, radiusCount);
        this.neighborSelection = new DQLNeighborSelection();
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

        // select optimal radius by DQL algorithm
        double lbf = simulationState.calculateLBF(simulationConfig.getDeltaT());
        int radius = deepQLearning.selectOptimalRadius(lbf);
        log.info("Select Radius {} as optimal radius in round {} at time: {}",
                radius, simulationState.getCurrentRound(), new Date());

        for (EdgeServer e_i : simulationState.getEdgeServers()) {
            neighborSelection.updateRadius(radius);

            // now, with optimal radius, select neighbors same as EVBLB
            List<EdgeServer> neighbors = neighborSelector.findNeighbors(e_i, simulationState.getEdgeServers());

            // find neighbor same as EVBLB
            EdgeServer e_k = super.findOptimalNeighbor(neighbors);

            // find region of desired server same as EVBLB
            Geometry serverRegion = vu.getRegion(config.getVoronoiTessellation(), e_i);

            // finally, assign all tasks of server to it optimal neighbor
            assignTasksInRegionToServer(serverRegion, simulationState.getRoundTasks(), e_k);
        }
    }

    @Override
    public NeighborSelector getNeighborSelector() {
        return neighborSelection;
    }

    @Override
    public String resultDirPath() {
        return "/dql";
    }
}
