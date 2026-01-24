package if5.research.core.bucketing.model;

import if5.research.core.bucketing.stats.*;

import java.util.ArrayList;
import java.util.List;

public class BucketManager {

    private static final int MAX_BUCKETS_PER_ROW = 2;

    private final List<Bucket> arrivalBuckets = new ArrayList<>();

    private final List<Integer> bucketIndexTable = new ArrayList<>();

    private final List<BucketRow> rows = new ArrayList<>();

    private final List<SplitStats> splitStatsTable = new ArrayList<>();

    private int totalCount = 0;

    private void rebuildSplitStatsTable() {
        splitStatsTable.clear();

        // split i = [0 .. i-1] | [i .. end]
        for (int i = 1; i < arrivalBuckets.size(); i++) {
            WindowSummary left =
                    summarize(arrivalBuckets.subList(0, i));

            WindowSummary right =
                    summarize(arrivalBuckets.subList(i, arrivalBuckets.size()));

            splitStatsTable.add(new SplitStats(left, right));
        }
    }

    public void insert(double value, double loss) {
        if (rows.isEmpty()) {
            rows.add(new BucketRow());
        }

        Bucket b = new Bucket(value, loss);

        arrivalBuckets.add(b);
        bucketIndexTable.add(arrivalBuckets.size() - 1);

        rows.get(0).buckets.add(b);

        totalCount++;

        compressBuckets();

        rebuildSplitStatsTable();
    }

    private void compressBuckets() {
        int row = 0;

        while (row < rows.size()) {
            BucketRow current = rows.get(row);

            if (current.buckets.size() <= MAX_BUCKETS_PER_ROW) {
                row++;
                continue;
            }

            Bucket first = current.buckets.get(0);
            Bucket second = current.buckets.get(1);

            // merge second into first
            first.merge(second);

            // remove second from physical row
            current.buckets.remove(1);

            // remove second from arrival list + index table
            int idx = arrivalBuckets.indexOf(second);
            arrivalBuckets.remove(idx);
            bucketIndexTable.remove(idx);

            // push merged bucket
            if (rows.size() <= row + 1) {
                rows.add(new BucketRow());
            }

            rows.get(row + 1).buckets.add(first);
            current.buckets.remove(0);
        }
    }

    public List<Bucket> flattenBuckets() {
        List<Bucket> all = new ArrayList<>();

        for (int r = rows.size() - 1; r >= 0; r--) {
            all.addAll(rows.get(r).buckets);
        }

        return all;
    }

    public WindowSummary summarize(List<Bucket> buckets) {
        WindowSummary ws = new WindowSummary();

        double sum = 0.0;
        double sumSq = 0.0;

        Quantiles quantiles = new Quantiles();
        Histogram histogram = new Histogram();

        for (Bucket b : buckets) {
            sum += b.sum;
            sumSq += b.sumSquares;

            quantiles.merge(b.quantiles);
            histogram.merge(b.histogram);

            ws.lossEMA = 0.9 * b.lossEMA + 0.1 * ws.lossEMA; //need to check whether this is correct mathematically
            ws.n += b.n;
        }

        if (ws.n > 0) {
            ws.mean = sum / ws.n;
            ws.variance = (sumSq / ws.n) - ws.mean * ws.mean;
        }

        ws.quantiles = quantiles;
        ws.histogram = histogram;

        return ws;
    }

    public List<Integer> candidateSplits() {
        List<Integer> splits = new ArrayList<>();
        for (int i = 0; i < splitStatsTable.size(); i++) {
            splits.add(i + 1);
        }
        return splits;
    }

    public WindowSummary[] getSplitSummaries(int splitIndex) {
        SplitStats s = splitStatsTable.get(splitIndex - 1);
        return new WindowSummary[]{s.left, s.right};
    }

    public List<double[]> buildActionState() {
        List<double[]> actions = new ArrayList<>();

        int featureDim = StatisticalFeatures.FEATURE_DIM;

        // wait action
        double[] waitPhi = new double[featureDim + 1];
        waitPhi[0] = 1.0;
        actions.add(waitPhi);

        for (SplitStats s : splitStatsTable) {
            double[] raw = StatisticalFeatures.extract(s.left, s.right);

            double[] phi = new double[raw.length + 1];
            phi[0] = 0.0;
            System.arraycopy(raw, 0, phi, 1, raw.length);

            actions.add(phi);
        }

        return actions;
    }

    public void discardOldestBuckets(int upToIndex) {
        int removed = 0;
        for (int i = 0; i < upToIndex; i++) {
            removed += arrivalBuckets.get(i).n;
        }

        arrivalBuckets.subList(0, upToIndex).clear();
        bucketIndexTable.subList(0, upToIndex).clear();

        rebuildFromBuckets(arrivalBuckets);

        rebuildSplitStatsTable();

        totalCount -= removed;
    }

    private void rebuildFromBuckets(List<Bucket> buckets) {
        rows.clear();
        totalCount = 0;

        for (Bucket b : buckets) {
            insertMergedBucket(b);
        }

        rebuildSplitStatsTable();
    }

    private void insertMergedBucket(Bucket bucket) {
        int row = 0;

        while ((1 << row) < bucket.n) {
            row++;
        }

        while (rows.size() <= row) {
            rows.add(new BucketRow());
        }

        rows.get(row).buckets.add(bucket);
        totalCount += bucket.n;

        compressBuckets();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getNumberOfBuckets() {
        return rows.stream().mapToInt(r -> r.buckets.size()).sum();
    }

}
