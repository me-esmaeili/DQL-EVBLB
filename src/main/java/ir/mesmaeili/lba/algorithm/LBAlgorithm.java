package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.config.SimulationState;

public interface LBAlgorithm {

    void dispatchTasksOverServers(SimulationState simulationState);
}
