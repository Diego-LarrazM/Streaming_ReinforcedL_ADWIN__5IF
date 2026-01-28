package if5.research.core.bucketing.stats;

import com.tdunning.math.stats.MergingDigest;

public class WindowSummary {

    public int n;
    public double mean;
    public double variance;

    public MergingDigest digest;

    public WindowSummary() {
        this.n = 0;
        this.mean = 0;
        this.variance = 0;
        this.digest = null;
    }
}
