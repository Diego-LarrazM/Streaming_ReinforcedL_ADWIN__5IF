package if5.research.core.reinforcement;

public class Critic {
    private double[] W; // Value function weights
    private double lr;
    private double discountGamma;

    public Critic(int featureDimension, double learningRate, double discountGamma) {
        this.W = new double[featureDimension];
        this.lr = learningRate;
        this.discountGamma = discountGamma;
    }

    public void init() {
        // Initialize value function weights to zero for neutral start
        for (int i = 0; i < W.length; i++) {
            W[i] = 0.0;
        }
    }

    public double evaluate(double[] state){ // Linear model
        return RLOps.dot(this.W, state); 
    }  

    public double advantage(double prevReward, double prevVal, double currentVal){
        return prevReward + discountGamma * currentVal - prevReward;
    }

    public void update(double advantage, double[] prevState) {
        // TD error (advantage):
        // δ_t = r_t + γV(s_{t+1}) - V(s_t)
        //
        // Loss:
        // L = 1/2 * δ_t^2
        //
        // Semi-gradient TD update (Sutton & Barto):
        // ∇_w L = -δ_t * s_t
        // w ← w + α δ_t s_t
        for (int i = 0; i < W.length; i++) {
            W[i] += lr * advantage * prevState[i];
        }

    }



}
