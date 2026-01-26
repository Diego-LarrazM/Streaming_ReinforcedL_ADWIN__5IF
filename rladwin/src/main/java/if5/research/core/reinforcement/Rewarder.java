package if5.research.core.reinforcement;

public class Rewarder {
  private final double latency_alpha;
  private final double split_cost;
  private final double trend_gamma;
  private final double kl_max; 
  private final double loss_max;
  private final double loss_min;
  private final double tau;

  private double baseline_loss;
  private double exploration_alpha;

  /**
     * Constructor
     * @param latency_alpha weight for latency/window-size term
     * @param split_cost penalty for splitting
     * @param exploration_alpha weight for entropy bonus
     * @param trend_gamma smoothing factor for running baseline
     * @param initial_baseline_loss initial guess for baseline loss
     * @param loss_max maximum normalized loss improvement
     * @param loss_min minimum normalized loss improvement
     * @param kl_max maximum KL divergence to avoid extreme split penalties
     * @param tau time constant for exploration decay
  */
  public Rewarder(
    double latency_alpha, 
    double split_cost, 
    double exploration_alpha, 
    double trend_gamma, 
    double initial_baseline_loss, 
    double kl_max, 
    double loss_max, 
    double loss_min,
    double tau
  ) {
    this.latency_alpha = latency_alpha;
    this.split_cost = split_cost;
    this.exploration_alpha = exploration_alpha;
    this.trend_gamma = trend_gamma;
    this.baseline_loss = initial_baseline_loss;
    this.loss_max = loss_max;
    this.loss_min = loss_min;
    this.kl_max = kl_max;
    this.tau = tau;
  }

  /**
     * Compute the reward for a step
     * @param window_size current window size
     * @param did_split 1 if split occurred, 0 otherwise
     * @param meanOld mean of rejected part of the split
     * @param varOld variance of rejected part of the split
     * @param meanNew mean of new window
     * @param varNew variance of new window
     * @param loss_before predictor loss before split
     * @param loss_after predictor loss after split
     * @param actionProbs array of action probabilities from policy [P(no_split), P(split)]
     * @return reward
  */
  public double computeReward(
    int window_size, 
    int did_split,  
    double meanOld,
    double varOld,
    double meanNew,
    double varNew,
    double loss_before, 
    double loss_after, 
    double[] actionProbs
  ) {
    // Reward based on prediction accuracy improvement
    baseline_loss = (1 - trend_gamma) * baseline_loss + trend_gamma * loss_before; // Update running baseline
    double loss_delta_norm = (loss_before - loss_after) / baseline_loss;
    loss_delta_norm = Math.max(Math.min(loss_delta_norm, loss_max), loss_min); // clip

    // Reward based on latency (window size)
    double latency_r = latency_alpha / window_size;

    // Entropy bonus to encourage exploration
    double entropy_bonus = exploration_alpha * RLOps.entropy(actionProbs);
    exploration_alpha *= Math.exp(-1 / tau); // decay exploration

    // Split penalty based on KL divergence between old and new DATA windows
    // Windows are modeled as Gaussians using mean/variance of the raw stream (or features)
    // KL is used to reduce split cost when a real distribution shift is detected
    double split_penalty = 0.0;
    if (did_split == 1) {
        double kl = RLOps.gaussianKL(meanOld, varOld, meanNew, varNew);
        kl = Math.min(kl, kl_max); // max KL divergence to avoid extreme split penalties
        split_penalty = split_cost / (1.0 + kl);
    }

    return loss_delta_norm + latency_r - split_penalty + entropy_bonus;
  }
}

