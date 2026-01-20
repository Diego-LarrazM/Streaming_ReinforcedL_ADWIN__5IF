package if5.research.core.Flink;

import java.time.Instant;

public class EventFormat {
    public Object obj; // to change to actual used type
    public long timestamp;

    public EventFormat(String s){
        // CSV format: parse attributes from source
        String[] parts = s.split(";");
        this.timestamp = Long.parseLong(parts[3].trim());

        this.obj = new Object(); // set up object from parsing
    }
}