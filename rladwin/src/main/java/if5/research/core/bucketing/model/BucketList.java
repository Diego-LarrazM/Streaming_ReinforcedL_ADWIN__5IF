package if5.research.core.bucketing.model;

import java.util.ArrayList;
import java.util.List;

public class BucketList {

    private static final int MAX_BUCKETS = 2;
    private final List<BucketRow> rows = new ArrayList<>();

    public void insert(double x, double loss) {
        if (rows.isEmpty()) {
            rows.add(new BucketRow());
        }

        rows.get(0).buckets.add(new Bucket(x, loss));
        compress();
    }

    private void compress() {
        int i = 0;

        while (i < rows.size()) {
            BucketRow row = rows.get(i);

            if (row.buckets.size() <= MAX_BUCKETS) {
                i++;
                continue;
            }

            Bucket b1 = row.buckets.remove(0);
            Bucket b2 = row.buckets.remove(0);

            Bucket merged = new Bucket(0, 0);
            merged.merge(b1);
            merged.merge(b2);

            if (rows.size() <= i + 1) {
                rows.add(new BucketRow());
            }

            rows.get(i + 1).buckets.add(merged);
        }
    }

    public List<Bucket> flatten() {
        List<Bucket> all = new ArrayList<>();
        for (BucketRow row : rows) {
            all.addAll(row.buckets);
        }
        return all;
    }
}
