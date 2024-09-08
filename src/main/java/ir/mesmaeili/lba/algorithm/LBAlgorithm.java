package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.EdgeServer;

import java.util.Collection;

public interface LBAlgorithm {

    void dispatchTasksOverServers(SimulationState simulationState);

    NeighborSelector getNeighborSelector();

    EdgeServer findOptimalNeighbor(Collection<EdgeServer> servers);

    default void afterScheduledProcessing() {
        return;
    }

    String resultDirPath();
}
