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
        // Calculate jounce
        double[] jounce = getJounce(absAccelerationBuffer);
        // Get absolute values
        FFTHelper.abs(jounce);
        // Apply convolution to blur with box kernel
        jounce = FFTHelper.FFTConvolution(jounce, FFTHelper.boxKernel(jounce.length, 3));
        // Binary threshold
        FFTHelper.binaryThreshold(jounce, 5); // TODO: tweak threshold

        // TODO: Triangle filter? Clamp to 1?
        jounce = FFTHelper.FFTConvolution(jounce, FFTHelper.triangleKernel(jounce.length, 10));
        FFTHelper.clampMaxValue(jounce, 1);

        // Check number of taps using threshold?
        // Check first tap at the front of buffer?

        TapPattern pattern = new TapPattern();
        pattern.duration = duration;
        pattern.frequency = frequency;
        pattern.pattern = jounce;
        return pattern;
    }

    // Calculates jounce from acceleration
    public static double[] getJounce(double[] acceleration) {
        double[] sobelKernel = FFTHelper.sobelKernel(acceleration.length, 1);
        double[] snap = FFTHelper.FFTConvolution(acceleration, sobelKernel); // Snap/Jerk
        double[] jounce = FFTHelper.FFTConvolution(snap, sobelKernel); // Jounce
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
