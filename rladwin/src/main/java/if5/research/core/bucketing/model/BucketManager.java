package if5.research.core.bucketing.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BucketManager {

    private static final int M = 5; // Max buckets per row
    private final List<LinkedList<Bucket>> rows = new ArrayList<>();

    // Adds a new value to the bucket manager
    public void update(double value) {
        Bucket bucket = new Bucket(value);
        insertBucket(bucket, 0);
    }

    // Inserts a new bucket in the bucket rows
    private void insertBucket(Bucket bucket, int rowIndex) {
        while (rows.size() <= rowIndex) {
            rows.add(new LinkedList<>());
        }

        // Insert bucket at head (newest)
        LinkedList<Bucket> row = rows.get(rowIndex);
        row.addFirst(bucket);

        // If the row is too long
        if (row.size() > M) {
            Bucket older = row.removeLast();
            Bucket newer = row.removeLast();
            Bucket merged = Bucket.merge(older, newer);
            insertBucket(merged, rowIndex + 1);
        }
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
