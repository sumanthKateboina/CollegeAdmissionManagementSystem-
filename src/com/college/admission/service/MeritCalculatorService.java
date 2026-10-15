package com.college.admission.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service responsible for calculating composite merit scores from academic and entrance exam results.
 * Formula:
 *   Composite Merit Score = (10th % * 0.20) + (12th % * 0.30) + (Entrance Exam % * 0.50)
 */
public class MeritCalculatorService {

    public static final double WEIGHT_10TH = 0.20;
    public static final double WEIGHT_12TH = 0.30;
    public static final double WEIGHT_ENTRANCE = 0.50;

    /**
     * Calculates the composite merit score rounded to 2 decimal places.
     */
    public static double calculateMerit(double score10th, double score12th, double entranceScore) {
        validateScore(score10th, "10th Score");
        validateScore(score12th, "12th Score");
        validateScore(entranceScore, "Entrance Exam Score");

        double raw = (score10th * WEIGHT_10TH) + (score12th * WEIGHT_12TH) + (entranceScore * WEIGHT_ENTRANCE);
        return BigDecimal.valueOf(raw).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculates effective merit score considering optional affirmative action / category concession.
     */
    public static double calculateMeritWithCategory(double score10th, double score12th, double entranceScore, String category) {
        double base = calculateMerit(score10th, score12th, entranceScore);
        // By default pure merit score is preserved, with category stored for quota cutoffs
        return base;
    }

    private static void validateScore(double score, String fieldName) {
        if (score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException(fieldName + " must be between 0.00 and 100.00. Provided: " + score);
        }
    }

    public static String getFormulaDescription() {
        return "Merit Score = (10th % × 20%) + (12th % × 30%) + (Entrance Exam × 50%)";
    }
}
