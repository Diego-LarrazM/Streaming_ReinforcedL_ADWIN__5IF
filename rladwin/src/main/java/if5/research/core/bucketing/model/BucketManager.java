package if5.research.core.bucketing.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import if5.research.core.bucketing.stats.WindowSummary;

public class BucketManager {

    private static final int M = 5; // Max buckets per row

    private final List<LinkedList<Bucket>> rows = new ArrayList<>();

    private final List<Bucket> buckets = new ArrayList<>();
    private final List<Integer> arrivalToBucket = new ArrayList<>();

    // Adds a new value to the bucket manager
    public void update(double value) {
        int arrivalIndex = arrivalToBucket.size();
        Bucket bucket = new Bucket(value);

        bucket.start = arrivalIndex;
        bucket.end = arrivalIndex;

        buckets.add(bucket);
        // track arrival order
        arrivalToBucket.add(buckets.size() - 1);

        insertBucket(bucket, 0);
    }

    // Inserts a new bucket in the bucket rows
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
        merged.end = newer.end;

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


}
