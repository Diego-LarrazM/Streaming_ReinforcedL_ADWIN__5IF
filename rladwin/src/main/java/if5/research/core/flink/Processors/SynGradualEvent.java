package if5.research.core.flink.Processors;

public class SynGradualEvent {
    public long timestamp;
    public long syntheticGradualValue;
    public String type;

    public SynGradualEvent(String s){
        // CSV format: parse attributes from source
        String[] parts = s.split(",");
        this.timestamp = Long.parseLong(parts[0].trim());
        this.syntheticGradualValue = Long.parseLong(parts[1].trim());
        this.type = parts[2].trim();
    }
}
