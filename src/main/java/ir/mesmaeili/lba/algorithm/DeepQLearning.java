package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import ir.mesmaeili.lba.util.NumberUtil;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DeepQLearning extends EvblbAlgorithm {
    private static final int EPISODE_COUNT = 100;
    private final int STATE_SIZE;
    private final int ACTION_SIZE;
    private static final double ALPHA = 0.1;
    private static final double GAMMA = 0.9;
    private static final double EPISODE_TERMINATION_EPSILON = 0.05;
    private final double[][] qTable;
    private final Random random;
    private final MultiLayerNetwork model;

    public DeepQLearning(SimulationConfig simulationConfig, EvblbConfig config) {
        super(simulationConfig, config);
        this.STATE_SIZE = 10;
        this.ACTION_SIZE = 10;
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
            List<EdgeServer> neighbors = neighborSelector.findNeighbors(e_i, simulationState.getEdgeServers());
            EdgeServer e_k = getServerWithMaxRemainingResource(neighbors);
            Geometry serverRegion = vu.getRegion(config.getVoronoiTessellation(), e_i);
            assignTasksInRegionToServer(serverRegion, simulationState.getRoundTasks(), e_k);
        }
    }

    @Override
    public NeighborSelector getNeighborSelector() {
        return new DQLNeighborSelection();
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

    private int chooseAction(int episode, int state) {
        if (episode < 30) {
            return random.nextInt(ACTION_SIZE);
        } else {
            return bestAction(state);
        }
    }

    private int bestAction(int state) {
        // Convert state to neural network input format
        INDArray input = Nd4j.create(qTable[state]);
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
        updateQTable(state, action, state, 0); // ?
        return bestAction(state);
    }

    public int run(SimulationConfig simulationConfig, SimulationState simulationState) {
        int state = 0;
        for (int episode = 0; episode < EPISODE_COUNT; episode++) {
            state = random.nextInt(STATE_SIZE);
            int maxTrain = 100;
            int trainCount = 1;
            while (trainCount < maxTrain) {
                int action = chooseAction(episode, state);
                double lbf = simulationState.calculateLBF(simulationConfig.getDeltaT());
                double reward = computeReward(lbf);
                int nextState = nextState(state, action);
                updateQTable(state, action, nextState, reward);
                if (Math.abs(state - nextState) < EPISODE_TERMINATION_EPSILON) {
                    break;
                }
                state = nextState;
                trainCount++;
            }
        }
        return NumberUtil.argMax(qTable[state]);
    }

    private double computeReward(double lbf) {
        return 1 - lbf;
    }
}
