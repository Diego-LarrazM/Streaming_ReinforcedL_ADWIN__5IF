
package if5.research.core.flink.Processors;

import if5.research.core.bucketing.model.BucketManager;
import if5.research.core.reinforcement.ActorCritic;
import if5.research.core.reinforcement.Rewarder;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.util.Collector;

public class ElecStreamProcessor {
    private StreamExecutionEnvironment env;
    QueryProcessor queryProcessor;

    static class QueryProcessor extends KeyedProcessFunction<Integer, ElectricalEvent, String> {

        private BucketManager bucketManager;
        private ActorCritic actorCritic;
        private Rewarder rewarder;

        private double[] lastGeneralStats = null;

        @Override
        public void open(OpenContext openContext) throws Exception {
            this.bucketManager = new BucketManager();
            this.actorCritic = new ActorCritic(
                    BucketManager.FEATURE_DIM,
                    6,
                    0.001,
                    0.001,
                    0.99
            );
            this.rewarder = new Rewarder(
                    0.01, 0.5, 0.01, 0.01,
                    1.0, 5.0, 1.0, -1.0, 1000.0
            );
        }

        @Override
        public void processElement(ElectricalEvent event,Context ctx,Collector<String> out) {
            double x = event.getValue();

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

            out.collect("action=" + action + " reward=" + reward);
            
            out.collect("BUCKET:\n" 
                        + bucketManager.toString()
                        + "STATS: " + splitFeatures.length
                        + "\nGENERALSTATS: " + generalStats.length + "\n------------------");
        }

    }

    public ElecStreamProcessor(int port) {
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.env.setParallelism(1);
        this.env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        this.queryProcessor = new QueryProcessor();

        DataStream<String> socketStream = this.env.socketTextStream("localhost", 9999);
        socketStream
                .filter(s -> (s != "" && s != null))
                .map(ElectricalEvent::new)
                .keyBy(t -> 0) // Tous les événements dans la même clé pour un seul processFunction
                .process(this.queryProcessor)
                .print();
    }

    public void execute(String job_name) throws Exception {
        this.env.execute(job_name);
    }

}