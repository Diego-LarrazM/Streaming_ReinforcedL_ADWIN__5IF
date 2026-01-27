package if5.research.core.bucketing.mock;

import java.util.Random;
import java.util.stream.Stream;

import if5.research.core.bucketing.model.BucketManager;

public class BucketingMockJob {

    public static void main(String[] args) {
        BucketManager bm = new BucketManager();
        Random rand = new Random();

        System.out.println("Stream started...");

        Stream.generate(rand::nextGaussian)
                .limit(11)
                .forEach(bm::update);

        System.out.println(bm.toString());
        System.out.println("Stream finished...");
    }
}
