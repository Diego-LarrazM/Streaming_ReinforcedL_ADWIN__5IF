package if5.research;

import if5.research.core.flink.Processors.StreamProcessor;

public class Main {
    public static void main(String[] args) {
        StreamProcessor processor = new StreamProcessor();
        try {
            processor.execute("Stream Graph Processor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}