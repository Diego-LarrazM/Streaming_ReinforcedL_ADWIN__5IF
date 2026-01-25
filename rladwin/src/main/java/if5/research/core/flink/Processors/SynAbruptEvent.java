package if5.research.core.flink.Processors;

public class SynAbruptEvent {
    public long timestamp;
    public long syntheticAbruptValue;
    public String type;

    public SynAbruptEvent(String s){
        // CSV format: parse attributes from source
        String[] parts = s.split(",");
        this.timestamp = Long.parseLong(parts[0].trim());
        this.syntheticAbruptValue = Long.parseLong(parts[1].trim());
        this.type = parts[2].trim();
    }

}
