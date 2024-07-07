package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Arrays;
import java.util.Random;

public class DeepQLearning {
    private static final int EPISODE_COUNT = 10;
    private static final int STATE_SIZE = 10;
    private static final int ACTION_SIZE = 5;
    private static final double ALPHA = 0.1;
    private static final double GAMMA = 0.9;
    private static final double EPSILON = 0.2;
    private static final double EPISODE_TERMINATION_EPSILON = 0.01;
    private static final double THETA = 0.5;
    private static final double DELTA_T_MAX = 100;
    private final double[][] qTable;
    private final Random random;

    private MultiLayerNetwork model;


    public DeepQLearning() {
        qTable = new double[STATE_SIZE][ACTION_SIZE];
        random = new Random();
        // Define the neural network configuration
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(STATE_SIZE).nOut(20)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(20).nOut(10)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(10).nOut(ACTION_SIZE).build())
                .build();

        // Create the network
        model = new MultiLayerNetwork(conf);
        model.init();
    }

    private double estimatedReward(double fi, double deltaTj) {
        return THETA * (1 - fi) + (1 - THETA) * (deltaTj / DELTA_T_MAX);
    }

    private int chooseAction(int state) {
        if (random.nextDouble() < EPSILON) {
            return random.nextInt(ACTION_SIZE);
        } else {
            return bestAction(state);
        }
    }


    private int bestAction(int state) {
        // Convert state to neural network input format
        INDArray input = Nd4j.create(qTable[state]);
        // Get Q-values from the network
        INDArray qValues = model.output(input);
        // Find the best action
        return Nd4j.argMax(qValues).getInt(0);
    }

//    private int bestAction(int state) {
//        int bestAction = 0;
//        double maxQValue = Double.NEGATIVE_INFINITY;
//        for (int action = 0; action < ACTION_SIZE; action++) {
//            if (qTable[state][action] > maxQValue) {
//                maxQValue = qTable[state][action];
//                bestAction = action;
//            }
//        }
//        return bestAction;
//    }

    private void updateQTable(int state, int action, int nextState, double reward) {
        double maxNextQValue = Arrays.stream(qTable[nextState]).max().orElse(0);
        qTable[state][action] = (1 - ALPHA) * qTable[state][action] + ALPHA * (reward + GAMMA * maxNextQValue);
    }

    private int nextState(int state, int action) {
        updateQTable(state, action, state, 0);
        return bestAction(state);
    }

    public void run(SimulationConfig simulationConfig, SimulationState simulationState) {
        for (int episode = 0; episode < EPISODE_COUNT; episode++) {
            int state = random.nextInt(STATE_SIZE);
            while (true) {
                int action = chooseAction(state);
                double lbf = simulationState.calculateLBF(simulationConfig.getDeltaT());
                double reward = computeReward(lbf);
                int nextState = nextState(state, action);
                updateQTable(state, action, nextState, reward);
                if (Math.abs(state - nextState) < EPISODE_TERMINATION_EPSILON) {
                    break;
                }
                state = nextState;
            }
        }
    }

    private double computeReward(double lbf) {
        return 1 - lbf;
    }
}
