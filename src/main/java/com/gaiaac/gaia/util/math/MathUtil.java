package com.gaiaac.gaia.util.math;

/**
 * Math utility class for anticheat calculations.
 * Provides GCD, statistical analysis, and geometric helpers.
 */
public final class MathUtil {

    private MathUtil() {}

    /**
     * Calculate the Greatest Common Divisor of two values.
     * Used for detecting GCD bypass in rotation checks.
     */
    public static double gcd(double a, double b) {
        if (Math.abs(b) < 1E-6) return a;
        return gcd(b, a % b);
    }

    /**
     * Calculate the Greatest Common Divisor for long values.
     */
    public static long gcd(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    /**
     * Calculate the standard deviation of an array of values.
     */
    public static double standardDeviation(double[] values) {
        if (values.length == 0) return 0;
        double mean = mean(values);
        double sumSquaredDiff = 0;
        for (double v : values) {
            double diff = v - mean;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / values.length);
    }

    /**
     * Calculate the mean of an array of values.
     */
    public static double mean(double[] values) {
        if (values.length == 0) return 0;
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    /**
     * Calculate the variance of an array of values.
     */
    public static double variance(double[] values) {
        if (values.length == 0) return 0;
        double mean = mean(values);
        double sumSquaredDiff = 0;
        for (double v : values) {
            double diff = v - mean;
            sumSquaredDiff += diff * diff;
        }
        return sumSquaredDiff / values.length;
    }

    /**
     * Calculate the kurtosis of an array of values.
     * High kurtosis can indicate bot-like consistency.
     */
    public static double kurtosis(double[] values) {
        if (values.length < 4) return 0;
        double mean = mean(values);
        double std = standardDeviation(values);
        if (std == 0) return 0;

        double sum = 0;
        for (double v : values) {
            double diff = (v - mean) / std;
            sum += diff * diff * diff * diff;
        }
        return (sum / values.length) - 3.0;
    }

    /**
     * Calculate the skewness of an array of values.
     */
    public static double skewness(double[] values) {
        if (values.length < 3) return 0;
        double mean = mean(values);
        double std = standardDeviation(values);
        if (std == 0) return 0;

        double sum = 0;
        for (double v : values) {
            double diff = (v - mean) / std;
            sum += diff * diff * diff;
        }
        return sum / values.length;
    }

    /**
     * Get the number of distinct values in an array.
     */
    public static int distinctCount(double[] values) {
        java.util.Set<Long> seen = new java.util.HashSet<>();
        for (double v : values) {
            seen.add(Double.doubleToLongBits(v));
        }
        return seen.size();
    }

    /**
     * Get the number of duplicates in an array.
     */
    public static int duplicates(double[] values) {
        return values.length - distinctCount(values);
    }

    /**
     * Calculate horizontal distance between two 2D points.
     */
    public static double horizontalDistance(double x1, double z1, double x2, double z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Calculate 3D distance between two points.
     */
    public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Clamp a value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Convert Minecraft sensitivity to mouse delta.
     * Used for GCD analysis in rotation checks.
     */
    public static double sensitivityToMouseDelta(double sensitivity) {
        double f = sensitivity * 0.6F + 0.2F;
        return f * f * f * 1.2F;
    }

    /**
     * Get the angle difference between two angles, accounting for wrapping.
     */
    public static float angleDifference(float a, float b) {
        float diff = ((a - b) % 360.0f + 540.0f) % 360.0f - 180.0f;
        return Math.abs(diff);
    }
}
