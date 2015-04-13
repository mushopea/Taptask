package sg.edu.nus.taptask.model;

import android.util.Log;

import java.util.ArrayList;

import sg.edu.nus.taptask.FFTHelper;

/**
 * Class representing a tap pattern
 */
public class TapPattern {
    public static final double MATCH_PERCENTAGE_THRESHOLD = 0.75; // TODO: test what values are good or let user decide.

    public double duration;
    public double frequency;
    public double[] pattern;



    private transient static double[] boxKernel = null;
    private transient static double[] triangleKernel = null;
    public static TapPattern createPattern(double[] absAccelerationBuffer, double duration, double frequency) {
        return createPattern(absAccelerationBuffer, duration, frequency, null);
    }
    public static TapPattern createPattern(double[] absAccelerationBuffer, double duration, double frequency, TapPattern pattern) {
        // Calculate jounce
        double[] jounce = getJounce(absAccelerationBuffer);
        // Get absolute values
        FFTHelper.abs(jounce);
        // Binary threshold
        if (frequency < AccelerometerConfig.getInstance(null).minSamplingFrequency){
            FFTHelper.binaryThreshold(jounce, 6);
        } else {
            // Apply convolution to blur with box kernel
            if (boxKernel == null || boxKernel.length != jounce.length) {
                boxKernel = FFTHelper.boxKernel(jounce.length, 3);
                Log.e("new", "boxKernel");
            }
            jounce = FFTHelper.FFTConvolution(jounce, boxKernel);
            FFTHelper.binaryThreshold(jounce, 6); // TODO: tweak threshold
        }
        // Triangle filter
        if (triangleKernel == null || triangleKernel.length != jounce.length) {
            triangleKernel = FFTHelper.triangleKernel(jounce.length, 10); // TODO: tweak kernel size
            Log.e("new", "triangleKernel");
        }
        jounce = FFTHelper.FFTConvolution(jounce, triangleKernel);
        // Clamp max value
        FFTHelper.clampMaxValue(jounce, 1);

        // Check number of taps using threshold?
        // Check first tap at the front of buffer?

        if (pattern == null) {
            pattern = new TapPattern();
            Log.e("new", "TapPattern");
        }
        pattern.duration = duration;
        pattern.frequency = frequency;
        pattern.pattern = jounce;
        return pattern;
    }

    // Calculates jounce from acceleration
    private transient static double[] sobelKernel = null;
    public static double[] getJounce(double[] acceleration) {
        if (sobelKernel == null || sobelKernel.length != acceleration.length) {
            sobelKernel = FFTHelper.sobelKernel(acceleration.length, 1); // TODO: tweak kernel size
            Log.e("new", "sobelKernel");
        }
        double[] jounce = FFTHelper.FFTConvolution(acceleration, sobelKernel, 2); // Jounce (m/s^4)
        return jounce;
    }

    public boolean matchPattern(TapPattern pattern) {
        return matchPatternPercentage(pattern) >= MATCH_PERCENTAGE_THRESHOLD;
    }

    public double matchPatternPercentage(TapPattern tapPattern) {
        double[] pattern = FFTHelper.padWithZeros(this.pattern, this.pattern.length*2, null);
        double[] signal = FFTHelper.padWithZeros(tapPattern.pattern, tapPattern.pattern.length*2, null);
        if (this.pattern.length != signal.length) {
            // Should not happen
            Log.i("TapPattern", "matchPatternPercentage: pattern length mismatch");
        }
        // TODO: pad with zeros to possibly make correlation result better?

        double[] correlationResult = FFTHelper.FFTCorrelation(
                signal, pattern);
        int maxIndex = FFTHelper.maxIndex(correlationResult);
        if (maxIndex == -1) {
            return 0;
        }
        double maxValue = correlationResult[maxIndex];
        double normalizingFactor = FFTHelper.absSquareSum(pattern);
        double pctMatch = maxValue / normalizingFactor;

        Log.i("TapPattern", "Best match at pos: " + maxIndex + ", value: " + maxValue + ", pct: " + pctMatch);
        Log.i("TapPattern", "AbsSum corr result: " + FFTHelper.absSum(correlationResult));
        Log.i("TapPattern", "AbsSum pattern: " + FFTHelper.absSum(pattern));
        Log.i("TapPattern", "AbsSum signal: " + FFTHelper.absSum(signal));
        Log.i("TapPattern", "AbsSquareSum pattern: " + FFTHelper.absSquareSum(pattern));
        Log.i("TapPattern", "AbsSquareSum signal: " + FFTHelper.absSquareSum(signal));
        return pctMatch;
    }

    public double matchSignalPercentage(double[] signal) {
        double[] pattern = this.pattern;
        if (this.pattern.length != signal.length) {

            if (this.pattern.length > signal.length) {
                // Something wrong..
                Log.e("TapPattern", "Signal length shorter than pattern length.");
            } else {
                // TODO: Pad with twice the length
                pattern = FFTHelper.padWithZeros(pattern, signal.length, null);
            }
        }

        double[] correlationResult = FFTHelper.FFTCorrelation(
                signal, pattern);
        int maxIndex = FFTHelper.maxIndex(correlationResult);
        if (maxIndex == -1) {
            return 0;
        }
        double maxValue = correlationResult[maxIndex];
        double normalizingFactor = FFTHelper.absSquareSum(pattern);
        double pctMatch = maxValue / normalizingFactor;

        Log.i("TapPattern", "Best match at pos: " + maxIndex + ", value: " + maxValue + ", pct: " + pctMatch);
        Log.i("TapPattern", "AbsSum corr result: " + FFTHelper.absSum(correlationResult));
        Log.i("TapPattern", "AbsSum pattern: " + FFTHelper.absSum(pattern));
        Log.i("TapPattern", "AbsSum signal: " + FFTHelper.absSum(signal));
        Log.i("TapPattern", "AbsSquareSum pattern: " + FFTHelper.absSquareSum(pattern));
        Log.i("TapPattern", "AbsSquareSum signal: " + FFTHelper.absSquareSum(signal));

        return pctMatch;
    }

    /**
     * Gets a list of positions to represent the pattern
     */
    public ArrayList<Double> getCirclePositions() {
        ArrayList<Double> list = new ArrayList<Double>();

        for (int i=0 ; i<pattern.length ; i++) {
            int start = FFTHelper.firstElementLargerThan(pattern, 0.90, i);
            if (start == -1) {
                break;
            }
            int end = FFTHelper.firstElementSmallerThan(pattern, 0.90, start);
            if (end == -1) {
                break;
            }
            double mid = (start + end)/2.0;
            list.add(mid);
            i = end;
        }

        return list;
    }

}