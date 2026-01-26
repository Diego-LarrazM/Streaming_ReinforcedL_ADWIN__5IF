package if5.research.core.bucketing.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import if5.research.core.bucketing.stats.StatisticalFeatures;

public class BucketManager {

    private static final int M = 5; // Max buckets per row

    private final List<LinkedList<Bucket>> rows = new ArrayList<>();

    private final List<Bucket> buckets = new ArrayList<>();
    private final List<Integer> arrivalToBucket = new ArrayList<>();

    public void update(double value) {
        int arrivalIndex = arrivalToBucket.size();
        // TODO: Calculate actual loss based on your application logic
        // For now, using 1.0 as default loss
        Bucket bucket = new Bucket(value, 1.0);

        bucket.start = arrivalIndex;
        bucket.end = arrivalIndex;

        buckets.add(bucket);
        // track arrival order
        arrivalToBucket.add(buckets.size() - 1);

        insertBucket(bucket, 0);
    }

    private void insertBucket(Bucket bucket, int rowIndex) {
        while (rows.size() <= rowIndex) {
            rows.add(new LinkedList<>());
        }

        LinkedList<Bucket> row = rows.get(rowIndex);
        row.addFirst(bucket);

        if (row.size() > M) {
            Bucket older = row.removeLast();
            Bucket newer = row.removeLast();
            Bucket merged = mergeBuckets(older, newer);
            insertBucket(merged, rowIndex + 1);
        }
    }

    private Bucket mergeBuckets(Bucket older, Bucket newer) {

        Bucket merged = Bucket.merge(older, newer);
        merged.start = older.start;
        merged.end   = newer.end;

        int idxOlder = buckets.indexOf(older);
        int idxNewer = buckets.indexOf(newer);

        buckets.set(idxOlder, merged);

        buckets.remove(idxNewer);

        for (int i = merged.start; i <= merged.end; i++) {
            arrivalToBucket.set(i, idxOlder);
        }

        for (int i = 0; i < arrivalToBucket.size(); i++) {
            int idx = arrivalToBucket.get(i);
            if (idx > idxNewer) {
                arrivalToBucket.set(i, idx - 1);
            }
        }

        return merged;
    }

    public WindowSummary summarizeRange(int from, int to) {

        WindowSummary ws = new WindowSummary();
        int lastBucket = -1;

        for (int i = from; i <= to; i++) {
            int bIdx = arrivalToBucket.get(i);

            if (bIdx != lastBucket) {
                mergeInto(ws, buckets.get(bIdx));
                lastBucket = bIdx;
            }
        }
        return ws;
    }

    private void mergeInto(WindowSummary ws, Bucket b) {
        int n0 = ws.n;
        int n1 = (int) b.count;
        int n = n0 + n1;

        if (n == 0) return;

        double mean = (ws.mean * n0 + b.sum) / n;

        double var =
                (n0 * ws.variance +
                        b.count * b.variance +
                        (n0 * b.count * Math.pow(ws.mean - b.sum / b.count, 2)) / n)
                        / n;

        ws.n = n;
        ws.mean = mean;
        ws.variance = var;

        ws.quantiles.merge(b.tDigest);
        ws.histogram.merge(b);
        ws.lossEMA = Math.max(ws.lossEMA, b.lossEMA);
    }

    public double[][] buildSplitFeatureBatch(List<Integer> candidateSplits) {

        int K = candidateSplits.size();
        double[][] features = new double[K + 1][StatisticalFeatures.FEATURE_DIM];

        // Action 0 = WAIT
        features[0] = StatisticalFeatures.waitAction();

        for (int i = 0; i < K; i++) {
            int split = candidateSplits.get(i);

            WindowSummary oldW = summarizeRange(0, split);
            WindowSummary newW = summarizeRange(split + 1, arrivalToBucket.size() - 1);


            features[i + 1] = StatisticalFeatures.extract(oldW, newW);
        }

        return features;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LinkedList<Bucket> row : rows) {
            for (Bucket bucket : row) {
                sb.append(bucket.toString());
                sb.append("\n");
            }
            sb.append("\n"); // Separate rows with newline
        }
        return sb.toString();
    }

    public void discardUpToArrival(int splitArrival) {

        int bucketIdx = arrivalToBucket.get(splitArrival);
        Bucket cutBucket = buckets.get(bucketIdx);
        int cutArrival = cutBucket.end;

        arrivalToBucket.subList(0, cutArrival + 1).clear();

        buckets.subList(0, bucketIdx + 1).clear();

        for (int i = 0; i < arrivalToBucket.size(); i++) {
            arrivalToBucket.set(i, arrivalToBucket.get(i) - (bucketIdx + 1));
        }

        rebuildRows();
    }

    private void rebuildRows() {
        rows.clear();
        for (Bucket b : buckets) {
            insertBucket(b, 0);
        }
    }

}
