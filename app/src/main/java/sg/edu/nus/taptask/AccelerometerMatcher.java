package sg.edu.nus.taptask;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapPattern;

/**
 * Keeps sampling and looking for pattern matches in the signal
 */
public class AccelerometerMatcher extends AccelerometerSampler {

    public ArrayList<TapAction> tapActionsToMatch;

    public AccelerometerMatcher(Context context) {
        super(context);
    }


    public void clearBuffer() {
        synchronized (this) {
            Arrays.fill(absAccelerationBuffer, 0);
        }
    }

    private TapPattern signalPattern = null;
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
                if (this.tapActionsToMatch != null && this.tapActionsToMatch.size() > 0) {
                    signalPattern = TapPattern.createPattern(getAbsAccelerationBuffer(), this.samplingDuration, this.samplingFrequency, signalPattern);
                    matchPattern(signalPattern);
                } else {
                    Log.e("AccMatcher", "TapPatternToMatch null");
                }
            }

        }
    }

    protected void matchPattern(TapPattern signalPattern) {
        double bestMatchPct = -1;
        TapAction bestMatchTapAction = null;
        for (int i=0 ; i<tapActionsToMatch.size() ; i++) {
            TapAction tapActionToMatch = tapActionsToMatch.get(i);
            TapPattern tapPattern = tapActionToMatch.getPattern();
            double matchPct = tapPattern.matchSignalPercentage(signalPattern);
            if (matchPct > bestMatchPct) {
                bestMatchPct = matchPct;
                bestMatchTapAction = tapActionToMatch;
            }
        }

        if (bestMatchPct > TapPattern.MATCH_PERCENTAGE_THRESHOLD) {
            if (this.accelerometerSamplerListener != null) {
                accelerometerSamplerListener.onMatchFound(bestMatchTapAction, signalPattern, bestMatchPct);
            }
        }

    }

    public ArrayList<TapAction> getTapActionsToMatch() {
        return tapActionsToMatch;
    }

    public void setTapActionsToMatch(ArrayList<TapAction> tapActionsToMatch) {
        this.tapActionsToMatch = tapActionsToMatch;
    }
}
