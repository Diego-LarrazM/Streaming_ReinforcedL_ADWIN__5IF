package if5.research.core.reinforcement;

public class CriticInfo {
    public double[] state; // s_t
    public double value;

    public CriticInfo(double[] state, double value) {
        this.state = state;
        this.value = value;
    }
}
