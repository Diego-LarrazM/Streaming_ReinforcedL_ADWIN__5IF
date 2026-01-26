package if5.research.core.bucketing.mock;

import if5.research.core.bucketing.model.BucketManager;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

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
        bucketManager.update(
                event.value
        );

    }
}
