package if5.research.core.bucketing.stats;

public class WindowSummary {

    public int n;
    public double mean;
    public double variance;

    public double lossEMA;
    public double prediction;

    public WindowSummary() {
        this.n = 0;
        this.mean = 0;
        this.variance = 0;

        this.lossEMA = 0;
        this.prediction = 0;
    }
}
