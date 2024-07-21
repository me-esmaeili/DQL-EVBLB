package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.api.Layer;
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

@Slf4j
public class DeepQLearning {
    private static final int EPISODE_COUNT = 100;
    private final static int EPISODE_MAX_TRAIN_COUNT = 10;
    private static final int STABLE_EPISODE_COUNT = 30;
    private static final double ALPHA = 0.1;
    private static final double GAMMA = 0.9;
    private static final double EPISODE_TERMINATION_EPSILON = 0.05;
    private final int STATE_SIZE;
    private final int ACTION_SIZE;
    private final double[][] qTable;
    private final Random random;
    private final MultiLayerNetwork model;
    private int currentEpisode;
    private int currentState;
    private int currentTrainEpisode;

    public DeepQLearning(int stateCount, int actionCount) {
        this.STATE_SIZE = stateCount; // equal to LBF
        this.ACTION_SIZE = actionCount; // equal to Radius
        this.qTable = new double[this.STATE_SIZE][this.ACTION_SIZE];
        this.random = new Random();
        this.currentState = random.nextInt(STATE_SIZE);
        this.currentEpisode = 0;
        this.currentTrainEpisode = 0;
        // Define the neural network configuration
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(ACTION_SIZE).nOut(20)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(20).nOut(10)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(10).nOut(STATE_SIZE).build())
                .build();
        // Create the network
        model = new MultiLayerNetwork(conf);
        model.init();
    }

    public int selectOptimalRadius(double lbf) {
        if (currentEpisode < EPISODE_COUNT) {
            currentState = random.nextInt(STATE_SIZE);
            if (this.currentTrainEpisode < EPISODE_MAX_TRAIN_COUNT) {
                int action = chooseAction(currentEpisode, currentState);
                log.info("Select action(LBF {}) as the best action in episode {}", action, currentEpisode);
                double reward = computeReward(lbf);
                int nextState = nextState(currentState, action);
                updateQTable(currentState, action, nextState, reward);
                if (Math.abs(currentState - nextState) < EPISODE_TERMINATION_EPSILON) {
                    return NumberUtil.argMax(qTable[currentState]);
                }
                currentState = nextState;
                currentTrainEpisode++;
                return NumberUtil.argMax(qTable[currentState]);
            } else {
                this.currentTrainEpisode = 1; // reset episode train
            }
            currentEpisode++;
        }
        return NumberUtil.argMax(qTable[currentState]);
    }

    private int chooseAction(int episode, int state) {
        if (episode < STABLE_EPISODE_COUNT) {
            return random.nextInt(ACTION_SIZE);
        } else {
            return bestAction(state);
        }
    }

    private int bestAction(int state) {
        // Convert state to neural network input format
        INDArray input = Nd4j.createFromArray(qTable[state]).reshape(1, ACTION_SIZE);
        // Get Q-values from the network
        INDArray qValues = model.output(input, Layer.TrainingMode.TRAIN);
        // Find the best action
        return Nd4j.argMax(qValues).getInt(0);
    }

    private void updateQTable(int state, int action, int nextState, double reward) {
        double maxNextQValue = Arrays.stream(qTable[nextState]).max().orElse(0);
        qTable[state][action] = (1 - ALPHA) * qTable[state][action] + ALPHA * (reward + GAMMA * maxNextQValue);
    }

    private int nextState(int state, int action) {
        updateQTable(state, action, state, 0);
        return bestAction(state);
    }

    private double computeReward(double lbf) {
        return 1 - lbf;
    }
}
