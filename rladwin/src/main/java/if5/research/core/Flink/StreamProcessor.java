package if5.research.core.Flink;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.Collector;

import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;

public class StreamProcessor {

    StreamExecutionEnvironment env;
    DataStream<Object> streamGraph; // to change to parsed obj

    class GraphExpirer extends KeyedProcessFunction<Integer, EventFormat, String> {
        private final long WINDOW_SIZE;
        // Define data structures to sava data

        public GraphExpirer(long windowSize) {
            this.WINDOW_SIZE = windowSize;
        }

        @Override
        public void open(OpenContext ctx) throws Exception {
            // Intialise data structures
        }

        @Override
        public void processElement(
            EventFormat edge_event, 
            Context context,
            Collector<String> out) throws Exception {
            // Processing of each event (Object + timestamp + Watermark)

            // Update State
            Object obj = edge_event.obj; // to change to parsed obj
            obj.setExpiricy(Instant.ofEpochMilli(edge_event.timestamp + WINDOW_SIZE));

            // Register expiration timer to call onTimer when currentWatermark >= expirationTimer
            context.timerService().registerEventTimeTimer(obj.getExpiricy_ms());
        }

        @Override
        public void onTimer(
            long timestamp,
            OnTimerContext context,
            Collector<String> out) throws Exception {
            // Timer triggered when currentWatermark >= expirationTimer
            // To expire
        }
    }

    public StreamProcessor(long window_size, int watermarkDelta) {
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.env.setParallelism(1);
        this.env.setRuntimeMode(RuntimeExecutionMode.STREAMING);

        DataStream<String> socketStream = this.env.socketTextStream("localhost", 8080);
        socketStream
        .filter(s->(s != "" && s != null))
        .map(EventFormat::new)
        .assignTimestampsAndWatermarks(
            WatermarkStrategy //Sets up time for expiration given event start times and lateness available
            .<EventFormat>forBoundedOutOfOrderness(Duration.ofMillis(watermarkDelta))
            .withTimestampAssigner((event, ts) -> event.timestamp))
        .keyBy(edge -> 0)
        .process(new GraphExpirer(window_size))
        .print();
    }

    public void execute(String job_name) throws Exception {
        this.env.execute(job_name);
    }

}