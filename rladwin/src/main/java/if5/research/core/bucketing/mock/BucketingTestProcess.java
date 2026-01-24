package if5.research.core.bucketing.mock;

import if5.research.core.bucketing.model.BucketManager;
import if5.research.core.bucketing.stats.StatisticalFeatures;
import if5.research.core.bucketing.stats.WindowSummary;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.List;

public class BucketingTestProcess
        extends ProcessFunction<MockEvent, String> {

    private transient BucketManager bucketManager;

    @Override
    public void open(org.apache.flink.api.common.functions.OpenContext ctx) {
        bucketManager = new BucketManager();
    }

    @Override
    public void processElement(
            MockEvent event,
            Context ctx,
            Collector<String> out) {

        // Insert into buckets
        bucketManager.insert(
                event.value,
                event.loss
        );

        // Occasionally test splits
        if (bucketManager.getTotalCount() % 20 == 0) {

            List<Integer> splits = bucketManager.candidateSplits();

            for (int s : splits) {
                WindowSummary[] ws = bucketManager.getSplitSummaries(s);
                double[] features =
                        StatisticalFeatures.extract(ws[0], ws[1]);

                out.collect(
                        "Split@" + s +
                                " | meanDiff=" + features[0] +
                                " | varDiff=" + features[2] +
                                " | wasserstein=" + features[7]
                );
            }

            out.collect(
                    "Buckets=" + bucketManager.getNumberOfBuckets() +
                            " TotalCount=" + bucketManager.getTotalCount()
            );
        }
    }
}
