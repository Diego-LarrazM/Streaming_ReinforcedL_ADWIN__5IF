package if5.research.core.reinforcement;

public class RLOps {

    public static double sigmoid(double x) { // double x -> [0,1]
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static double dot(double[] a, double[] b) {
        double result = 0.0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }

    public static double max(double[] array) {
        double maxVal = Double.NEGATIVE_INFINITY;
        for (double val : array) {
            if (val > maxVal) {
                maxVal = val;
            }
        }
        return maxVal;
    }

    public static int argmax(double[] array){
        int iMax = 0;
        double maxVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++){
            double val = array[i];
            if(val > maxVal){
                maxVal = val;
                iMax = i;
            }
        }
        return iMax;
    }

    public static double[] softmax(double[] logits) {
        double maxLogit = max(logits); // to improve numerical stability, preventing overflow

        double sumExp = 0.0;
        double[] expValues = new double[logits.length];
        for (int i = 0; i < logits.length; i++) {
            expValues[i] = Math.exp(logits[i] - maxLogit);
            sumExp += expValues[i];
        }

        double[] softmaxValues = new double[logits.length];
        for (int i = 0; i < logits.length; i++) {
            softmaxValues[i] = expValues[i] / sumExp;
        }

        return softmaxValues;
    }
    
}
