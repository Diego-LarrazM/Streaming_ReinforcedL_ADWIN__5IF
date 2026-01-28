package if5.research.core.bucketing.model;

import java.io.Serializable;

import com.tdunning.math.stats.MergingDigest;

public class Bucket {

    // Core Statistics
    public double count;
    public double sum;
    public double variance;
    public double min;
    public double max;

    // Advanced Distribution Statistics
    public MergingDigest tDigest;  // T-Digest for quantile estimation

    private static final int TDIGEST_COMPRESSION = 100; // Compression factor for T-Digest (higher = more accuracy but more memory)

    // Constructor for a single value
    public Bucket(double value) {
        this.count = 1;
        this.sum = value;
        this.variance = 0;
        this.min = value;
        this.max = value;

        // Initialize T-Digest and add the first value
        this.tDigest = new MergingDigest(TDIGEST_COMPRESSION);
        this.tDigest.add(value);
    }

    // Constructor for all values
    public Bucket(double count, double sum, double variance, double min, double max, MergingDigest tDigest) {
        this.count = count;
        this.sum = sum;
        this.variance = variance;
        this.min = min;
        this.max = max;
        this.tDigest = tDigest;
    }

    // Merge two buckets
    public static Bucket merge(Bucket older, Bucket newer) {
        double newCount = older.count + newer.count;
        double newSum = older.sum + newer.sum;
        double newMin = Math.min(older.min, newer.min);
        double newMax = Math.max(older.max, newer.max);

        // Robust variance calculation
        double meanOlder = older.sum / older.count;
        double meanNewer = newer.sum / newer.count;
        double deltaMean = meanOlder - meanNewer;
        double weightedVarianceSum = (older.count * older.variance) + (newer.count * newer.variance);
        double meanDifferenceTerm = (older.count * newer.count * deltaMean * deltaMean) / newCount;
        double newVariance = (weightedVarianceSum + meanDifferenceTerm) / newCount;

        // Merge T-Digests (create new digest to avoid mutating references)
        MergingDigest newDigest = new MergingDigest(TDIGEST_COMPRESSION);
        newDigest.add(older.tDigest);
        newDigest.add(newer.tDigest);

        return new Bucket(newCount, newSum, newVariance, newMin, newMax, newDigest);
    }

    /**
     * Get the median (50th percentile) of values in this bucket
     */
    public double getMedian() {
        return tDigest.quantile(0.5);
    }

    /**
     * Get a specific quantile (percentile) of values in this bucket
     *
     * @param quantile Value between 0.0 and 1.0 (e.g., 0.99 for 99th
     *                 percentile)
     */
    public double getQuantile(double quantile) {
        return tDigest.quantile(quantile);
    }

    @Override
    public String toString() {
        return "count: " + count + ", sum: " + sum + ", mean: " + (sum / count) + ", median: " + getMedian() + ", variance: " + variance +
                ", quantile-tdigest: "+tDigest.quantile(0.5)+", min: " + min + ", max: " + max;
    }
}
