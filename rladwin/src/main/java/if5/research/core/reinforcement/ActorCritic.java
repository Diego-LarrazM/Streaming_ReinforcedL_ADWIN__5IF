package if5.research.core.reinforcement;

import java.io.Serializable;
import java.util.Arrays;

public class ActorCritic {
    private ActorPolicy aPolicy;
    private Critic critic;
    private EvaluationInfo lastEvaluationInfo = null;
    private EvaluationInfo currentEvaluationInfo = null;
    private double[] lastActionProbs;

    public ActorCritic(int actorFeatureDimension, int criticFeatureDimension, double alr, double clr, double discountGamma) {
        this.aPolicy = new ActorPolicy(actorFeatureDimension, alr);
        this.critic = new Critic(criticFeatureDimension, clr, discountGamma);
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
        this.lastActionProbs = scores;
        int action = sample(scores);
        // Save information of current actions for delayed updating
        currentEvaluationInfo = new EvaluationInfo(splitsFeaturesBatch, action, scores, generalStats);
        return action;
    }

    public void update(double reward, double[] newStateGeneralStats) {
        if(lastEvaluationInfo != null){
            // Predict expected return from this state given onwards.
            double current_value = critic.evaluate(currentEvaluationInfo.criticState); // V_{t+1}
            // Compute advantage (TD error)
            double advantage = critic.advantage(reward, lastEvaluationInfo.value, current_value); // δ_t
            // Update actor policy
            aPolicy.update(lastEvaluationInfo, advantage);
            // Update critic value function
            critic.update(advantage, lastEvaluationInfo.criticState);
        }
        // update old info
        lastEvaluationInfo = currentEvaluationInfo;
        System.out.println(Arrays.toString(aPolicy.getW()));
    }
    public double[] getLastActionProbs() {
        return lastActionProbs;
    }

}








