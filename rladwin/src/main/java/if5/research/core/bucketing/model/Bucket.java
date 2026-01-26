package if5.research.core.bucketing.model;

import com.tdunning.math.stats.MergingDigest;

public class Bucket {

    public int start;
    public int end;

    public double count;
    public double sum;
    public double sumOfSquares;
    public double variance;
    public double min;
    public double max;
    public double ema;        // Exponential Moving Average of values
    public double lossEMA;    // Exponential Moving Average of loss
    public MergingDigest tDigest;  // T-Digest for quantile estimation

    private static final double ALPHA = 0.7; // alpha = 0.7 gives 70% weight to newer values, higher value means more weight to recent data
    private static final int TDIGEST_COMPRESSION = 100; // Compression factor for T-Digest (higher = more accuracy but more memory)
    // TODO : see if it is interesting to add Wasserstein-1 histogram

    public Bucket(double value, double loss) {
        this.count = 1;
        this.sum = value;
        this.sumOfSquares = value * value;
        this.variance = 0;
        this.min = value;
        this.max = value;
        this.ema = value;
        this.lossEMA = loss;

        // Initialize T-Digest and add the first value
        this.tDigest = new MergingDigest(TDIGEST_COMPRESSION);
        this.tDigest.add(value);
    }

    public Bucket(double count, double sum, double sumOfSquares, double variance, double min, double max, double ema, double lossEMA, MergingDigest tDigest) {
        this.count = count;
        this.sum = sum;
        this.sumOfSquares = sumOfSquares;
        this.variance = variance;
        this.min = min;
        this.max = max;
        this.ema = ema;
        this.lossEMA = lossEMA;
        this.tDigest = tDigest;
    }

    public static Bucket merge(Bucket older, Bucket newer) {
        double newCount = older.count + newer.count;
        double newSum = older.sum + newer.sum;
        double newSumOfSquares = older.sumOfSquares + newer.sumOfSquares;
        double newMin = Math.min(older.min, newer.min);
        double newMax = Math.max(older.max, newer.max);

        // Robust variance calculation
        double meanOlder = older.sum / older.count;
        double meanNewer = newer.sum / newer.count;
        double deltaMean = meanOlder - meanNewer;
        double weightedVarianceSum = (older.count * older.variance) + (newer.count * newer.variance);
        double meanDifferenceTerm = (older.count * newer.count * deltaMean * deltaMean) / newCount;
        double newVariance = (weightedVarianceSum + meanDifferenceTerm) / newCount;

        // Exponential Moving Average with decay factor 
        // The older EMA must decay by the count of values in the newer bucket.
        double decayFactor = Math.pow(1.0 - ALPHA, newer.count);
        double newEMA = newer.ema + (older.ema * decayFactor);
        double newLossEMA = newer.lossEMA + (older.lossEMA * decayFactor);

        // Merge T-Digests (create new digest to avoid mutating references)
        MergingDigest newDigest = new MergingDigest(TDIGEST_COMPRESSION);
        newDigest.add(older.tDigest);
        newDigest.add(newer.tDigest);

        return new Bucket(newCount, newSum, newSumOfSquares, newVariance, newMin, newMax, newEMA, newLossEMA, newDigest);
    }

    // --- Quantile Query Methods ---
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
     * percentile)
     */
    public double getQuantile(double quantile) {
        return tDigest.quantile(quantile);
    }

    @Override
    public String toString() {
        return "count: " + count + ", sum: " + sum + ", mean: " + (sum / count) + ", median: " + getMedian() + ", ema: " + ema + ", sumOfSquares: " + sumOfSquares + ", variance: " + variance + ", min: " + min + ", max: " + max + ", lossEMA: " + lossEMA;
    }
}
