
package if5.research.core.flink.Processors;

import if5.research.core.bucketing.model.BucketManager;
import if5.research.core.reinforcement.ActorCritic;
import if5.research.core.reinforcement.Rewarder;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.util.Collector;

public class StreamProcessor<Event> {
    private StreamExecutionEnvironment env;
    QueryProcessor queryProcessor;

    class QueryProcessor extends ProcessFunction<Event, String> {

        /*private final BucketManager bucketManager;
        private final ActorCritic actorCritic;
        private final Rewarder rewarder;

        private double[] lastGeneralStats = null;*/

        public QueryProcessor() {
            /*this.bucketManager = new BucketManager();
            this.actorCritic = new ActorCritic(
                    BucketManager.FEATURE_DIM, // feature dim
                    0.001,  // actor lr
                    0.001,  // critic lr
                    0.99    // gamma
            );
            this.rewarder = new Rewarder(
                    0.01,   // latency_alpha
                    0.5,    // split_cost
                    0.01,   // exploration_alpha
                    0.01,   // trend_gamma
                    1.0,    // initial_baseline_loss
                    5.0,    // kl_max
                    1.0,    // loss_max
                    -1.0,   // loss_min
                    1000.0  // tau
            );*/
        }

        @Override
        public void processElement(Event event,Context ctx,Collector<String> out) {
            /*
            double x = extractValue(event);

            bucketManager.update(x);

            double[][] splitFeatures = bucketManager.buildSplitFeatureBatch(2);

            double[] generalStats = bucketManager.getGeneralStats();

            int action = actorCritic.decide(splitFeatures, generalStats);

            bucketManager.applyAction(action);

            double reward = 0.0;

            if (lastGeneralStats != null) {
                int window_size = (int) generalStats[3];
                int did_split = (action == 0) ? 0 : 1;

                double meanOld = lastGeneralStats[1];
                double varOld  = lastGeneralStats[2];

                double meanNew = generalStats[1];
                double varNew  = generalStats[2];

                double loss_before = varOld;
                double loss_after  = varNew;

                double[] actionProbs = actorCritic.getLastActionProbs();

                reward = rewarder.computeReward(window_size,did_split,meanOld,varOld,meanNew,varNew,loss_before,loss_after,actionProbs);
            }

            actorCritic.update(reward, generalStats);

            lastGeneralStats = generalStats;

            out.collect("action=" + action + " reward=" + reward);*/
            out.collect("testing");
        }

    }

    public StreamProcessor(int port, EventMapper<Event> mapper) {
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.env.setParallelism(1);
        this.env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        this.queryProcessor = new QueryProcessor();

        DataStream<String> socketStream = this.env.socketTextStream("localhost", 9999);
        socketStream
                .filter(s -> (s != "" && s != null))
                .map(mapper)
                .returns(mapper.getEventClass()) // Vu que c'est générique vaut l'indiquer la classe de retour
                .process(this.queryProcessor)
                .print();
    }

    public void execute(String job_name) throws Exception {
        this.env.execute(job_name);
    }

    private double extractValue(Object event) {

        if (event instanceof SynGradualEvent) {
            return ((SynGradualEvent) event).syntheticGradualValue;
        }

        if (event instanceof SynAbruptEvent) {
            return ((SynAbruptEvent) event).syntheticAbruptValue;
        }

        if (event instanceof ElectricalEvent) {
            return ((ElectricalEvent) event).new_price;
        }

        throw new IllegalArgumentException(
                "Unsupported event type: " + event.getClass()
        );
    }

}