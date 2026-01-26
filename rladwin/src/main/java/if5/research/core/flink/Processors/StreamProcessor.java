package if5.research.core.flink.Processors;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.util.Collector;

public class StreamProcessor<Event> {
    private StreamExecutionEnvironment env;
    QueryProcessor queryProcessor;

    class QueryProcessor extends ProcessFunction<Event, String>{
        public QueryProcessor() {
            // Initialize query processor
        }

        @Override
        public void processElement(Event event, Context ctx, Collector<String> out) {
            // Process each Event and produce output
            String result = "Processed event:" + event.toString();
            out.collect(result);
        }
    }

    public StreamProcessor(int port, EventMapper<Event> mapper) {
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.env.setParallelism(1);
        this.env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        this.queryProcessor = new QueryProcessor();

        DataStream<String> socketStream = this.env.socketTextStream("localhost", 9999);
        socketStream
        .filter(s->(s != "" && s != null))
        .map(mapper)
        .returns(mapper.getEventClass()) // Vu que c'est générique vaut l'indiquer la classe de retour
        .process(this.queryProcessor)
        .print();
    }

    public void execute(String job_name) throws Exception {
        this.env.execute(job_name);
    }
}