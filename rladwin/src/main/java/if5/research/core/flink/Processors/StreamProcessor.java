package if5.research.core.flink.Processors;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.util.Collector;

public class StreamProcessor {
    private StreamExecutionEnvironment env;
    QueryProcessor queryProcessor;

    class QueryProcessor extends KeyedProcessFunction<Integer, ElectricalEvent, String>{
        public QueryProcessor() {
            // Initialize query processor
        }

        @Override
        public void processElement(ElectricalEvent event, Context ctx, Collector<String> out) {
            // Process each ElectricalEvent and produce output
            String result = "Processed Event: " + event.period + ", " + event.new_price + ", " + event.new_demand + ", " + event.vicprice + ", " + event.vicdemand + ", " + event.transfer + ", " + event.classification;
            System.out.println(result);
            out.collect(result);
        }
    }

    public StreamProcessor() {
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.env.setParallelism(1);
        this.env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        this.queryProcessor = new QueryProcessor();

        DataStream<String> socketStream = this.env.socketTextStream("localhost", 9999);
        socketStream
        .filter(s->(s != "" && s != null))
        .map(ElectricalEvent::new)
        .keyBy(ElectricalEvent -> 1) // single key to process all events together
        .process(this.queryProcessor)
        .print();
    }

    public void execute(String job_name) throws Exception {
        this.env.execute(job_name);
    }
}