package if5.research.core.reinforcement;

public class Rewarder {
  private double latency_alpha;
  private double split_cost;
  private double exploration_alpha;
  private double baseline_loss;
  private double trend_gamma;

  /**
     * Constructor
     * @param latency_alpha weight for latency/window-size term
     * @param split_cost penalty for splitting
     * @param exploration_alpha weight for entropy bonus
     * @param trend_gamma smoothing factor for running baseline
     * @param initial_baseline_loss initial guess for baseline loss
  */
  public Rewarder(double latency_alpha, double split_cost, double exploration_alpha, double trend_gamma, double initial_baseline_loss) {
    this.latency_alpha = latency_alpha;
    this.split_cost = split_cost;
    this.exploration_alpha = exploration_alpha;
    this.trend_gamma = trend_gamma;
    this.baseline_loss = initial_baseline_loss;
  }

  /**
     * Compute the reward for a step
     * @param window_size current window size
     * @param did_split 1 if split occurred, 0 otherwise
     * @param loss_before predictor loss before split
     * @param loss_after predictor loss after split
     * @param actionProbs array of action probabilities from policy [P(no_split), P(split)]
     * @return reward
  */
  public double computeReward(int window_size, int did_split, double loss_before, double loss_after, double[] actionProbs) {
    // Reward based on prediction accuracy improvement
    baseline_loss = (1 - trend_gamma) * baseline_loss + trend_gamma * loss_before; // Update running baseline
    double loss_delta_norm = (loss_before - loss_after) / baseline_loss;
    loss_delta_norm = Math.max(Math.min(loss_delta_norm, 1.0), -1.0); // clip

    // Reward based on latency (window size)
    double latency_r = latency_alpha / window_size;

    // Entropy bonus to encourage exploration
    double entropy_bonus = exploration_alpha * RLOps.entropy(actionProbs);

    return loss_delta_norm + latency_r - split_cost * did_split + entropy_bonus;
  }
}

