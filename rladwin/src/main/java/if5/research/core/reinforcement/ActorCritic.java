package if5.research.core.reinforcement;

public class ActorCritic {
    private ActorPolicy aPolicy;
    private Critic critic;
    private EvaluationInfo lastEvaluationInfo = null;
    private EvaluationInfo currentEvaluationInfo = null;

    public ActorCritic(int featureDimension, double alr, double clr, double discountGamma) {
        this.aPolicy = new ActorPolicy(featureDimension, alr);
        this.critic = new Critic(featureDimension, clr, discountGamma);
    }

    public int sample(double[] distribution){
        double u = Math.random(); // uniform in [0,1)
        double cumulative = 0.0;

        for (int i = 0; i < distribution.length; i++) {
            cumulative += distribution[i];
            if (u <= cumulative) {
                return i;
            }
        }
        // Numerical safety
        return distribution.length - 1;
    }

    public int decide(double [][] splitsFeaturesBatch, double[] generalStats){
        // Decide an action
        double[] scores = aPolicy.evaluate(splitsFeaturesBatch);
        int action = sample(scores);
        // Save information of current actions for delayed updating
        currentEvaluationInfo = new EvaluationInfo(splitsFeaturesBatch, action, scores, generalStats);
        return action;
    }

    public void update(double previousReward, double[] newStateGeneralStats) {
        if(lastEvaluationInfo != null){
            // Predict expected return from this state given onwards.
            double current_value = critic.evaluate(currentEvaluationInfo.criticState); // V_{t+1}
            // Compute advantage (TD error)
            double advantage = critic.advantage(previousReward, lastEvaluationInfo.value, current_value); // δ_t
            // Update actor policy
            aPolicy.update(lastEvaluationInfo, advantage);
            // Update critic value function
            critic.update(advantage, lastEvaluationInfo.criticState);
        }
        // update old info
        lastEvaluationInfo = currentEvaluationInfo;
    }
}








