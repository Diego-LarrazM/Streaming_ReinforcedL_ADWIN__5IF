package if5.research.core.bucketing.mock;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.RuntimeExecutionMode;

public class BucketingMockJob {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);
        env.setRuntimeMode(RuntimeExecutionMode.STREAMING);

        env
                .addSource(new RandomEventSource())
                .process(new BucketingTestProcess())
                .print();

        env.execute("Bucketing Mock Test");
    }
}
