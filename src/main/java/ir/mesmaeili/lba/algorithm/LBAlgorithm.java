package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.EdgeServer;

import java.util.List;

public interface LBAlgorithm {

    void dispatchTasksOverServers(SimulationState simulationState);

    NeighborSelector getNeighborSelector();

    void setServerLocations(List<EdgeServer> servers);
}
