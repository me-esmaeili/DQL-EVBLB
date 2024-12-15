package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import lombok.extern.slf4j.Slf4j;

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
    public float calsulateNeighborRadius(SimulationState simulationState) {
        // select optimal radius by DQL algorithm
        double lbf = simulationState.calculateLBF(simulationConfig.getDeltaT());
        return deepQLearning.selectOptimalRadius(lbf) * 1.0f;
    }

    @Override
    public NeighborSelector getNeighborSelector() {
        return neighborSelection;
    }

    @Override
    public String resultDirPath() {
        return "/dql";
    }

    @Override
    public String getConfigStatus() {
        return "";
    }
}
