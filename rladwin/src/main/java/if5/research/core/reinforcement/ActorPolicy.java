package if5.research.core.reinforcement;

import java.io.Serializable;

public class ActorPolicy {
    private double[] W; // Policy weights
    private double lr;

    public ActorPolicy(int featureDimension, double learningRate) {
        this.W = new double[featureDimension];
        this.lr = learningRate;
    }

    public void init() {
        // Initialize policy weights to zero to start with 50/50 chance for each action
        for (int i = 0; i < W.length; i++) {
            W[i] = 0.0;
        }
    }

    public double[] evaluate(double[][] state) { // state : features vector
        double[] logits = new double[state.length];
        for (int i = 0; i < state.length; i++) {
            logits[i] = RLOps.dot(W, state[i]); // W·φ_i
        }
        return RLOps.softmax(logits); // probabilities for each action
    }

    public void update(EvaluationInfo lastActionInfo, double advantage) {
        // lastActionInfo.action = chosen split index (0..K) at previous step with K splits
        // lastActionInfo.actionState = state features of splits at previous step
        // lastActionInfo.probabilities = softmax probabilities of all splits
        // state = all candidate split features at current step

        // φ_i = s[i] feature vector of action i (split i or WAIT bias)
        // p(i | s) = softmax(W·φ_i)
        // π(. | s) = [p(0 | s), ..., p(K | s)] distribution over actions
        // π(a | s) = p(a | s) case where action a was chosen
        // ∇_w log π(a | s) = φ_a - sum_i (p(i | s) · φ_i)
        // ΔW = lr * advantage · ∇_w ​log π(a∣s)
        double[][] state = lastActionInfo.actionState;
        double[] phi_a = state[lastActionInfo.action]; // φ_a
        for (int j = 0; j < W.length; j++) { // for each feature dimension j
            double wsum_i = 0.0;
            for (int i = 0; i < state.length; i++) { // for each action i
                wsum_i += lastActionInfo.probabilities[i] * state[i][j]; // p(i | s) · φ_i[j]
            }
            W[j] += lr * advantage * (phi_a[j] - wsum_i);
        }
    }
}