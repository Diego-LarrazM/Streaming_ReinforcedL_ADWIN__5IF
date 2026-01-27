package if5.research;

import if5.research.core.flink.Processors.ElectricalEvent;
import if5.research.core.flink.Processors.EventMapper;
import if5.research.core.flink.Processors.StreamProcessor;
import if5.research.core.flink.Processors.SynAbruptEvent;

public class Main {
    public static void main(String[] args) {
        StreamProcessor<ElectricalEvent> processor = new StreamProcessor<>(9999, new EventMapper<>(ElectricalEvent.class));
        //StreamProcessor<SynAbruptEvent> processor = new StreamProcessor<>(9999, new EventMapper<>(SynAbruptEvent.class));
        try {
            processor.execute("Stream Graph Processor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}