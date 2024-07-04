package ir.mesmaeili.drl.alg;

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

    public DeepQLearning() {
        qTable = new double[STATE_SIZE][ACTION_SIZE];
        random = new Random();
    }

    private double calculateLBF(int regionIndex, double[] serverLoads) {
        double meanLoad = Arrays.stream(serverLoads).average().orElse(0);
        return Math.sqrt(Arrays.stream(serverLoads).map(u -> Math.pow(u - meanLoad, 2)).sum() / serverLoads.length);
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
        int bestAction = 0;
        double maxQValue = Double.NEGATIVE_INFINITY;
        for (int action = 0; action < ACTION_SIZE; action++) {
            if (qTable[state][action] > maxQValue) {
                maxQValue = qTable[state][action];
                bestAction = action;
            }
        }
        return bestAction;
    }

    private void updateQTable(int state, int action, int nextState, double reward) {
        double maxNextQValue = Arrays.stream(qTable[nextState]).max().orElse(0);
        qTable[state][action] = (1 - ALPHA) * qTable[state][action] + ALPHA * (reward + GAMMA * maxNextQValue);
    }

    private int nextState(int state, int action) {
        updateQTable(state, action, state, 0);
        return bestAction(state);
    }

    // اجرای الگوریتم
    public void run() {
        for (int episode = 0; episode < EPISODE_COUNT; episode++) {
            int state = random.nextInt(STATE_SIZE);
            while (true) {
                int action = chooseAction(state);
                double[] serverLoads = new double[0];
                double lbf = calculateLBF(state, serverLoads);
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
