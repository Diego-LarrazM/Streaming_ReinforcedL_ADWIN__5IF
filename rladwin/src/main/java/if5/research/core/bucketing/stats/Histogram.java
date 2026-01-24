package if5.research.core.bucketing.stats;

import java.util.Arrays;

public class Histogram {

    private static final int NUM_BINS = 16;
    private final double[] bins;
    private double min;
    private double max;
    private int totalCount;

    public Histogram() {
        this.bins = new double[NUM_BINS];
        this.min = Double.POSITIVE_INFINITY;
        this.max = Double.NEGATIVE_INFINITY;
        this.totalCount = 0;
    }

    public Histogram(double value) {
        this();
        observe(value);
    }

    public void observe(double value) {
        if (Double.isNaN(value)) return;

        if (value < min) min = value;
        if (value > max) max = value;

        if (max == min) {
            bins[0]++;
        } else {
            int idx = (int) ((value - min) / (max - min) * (NUM_BINS - 1));
            idx = Math.max(0, Math.min(NUM_BINS - 1, idx));
            bins[idx]++;
        }
        totalCount++;
    }

    public void merge(Histogram other) {
        if (other.totalCount == 0) return;

        if (this.totalCount == 0) {
            System.arraycopy(other.bins, 0, this.bins, 0, NUM_BINS);
            this.min = other.min;
            this.max = other.max;
            this.totalCount = other.totalCount;
            return;
        }

        double newMin = Math.min(this.min, other.min);
        double newMax = Math.max(this.max, other.max);

        double[] newBins = new double[NUM_BINS];

        rebin(this.bins, this.min, this.max, newBins, newMin, newMax);
        rebin(other.bins, other.min, other.max, newBins, newMin, newMax);

        System.arraycopy(newBins, 0, this.bins, 0, NUM_BINS);
        this.min = newMin;
        this.max = newMax;
        this.totalCount += other.totalCount;
    }

    private void rebin(double[] src, double srcMin, double srcMax,
                       double[] dst, double dstMin, double dstMax) {

        if (srcMax == srcMin) {
            dst[0] += Arrays.stream(src).sum();
            return;
        }

        for (int i = 0; i < NUM_BINS; i++) {
            double center = srcMin + (i + 0.5) * (srcMax - srcMin) / NUM_BINS;
            int idx = (int) ((center - dstMin) / (dstMax - dstMin) * (NUM_BINS - 1));
            idx = Math.max(0, Math.min(NUM_BINS - 1, idx));
            dst[idx] += src[i];
        }
    }

    public double wassersteinDistance(Histogram other) {
        double[] cdf1 = cumulative();
        double[] cdf2 = other.cumulative();

        double dist = 0;
        for (int i = 0; i < NUM_BINS; i++) {
            dist += Math.abs(cdf1[i] - cdf2[i]);
        }
        return dist / NUM_BINS;
    }

    private double[] cumulative() {
        double[] cdf = new double[NUM_BINS];
        double cum = 0;
        for (int i = 0; i < NUM_BINS; i++) {
            cum += bins[i];
            cdf[i] = cum / totalCount;
        }
        return cdf;
    }

    public double[] approximateQuantiles() {
        double[] qs = new double[] {0.25, 0.5, 0.75};
        double[] result = new double[3];
        double cum = 0;
        int qIdx = 0;

        for (int i = 0; i < NUM_BINS && qIdx < qs.length; i++) {
            cum += bins[i];
            while (cum / totalCount >= qs[qIdx]) {
                result[qIdx] = min + (i + 0.5) * (max - min) / NUM_BINS;
                qIdx++;
                if (qIdx >= qs.length) break;
            }
        }
        return result;
    }
}
