package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.Task;

import java.util.Queue;

public class LSTMAlgorithm extends EvblbAlgorithm {

    private final PsiPredictionClient predictionClient;

    // Constructor
    public LSTMAlgorithm(SimulationConfig simulationConfig, EvblbConfig config) {
        super(simulationConfig, config);
        predictionClient = new PsiPredictionClient();
    }

    @Override
    public float calsulateNeighborRadius(SimulationState simulationState) {
        Queue<Task> roundTasks = simulationState.getRoundTasks();
        if (roundTasks.isEmpty()) {
            return 0;
        }
        double totalCpu = 0;
        int count = 0;

        for (Task task : roundTasks) {
            totalCpu += task.getCpu();
            count++;
        }
        return predictionClient.getPredictedPsi((float) (totalCpu / count), (float) (count / simulationConfig.getDeltaT()));
    }
}