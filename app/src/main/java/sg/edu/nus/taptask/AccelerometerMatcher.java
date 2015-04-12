package sg.edu.nus.taptask;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import sg.edu.nus.taptask.model.AccelerometerConfig;
import sg.edu.nus.taptask.model.TapPattern;

/**
 * Keeps sampling and looking for pattern matches in the signal
 */
public class AccelerometerMatcher extends AccelerometerSampler {

    public TapPattern tapPatternToMatch;

    public AccelerometerMatcher(Context context) {
        super(context);
    }

    @Override
    protected void samplingStep(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        double absAcceleration = Math.sqrt(x * x + y * y + z * z);
        synchronized (this) {
            if (!isSampling) {
                return;
            }
            absAccelerationBuffer[timeIndex] = absAcceleration - accelerometerConfig.gravityOffset;
            timeIndex += 1;
            timeIndex %= absAccelerationBuffer.length;

            // Attempt to locate match tap every 5/10 sec (0.5 sec) (samplingDuration/10)
            if (timeIndex % (absAccelerationBuffer.length / 10) == 0) {
                if (this.tapPatternToMatch != null) {
                    TapPattern signalPattern = TapPattern.createPattern(getAbsAccelerationBuffer(), this.samplingDuration, this.samplingFrequency);
                    matchPattern(this.tapPatternToMatch, signalPattern.pattern);
                } else {
                    Log.e("AccMatcher", "TapPatternToMatch null");
                }
            }

        }
    }

    protected void matchPattern(TapPattern tapPattern, double[] signal) {
        double matchPct = tapPattern.matchSignalPercentage(signal);
        if (matchPct > TapPattern.MATCH_PERCENTAGE_THRESHOLD) {
            if (this.accelerometerSamplerListener != null) {
                accelerometerSamplerListener.onMatchFound(tapPattern, signal, matchPct);
            }
        }
    }

    public TapPattern getTapPatternToMatch() {
        return tapPatternToMatch;
    }

    public void setTapPatternToMatch(TapPattern tapPatternToMatch) {
        this.tapPatternToMatch = tapPatternToMatch;
    }
}
