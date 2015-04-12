package sg.edu.nus.taptask.model;

import android.util.Log;

import java.util.ArrayList;

import sg.edu.nus.taptask.FFTHelper;

/**
 * Class representing a tap pattern
 */
public class TapPattern {
    public static final double MATCH_PERCENTAGE_THRESHOLD = 0.6; // TODO: test what values are good or let user decide.

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
        // Triangle filter
        jounce = FFTHelper.FFTConvolution(jounce, FFTHelper.triangleKernel(jounce.length, 15)); // TODO: tweak kernel size
        // Clamp max value
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
        double[] jounce = FFTHelper.FFTConvolution(acceleration, sobelKernel, 2); // Jounce (m/s^4)
        return jounce;
    }

    public boolean matchPattern(TapPattern pattern) {
        return matchPatternPercentage(pattern) >= MATCH_PERCENTAGE_THRESHOLD;
    }

    public double matchPatternPercentage(TapPattern tapPattern) {
        double[] pattern = this.pattern;
        double[] signal = tapPattern.pattern;
        if (this.pattern.length != signal.length) {
            // Should not happen
            Log.i("TapPattern", "matchPatternPercentage: pattern length mismatch");
        }
        // TODO: pad with zeros to possibly make correlation result better?

        double[] correlationResult = FFTHelper.FFTConvolution(
                signal,
                FFTHelper.reverse(pattern));
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
            // TODO: Pad with zeros
            if (this.pattern.length > signal.length) {
                // Something wrong..
                Log.e("TapPattern", "Signal length shorter than pattern length.");
            } else {
                pattern = FFTHelper.padWithZeros(pattern, signal.length);
                Log.d("TapPattern", "Padding pattern with zeros. Lengths:" + signal.length + ", " + pattern.length);
            }
        }

        double[] correlationResult = FFTHelper.FFTConvolution(
                signal,
                FFTHelper.reverse(pattern));
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
    public ArrayList<Integer> getCirclePositions() {
        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int i=0 ; i<pattern.length ; i++) {
            int start = FFTHelper.firstElementLargerThan(pattern, 0.1, i);
            if (start == -1) {
                break;
            }
            int end = FFTHelper.firstElementSmallerThan(pattern, 0.1, start);
            if (end == -1) {
                break;
            }
            int mid = (start + end)/2;
            list.add(mid);
            i = end;
        }

        return list;
    }

}