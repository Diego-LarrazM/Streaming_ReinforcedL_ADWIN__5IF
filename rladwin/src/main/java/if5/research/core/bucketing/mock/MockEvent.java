package if5.research.core.bucketing.mock;

public class MockEvent {
    public double value;
    public double loss;
    public double prediction;
    public long timestamp;

    public MockEvent(double value, double loss, double prediction, long timestamp) {
        this.value = value;
        this.loss = loss;
        this.prediction = prediction;
        this.timestamp = timestamp;
    }
}