package if5.research.core.bucketing.model;

import com.tdunning.math.stats.MergingDigest;
import if5.research.core.bucketing.stats.WindowSummary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BucketManager {

    private static final int M = 5; // Max buckets per row
    public static final int FEATURE_DIM = 11;

    private final List<LinkedList<Bucket>> rows = new ArrayList<>();

    // Adds a new value to the bucket manager
    public void update(double value) {
        Bucket bucket = new Bucket(value);
        insert(bucket, 0);
    }

    private void insert(Bucket bucket, int rowIndex) {

        while (rows.size() <= rowIndex) {
            rows.add(new LinkedList<>());
        }

        LinkedList<Bucket> row = rows.get(rowIndex);

        row.addLast(bucket);

        if (row.size() > M) {
            Bucket oldest = row.removeFirst();
            Bucket secondOldest = row.removeFirst();

            Bucket merged = Bucket.merge(oldest, secondOldest);

            insert(merged, rowIndex + 1);
        }
    }

    public Iterable<Bucket> oldestToNewest() {
        List<Bucket> ordered = new ArrayList<>();

        for (int r = rows.size() - 1; r >= 0; r--) {
            LinkedList<Bucket> row = rows.get(r);
            for (Bucket b : row) {
                ordered.add(b);
            }
        }
        return ordered;
    }

    public List<Bucket> getBucketsOldestToNewest() {
        List<Bucket> ordered = new ArrayList<>();
        for (int r = rows.size() - 1; r >= 0; r--) {
            LinkedList<Bucket> row = rows.get(r);
            for (Bucket b : row) {
                ordered.add(b);
            }
        }
        return ordered;
    }

    public List<Integer> getCandidateSplits(int minBucketsPerSide) {
        List<Integer> splits = new ArrayList<>();
        int total = 0;
        for (LinkedList<Bucket> row : rows) {
            total += row.size();
        }
        for (int i = minBucketsPerSide;
             i <= total - minBucketsPerSide;
             i++) {
            splits.add(i); // global time index
        }
        return splits;
    }

    private WindowSummary summarizeBuckets(List<Bucket> buckets, int from, int to) {

        WindowSummary w = new WindowSummary();

        double sum = 0;
        double varSum = 0;
        double count = 0;

        MergingDigest digest = null;

        for (int i = from; i < to; i++) {
            Bucket b = buckets.get(i);

            if (digest == null) {
                digest = new MergingDigest(100);
            }
            digest.add(b.tDigest);

            if (count == 0) {
                count = b.count;
                sum = b.sum;
                varSum = b.variance * b.count;
            } else {
                double mean1 = sum / count;
                double mean2 = b.sum / b.count;
                double delta = mean1 - mean2;

                varSum += b.variance * b.count
                        + (count * b.count * delta * delta)
                        / (count + b.count);

                sum += b.sum;
                count += b.count;
            }
        }

        w.n = (int) count;
        w.mean = sum / count;
        w.variance = varSum / count;
        w.digest = digest;

        return w;
    }

    public double[][] buildSplitFeatureBatch(int minBucketsPerSide) {

        List<Bucket> ordered = getBucketsOldestToNewest();
        List<Integer> candidateSplits = getCandidateSplits(minBucketsPerSide);

        int K = candidateSplits.size();
        double[][] features = new double[K + 1][FEATURE_DIM];

        features[0] = getWaitVector();

        for (int i = 0; i < K; i++) {

            int split = candidateSplits.get(i);

            WindowSummary oldW =
                    summarizeBuckets(ordered, 0, split);

            WindowSummary newW =
                    summarizeBuckets(ordered, split, ordered.size());

            double oldQ50 = oldW.digest.quantile(0.5);
            double newQ50 = newW.digest.quantile(0.5);

            double oldQ10 = oldW.digest.quantile(0.1);
            double oldQ90 = oldW.digest.quantile(0.9);

            double newQ10 = newW.digest.quantile(0.1);
            double newQ90 = newW.digest.quantile(0.9);

            double[] f = new double[FEATURE_DIM];
            double eps = 1e-9;

            f[0] = 1.0; // bias
            f[1] = oldW.mean;
            f[2] = newW.mean;
            f[3] = oldW.variance;
            f[4] = newW.variance;
            f[5] = Math.abs(newW.mean - oldW.mean);
            f[6] = newW.variance / (oldW.variance + eps);
            f[7] = newW.n / (double) (oldW.n + newW.n + eps);
            f[8]  = Math.abs(newQ50 - oldQ50);
            f[9]  = (newQ90 - newQ10);
            f[10] = (newQ90 - newQ10) / (oldQ90 - oldQ10 + eps);

            features[i + 1] = f;
        }

        return features;
    }

    // Returns a string representation of the bucket manager
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LinkedList<Bucket> row : rows) {
            for (Bucket bucket : row) {
                sb.append(bucket.toString());
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public double[] getGeneralStats() {

        List<Bucket> buckets = getBucketsOldestToNewest();

        double sum = 0;
        double varSum = 0;
        double count = 0;

        MergingDigest digest = new MergingDigest(100);

        for (Bucket b : buckets) {
            digest.add(b.tDigest);

            if (count == 0) {
                count = b.count;
                sum = b.sum;
                varSum = b.variance * b.count;
            } else {
                double mean1 = sum / count;
                double mean2 = b.sum / b.count;
                double delta = mean1 - mean2;

                varSum += b.variance * b.count
                        + (count * b.count * delta * delta)
                        / (count + b.count);

                sum += b.sum;
                count += b.count;
            }
        }

        double mean = sum / count;
        double variance = varSum / count;

        double q10 = digest.quantile(0.1);
        double q50 = digest.quantile(0.5);
        double q90 = digest.quantile(0.9);

        return new double[] {
                1.0,
                mean,
                variance,
                count,
                q50,
                q90 - q10
        };
    }

    private double[] getWaitVector() {
        double[] f = new double[FEATURE_DIM];
        f[0] = 1.0;
        return f;
    }

    public void applyAction(int action) {

        // Action 0 = WAIT
        if (action == 0) {
            return;
        }

        int splitIndex = action - 1;

        int seen = 0;

        for (int r = rows.size() - 1; r >= 0; r--) {
            LinkedList<Bucket> row = rows.get(r);

            if (seen + row.size() <= splitIndex) {
                // This entire row is older so we drop it
                seen += row.size();
                row.clear();
            } else {
                // Split happens inside this row
                int localIndex = splitIndex - seen;
                // Remove buckets strictly older than split
                for (int i = 0; i < localIndex; i++) {
                    row.removeFirst();
                }
                // All higher older rows must be cleared
                for (int rr = r + 1; rr < rows.size(); rr++) {
                    rows.get(rr).clear();
                }
                break;
            }
        }
    }

}
