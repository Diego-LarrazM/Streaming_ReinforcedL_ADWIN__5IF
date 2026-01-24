package if5.research.core.bucketing.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Quantiles {

    private static final int MAX_SIZE = 32;

    private final List<Double> samples;

    public Quantiles() {
        this.samples = new ArrayList<>();
    }

    public Quantiles(double value) {
        this();
        observe(value);
    }

    public void observe(double value) {
        if (Double.isNaN(value)) return;

        samples.add(value);
        Collections.sort(samples);

        if (samples.size() > MAX_SIZE) {
            compress();
        }
    }

    public void merge(Quantiles other) {
        if (other.samples.isEmpty()) return;

        samples.addAll(other.samples);
        Collections.sort(samples);

        if (samples.size() > MAX_SIZE) {
            compress();
        }
    }

    private void compress() {
        if (samples.size() <= MAX_SIZE) return;

        List<Double> compressed = new ArrayList<>();
        int step = samples.size() / MAX_SIZE;

        for (int i = 0; i < samples.size(); i += step) {
            compressed.add(samples.get(i));
            if (compressed.size() >= MAX_SIZE) break;
        }

        samples.clear();
        samples.addAll(compressed);
    }

    public double quantile(double q) {
        if (samples.isEmpty()) return Double.NaN;

        int idx = (int) Math.floor(q * (samples.size() - 1));
        return samples.get(idx);
    }

    public double q25() {
        return quantile(0.25);
    }

    public double q50() {
        return quantile(0.50);
    }

    public double q75() {
        return quantile(0.75);
    }

    public double[] summary() {
        return new double[] { q25(), q50(), q75() };
    }

    public int size() {
        return samples.size();
    }
}
