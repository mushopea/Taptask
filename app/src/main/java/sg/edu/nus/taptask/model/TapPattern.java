package sg.edu.nus.taptask.model;

import sg.edu.nus.taptask.FFTHelper;

/**
 * Class representing a tap pattern
 */
public class TapPattern {
    public static final double MATCH_PERCENTAGE_THRESHOLD = 0.8; // TODO: test what values are good or let user decide.

    public double duration;
    public double frequency;
    public double[] pattern;


    public static TapPattern createPattern(double[] absAccelerationBuffer, double duration, double frequency) {
        // TODO: implement this
        // Calculate jounce
        double[] jounce = getJounce(absAccelerationBuffer);
        // Apply convolution with gaussian kernel

        // Check number of taps using threshold?
        // Check first tap at the front of buffer?


        return null;
    }

    // Calculates jounce from acceleration
    public static double[] getJounce(double[] acceleration) {
        double[] jounce = FFTHelper.FFTConvolution(acceleration, FFTHelper.sobelKernel(acceleration.length, 1)); // Snap/Jerk
        jounce = FFTHelper.FFTConvolution(jounce, FFTHelper.sobelKernel(jounce.length,1)); // Jounce
        return jounce;
    }

    public boolean matchPattern(TapPattern pattern) {
        return matchPatternPercentage(pattern) >= MATCH_PERCENTAGE_THRESHOLD;
    }

    public double matchPatternPercentage(TapPattern pattern) {
        // TODO: implement this
        return 0;
    }

}
