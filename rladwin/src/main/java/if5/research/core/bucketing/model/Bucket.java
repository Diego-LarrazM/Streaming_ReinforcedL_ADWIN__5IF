package if5.research.core.bucketing.model;

import if5.research.core.bucketing.stats.Histogram;
import if5.research.core.bucketing.stats.Quantiles;

public class Bucket {

    public int n;
    public double sum;
    public double sumSquares;

    public Histogram histogram;
    public Quantiles quantiles;

    public double lossEMA;

    public Bucket(double x, double loss) {
        this.n = 1;
        this.sum = x;
        this.sumSquares = x * x;

        this.histogram = new Histogram(x);
        this.quantiles = new Quantiles(x);

        this.lossEMA = loss;
    }

    public void merge(Bucket other) {
        this.n += other.n;
        this.sum += other.sum;
        this.sumSquares += other.sumSquares;

        this.histogram.merge(other.histogram);
        this.quantiles.merge(other.quantiles);

        this.lossEMA = 0.9 * this.lossEMA + 0.1 * other.lossEMA; // need to check mathematically if this is how we merge lossema
    }
}
