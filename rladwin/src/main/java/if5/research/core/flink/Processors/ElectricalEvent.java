package if5.research.core.flink.Processors;

public class ElectricalEvent {

    public Double period;
    public Double new_price;
    public Double new_demand;
    public Double vicprice;
    public Double vicdemand;
    public Double transfer;
    public int classification;

    public ElectricalEvent(String s) {
        // CSV format: parse attributes from source
        String[] parts = s.split(",");
        this.period = Double.parseDouble(parts[0].trim());
        this.new_price = Double.parseDouble(parts[1].trim());
        this.new_demand = Double.parseDouble(parts[2].trim());
        this.vicprice = Double.parseDouble(parts[3].trim());
        this.vicdemand = Double.parseDouble(parts[4].trim());
        this.transfer = Double.parseDouble(parts[5].trim());
        this.classification = Integer.parseInt(parts[6].trim());
    }

    public double getValue() {
        return new_price;
    }

    public String toString() {
        return period + "," + new_price + "," + new_demand + "," + vicprice + "," + vicdemand + "," + transfer + "," + classification;
    }
}
