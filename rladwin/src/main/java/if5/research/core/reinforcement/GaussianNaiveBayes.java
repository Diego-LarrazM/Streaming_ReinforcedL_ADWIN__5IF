package if5.research.core.reinforcement;

import if5.research.core.flink.Processors.ElectricalEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


public class GaussianNaiveBayes {
    
    private double[][] means; // [classe][attribut]
    private double[][] variances;
    private double[] priorProbabilities;
    private final int numClasses = 2; // UP (1) et DOWN (0)
    private final int numFeatures = 6; // period, nswprice, nswdemand, vicprice, vicdemand, transfer
    private final double epsilon = 1e-6; // Pour éviter la division par zéro dans la variance

    public GaussianNaiveBayes() {
        this.means = new double[numClasses][numFeatures];
        this.variances = new double[numClasses][numFeatures];
        this.priorProbabilities = new double[numClasses];
    }

    /**
     * Entraîne le modèle sur les événements contenus dans la fenêtre actuelle.
     */
    public void train(List<ElectricalEvent> events) {
        if (events == null || events.isEmpty()) return;

        Map<Integer, List<double[]>> classGroups = new HashMap<>();
        for (int i = 0; i < numClasses; i++) classGroups.put(i, new ArrayList<>());

        // 1. Grouper les données par classe
        for (ElectricalEvent e : events) {
            double[] f = {e.period, e.new_price, e.new_demand, e.vicprice, e.vicdemand, e.transfer};
            classGroups.get(e.classification).add(f);
        }

        // 2. Calculer les statistiques pour chaque classe
        for (int c = 0; c < numClasses; c++) {
            List<double[]> data = classGroups.get(c);
            int n = data.size();
            priorProbabilities[c] = (double) n / events.size();

            if (n > 0) {
                // Calcul des moyennes
                for (int j = 0; j < numFeatures; j++) {
                    double sum = 0;
                    for (double[] row : data) sum += row[j];
                    means[c][j] = sum / n;
                }

                // Calcul des variances
                for (int j = 0; j < numFeatures; j++) {
                    double varSum = 0;
                    for (double[] row : data) {
                        varSum += Math.pow(row[j] - means[c][j], 2);
                    }
                    variances[c][j] = (varSum / n) + epsilon;
                }
            }
        }
    }

    /**
     * Calcule la Loss (Negative Log-Likelihood).
     * Utilisé par le Rewarder pour voir si un split améliore la cohérence du modèle.
     */
    public double calculateLoss(List<ElectricalEvent> events) {
        if (events == null || events.isEmpty()) return 0.0;

        double totalNegativeLogLikelihood = 0.0;

        for (ElectricalEvent e : events) {
            double[] f = {e.period, e.new_price, e.new_demand, e.vicprice, e.vicdemand, e.transfer};
            int actualClass = e.classification;

            // Calcul de P(Features | Classe Réelle)
            double logProb = Math.log(priorProbabilities[actualClass] + epsilon);
            for (int j = 0; j < numFeatures; j++) {
                logProb += calculateGaussianLogProb(f[j], means[actualClass][j], variances[actualClass][j]);
            }
            
            totalNegativeLogLikelihood -= logProb;
        }

        return totalNegativeLogLikelihood / events.size();
    }

    /**
     * Prédit la classe la plus probable pour un événement.
     */
    public int predict(ElectricalEvent e) {
        double[] f = {e.period, e.new_price, e.new_demand, e.vicprice, e.vicdemand, e.transfer};
        double bestLogPost = -Double.MAX_VALUE;
        int bestClass = 0;

        for (int c = 0; c < numClasses; c++) {
            double logPost = Math.log(priorProbabilities[c] + epsilon);
            for (int j = 0; j < numFeatures; j++) {
                logPost += calculateGaussianLogProb(f[j], means[c][j], variances[c][j]);
            }
            if (logPost > bestLogPost) {
                bestLogPost = logPost;
                bestClass = c;
            }
        }
        return bestClass;
    }

    private double calculateGaussianLogProb(double x, double mean, double var) {
        double exponent = -Math.pow(x - mean, 2) / (2 * var);
        return -0.5 * Math.log(2 * Math.PI * var) + exponent;
    }
}