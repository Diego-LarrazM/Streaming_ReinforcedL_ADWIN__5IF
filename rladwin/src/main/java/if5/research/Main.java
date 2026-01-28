package if5.research;
import if5.research.core.flink.Processors.ElecStreamProcessor;

public class Main {
    public static void main(String[] args) {
        ElecStreamProcessor processor = new ElecStreamProcessor(9999);
        //StreamProcessor<SynAbruptEvent> processor = new StreamProcessor<>(9999, new EventMapper<>(SynAbruptEvent.class));
        try {
            processor.execute("Stream Graph Processor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}