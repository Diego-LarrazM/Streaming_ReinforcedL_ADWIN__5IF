package if5.research.core.bucketing.stats;

public class StatisticalFeatures {

    public static final int FEATURE_DIM = 10;

    public static double[] extract(WindowSummary oldW, WindowSummary newW) {

        double eps = 1e-8;

        double meanDiff = newW.mean - oldW.mean;

        double varDiff = (newW.variance - oldW.variance)
                / (oldW.variance + eps);

        double oldQ25 = oldW.quantiles.q25();
        double oldQ50 = oldW.quantiles.q50();
        double oldQ75 = oldW.quantiles.q75();

        double newQ25 = newW.quantiles.q25();
        double newQ50 = newW.quantiles.q50();
        double newQ75 = newW.quantiles.q75();

        double iqr = Math.max(eps, oldQ75 - oldQ25);

        double q25Diff = (newQ25 - oldQ25) / iqr;
        double q50Diff = (newQ50 - oldQ50) / iqr;
        double q75Diff = (newQ75 - oldQ75) / iqr;

        double wasserstein =
                oldW.histogram.wassersteinDistance(newW.histogram);

        double lossTrend = newW.lossEMA - oldW.lossEMA;
        double predShift = newW.prediction - oldW.prediction;

        return new double[] {
                meanDiff,
                Math.abs(meanDiff),

                varDiff,
                Math.abs(varDiff),

                q25Diff,
                q50Diff,
                q75Diff,

                wasserstein,

                lossTrend,
                predShift
        };
    }
}
