package if5.research.core.reinforcement;

public class EvaluationInfo {
    public double[][] actionState; // split features vector
    public double[] criticState; // general stats
    public int action;
    public double value; // critic expected return from state
    public double[] probabilities; // softmax probabilities of all actions

    public EvaluationInfo(double[][] actionState, int action, double[] probabilities, double[] criticState) {
        this.actionState = actionState;
        this.action = action;
        this.probabilities = probabilities;
        this.criticState = criticState;
    }
}
