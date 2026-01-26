package if5.research.core.bucketing.mock;

import if5.research.core.bucketing.model.BucketManager;

public class BucketTest {
    public static void main(String[] args) {
        BucketManager bm = new BucketManager();

        java.util.Random rand = new java.util.Random();

        System.out.println("Stream started...");

        for (int i = 0; i < 11; i++) {
            double value = rand.nextGaussian();
            bm.update(value);
        }
        System.out.println(bm.toString());
        System.out.println("Stream finished...");

    }
}
