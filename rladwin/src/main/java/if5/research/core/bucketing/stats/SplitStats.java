package if5.research.core.bucketing.stats;

public class SplitStats {
    public final WindowSummary left;
    public final WindowSummary right;

    public SplitStats(WindowSummary left, WindowSummary right) {
        this.left = left;
        this.right = right;
    }
}

