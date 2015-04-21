package sg.edu.nus.taptask.model;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import sg.edu.nus.taptask.FFTHelper;

/**
 * Class representing a tap pattern
 */
public class TapPattern {
    public static final double MATCH_PERCENTAGE_THRESHOLD = 0.85;       // TODO: test what values are good or let user decide.
    public static final double MATCH_RECORD_CONFIRM_THRESHOLD = 0.7;    // Threshold for confirmation pattern

    public double duration;
    public double frequency;
    public double[] pattern;
    public ArrayList<Double> tapPositions;
    public ArrayList<Double> tapIntervals;

    public TapPattern() {
    }

    public TapPattern(TapPattern tapPattern) {
        this.duration = tapPattern.duration;
        this.frequency = tapPattern.frequency;
        this.pattern = Arrays.copyOf(tapPattern.pattern, tapPattern.pattern.length);
        this.tapPositions = new ArrayList<>(tapPattern.tapPositions);
        this.tapIntervals = new ArrayList<>(tapPattern.tapIntervals);
    }


    private transient static double[] boxKernel = null;
    private transient static double[] triangleKernel = null;
    private transient static double[] absAccelerationBufferOptimal = null;
    public static TapPattern createPattern(double[] absAccelerationBuffer, double duration, double frequency) {
        return createPattern(absAccelerationBuffer, duration, frequency, null);
    }
    public static TapPattern createPattern(double[] absAccelerationBuffer, double duration, double frequency, TapPattern pattern) {
        int optimalLength = FFTHelper.nextPowerOf2(absAccelerationBuffer.length * 2);
        if (absAccelerationBufferOptimal == null || absAccelerationBufferOptimal.length != optimalLength) {
            absAccelerationBufferOptimal = new double[optimalLength];
            Arrays.fill(absAccelerationBufferOptimal, 0);
        }

        System.arraycopy(absAccelerationBuffer, 0, absAccelerationBufferOptimal, 0, absAccelerationBuffer.length);
        Arrays.fill(absAccelerationBufferOptimal, absAccelerationBuffer.length, absAccelerationBufferOptimal.length, 0);

        // Calculate jounce
        double[] jounce = getJounce(absAccelerationBufferOptimal);
        // Get absolute values
        FFTHelper.abs(jounce);
        // Binary threshold
        if (frequency < AccelerometerConfig.getInstance(null).minSamplingFrequency){
            FFTHelper.binaryThreshold(jounce, 5);
        } else {
            // Apply convolution to blur with box kernel
//            if (boxKernel == null || boxKernel.length != jounce.length) {
//                boxKernel = FFTHelper.boxKernel(jounce.length, 3);
//            }
//            jounce = FFTHelper.FFTConvolution(jounce, boxKernel);
            FFTHelper.binaryThreshold(jounce, 5); // TODO: tweak threshold
        }
        // Triangle filter
        if (triangleKernel == null || triangleKernel.length != jounce.length) {
            triangleKernel = FFTHelper.triangleKernel(jounce.length, 10); // TODO: tweak kernel size
        }
        jounce = FFTHelper.FFTConvolution(jounce, triangleKernel);
        // Clamp max value
        FFTHelper.clampMaxValue(jounce, 1);

        // Trim length, re-use absAccelerationBuffer
        jounce = FFTHelper.trim(jounce, absAccelerationBuffer.length, absAccelerationBuffer);


        if (pattern == null) {
            pattern = new TapPattern();
            Log.d("new", "TapPattern");
        }
        pattern.duration = duration;
        pattern.frequency = frequency;
        pattern.pattern = jounce;
        pattern.tapPositions = pattern.calculateTapPositions();
        pattern.tapIntervals = pattern.calculateTapIntervals();

        return pattern;
    }

    // Calculates jounce from acceleration
    private transient static double[] sobelKernel = null;
    public static double[] getJounce(double[] acceleration) {
        if (sobelKernel == null || sobelKernel.length != acceleration.length) {
            sobelKernel = FFTHelper.sobelKernel(acceleration.length, 1); // TODO: tweak kernel size
        }
        double[] jounce = FFTHelper.FFTConvolution(acceleration, sobelKernel, 2); // Jounce (m/s^4)
        return jounce;
    }

    public boolean matchPattern(TapPattern pattern) {
        return matchPatternPercentage(pattern) >= MATCH_PERCENTAGE_THRESHOLD;
    }

    public double matchPatternPercentage(TapPattern tapPattern) {
        if (this.tapIntervals.size() != tapPattern.tapIntervals.size()) {
            Log.e("matchPatternPercentage", "Num of taps different.");
            return 0;
        }

        double magnitude0 = magnitude(this.tapIntervals);
        double magnitude1 = magnitude(tapPattern.tapIntervals);
        double dotProduct = dot(this.tapIntervals, tapPattern.tapIntervals);
        double angle = Math.acos(dotProduct/(magnitude0*magnitude1));

        // Factor in magnitude difference
        double magnitudeMatchPct =  1 - Math.abs(magnitude0 - magnitude1) / (magnitude0 + magnitude1);
        double matchPct = (Math.PI - angle)/Math.PI * magnitudeMatchPct;

        Log.d("matchPatternPercentage", "pct: " + matchPct);

        return matchPct;
    }

    public double matchPatternSimilarityPercentage(TapPattern tapPattern) {
        if (this.tapIntervals.size() > tapPattern.tapIntervals.size()) {
            return tapPattern.matchSignalPercentage(this);
        } else {
            return this.matchSignalPercentage(tapPattern);
        }
    }

    public double matchSignalPercentage(TapPattern signalTapPattern, int len) {
        // Get len taps
        ArrayList<Double> tapIntervals0 = new ArrayList<Double>(this.tapIntervals);
        ArrayList<Double> tapIntervals1 = new ArrayList<Double>(signalTapPattern.tapIntervals);
        while (tapIntervals0.size() > len) {
            tapIntervals0.remove(tapIntervals0.size()-1);
        }
        while (tapIntervals1.size() > len) {
            tapIntervals1.remove(0);
        }
        if (tapIntervals0.size() != tapIntervals1.size()) {
            return 0;
        }

        double magnitude0 = magnitude(tapIntervals0);
        double magnitude1 = magnitude(tapIntervals1);
        double dotProduct = dot(tapIntervals0, tapIntervals1);
        double angle = Math.acos(dotProduct/(magnitude0*magnitude1));

        // Factor in magnitude difference
        double magnitudeMatchPct =  1 - Math.abs(magnitude0 - magnitude1) / (magnitude0 + magnitude1);
        double matchPct = (Math.PI - angle)/Math.PI * magnitudeMatchPct;

        Log.d("matchPFrontPercentage", "pct: " + matchPct);

        return matchPct;
    }

    public double matchSignalPercentage(TapPattern signalTapPattern) {
        // Get last taps in signal
        ArrayList<Double> tapIntervals = new ArrayList<Double>(signalTapPattern.tapIntervals);
        while (tapIntervals.size() > this.tapIntervals.size()) {
            tapIntervals.remove(0);
        }
        if (this.tapIntervals.size() != tapIntervals.size()) {
            return 0;
        }

        double magnitude0 = magnitude(this.tapIntervals);
        double magnitude1 = magnitude(tapIntervals);
        double dotProduct = dot(this.tapIntervals, tapIntervals);
        double angle = Math.acos(dotProduct/(magnitude0*magnitude1));

        // Factor in magnitude difference
        double magnitudeMatchPct =  1 - Math.abs(magnitude0 - magnitude1) / (magnitude0 + magnitude1);
        double matchPct = (Math.PI - angle)/Math.PI * magnitudeMatchPct;

        Log.d("matchSignalPercentage", "pct: " + matchPct);

        return matchPct;
    }

    /**
     * Gets a list of positions to represent the pattern
     */
    public ArrayList<Double> calculateTapPositions() {
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

    /**
     * Gets a list of interval durations between tap positions
     * Requires tapPositions to be calculated before hand
     */
    public ArrayList<Double> calculateTapIntervals() {
        ArrayList<Double> list = new ArrayList<Double>();

        for (int i=0 ; i<tapPositions.size()-1 ; i++) {
            list.add(tapPositions.get(i+1) - tapPositions.get(i));
        }

        return list;
    }

    public long[] getVibrationPattern() {
        final long tap = 75;
        ArrayList<Double> tapPositions = this.tapPositions;
        long [] vibrationPattern = new long[tapPositions.size()*2];
        double delta = (duration / pattern.length) * 1000;
        for (int i = 0 ; i<tapPositions.size() ; i++) {
            if (i == 0) {
                vibrationPattern[i * 2] = 0;
            } else if (i<tapPositions.size()) {
                vibrationPattern[i * 2] = (long) ((tapPositions.get(i) - tapPositions.get(i-1)) * delta);
            }
            vibrationPattern[i*2+1] = tap;
        }
        return vibrationPattern;
    }

    public void vibratePattern(Context context) {
        long[] vibrationPattern = getVibrationPattern();
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(vibrationPattern, -1);
    }


    // Helper functions
    public static double magnitude(ArrayList<Double> vector) {
        double magnitude = 0;
        for (Double d : vector) {
            magnitude += d*d;
        }
        return Math.sqrt(magnitude);
    }

    public static double dot(ArrayList<Double> vector0, ArrayList<Double> vector1) {
        if (vector0.size() != vector1.size()) {
            Log.e("dot", "size mismatch");
            return 0;
        }
        double dotProduct = 0;
        for (int i=0 ; i<vector0.size() ; i++) {
            dotProduct += vector0.get(i) * vector1.get(i);
        }
        return dotProduct;
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}