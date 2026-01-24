package if5.research.core.bucketing.mock;

import org.apache.flink.streaming.api.functions.source.legacy.SourceFunction;

import java.util.Random;

public class RandomEventSource implements SourceFunction<MockEvent> {

    private volatile boolean running = true;
    private final Random rnd = new Random();

    @Override
    public void run(org.apache.flink.streaming.api.functions.source.legacy.SourceFunction.SourceContext<MockEvent> ctx) throws Exception {
        long t = System.currentTimeMillis();

        while (running) {
            double value = rnd.nextGaussian();               // data
            double prediction = value + rnd.nextGaussian()*0.1;
            double loss = Math.abs(value - prediction);

            ctx.collect(new MockEvent(
                    value,
                    loss,
                    prediction,
                    t
            ));

            t += 100; // 100 ms spacing
            Thread.sleep(100);
        }
    }

    @Override
    public void cancel() {
        running = false;
    }
}
