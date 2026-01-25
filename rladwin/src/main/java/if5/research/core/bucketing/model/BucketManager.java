package if5.research.core.bucketing.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BucketManager {

    private static final int M = 5; // Max buckets per row
    private final List<LinkedList<Bucket>> rows = new ArrayList<>();

    public void update(double value) {
        // TODO: Calculate actual loss based on your application logic
        // For now, using 1.0 as default loss
        Bucket bucket = new Bucket(value, 1.0);
        insertBucket(bucket, 0);
    }

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
}
