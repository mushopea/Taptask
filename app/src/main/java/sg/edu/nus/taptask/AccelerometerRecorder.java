package sg.edu.nus.taptask;

import android.app.Activity;
import android.hardware.SensorEvent;
import android.util.Log;

import sg.edu.nus.taptask.model.TapPattern;

/**
 * Samples and records a tap pattern for a specified duration.
 */
public class AccelerometerRecorder extends AccelerometerSampler {

    protected boolean waitForFirstTap = false;                    // Wait for first tap before beginning to record
    protected double delayBeforeStart = 0;                        // Delay before recording
    protected volatile double remainingDelayBeforeStart = 0;      // Remaining delay before recording
    protected volatile int timeIndexBeforeStop = -1;
    protected volatile int filledTimeIndex = 0;

    public AccelerometerRecorder(Activity activity) {
        super(activity);
        this.waitForFirstTap = true;
        this.delayBeforeStart = 0.5;
    }

    public AccelerometerRecorder(Activity activity, boolean waitForFirstTap, double delayBeforeStart) {
        super(activity);
        this.waitForFirstTap = waitForFirstTap;
        this.delayBeforeStart = delayBeforeStart;
    }

    // Returns time left before recording ends in seconds
    public double timeRemainingBeforeStop() {
        return timeIndexRemainingBeforeStop() * samplingPeriod;
    }

    public double timeIndexRemainingBeforeStop() {
        if (waitForFirstTap) {
            if (timeIndexBeforeStop == -1) {
                return absAccelerationBuffer.length + (remainingDelayBeforeStart / samplingPeriod);
            } else {
                return timeIndexBeforeStop;
            }
        } else {
            return absAccelerationBuffer.length - filledTimeIndex;
        }
    }

    @Override
    public void startSampling(int sensorDelay, double samplingFrequency, double bufferSizeInSeconds) {
        this.remainingDelayBeforeStart = this.delayBeforeStart;
        this.filledTimeIndex = 0;
        this.timeIndexBeforeStop = -1;
        super.startSampling(sensorDelay, samplingFrequency, bufferSizeInSeconds);
    }

    @Override
    protected void samplingStep(SensorEvent sensorEvent) {
        // Delay start
        if (this.remainingDelayBeforeStart > 0) {
            this.remainingDelayBeforeStart -= this.samplingPeriod;
            if (this.remainingDelayBeforeStart <= 0) {
                this.remainingDelayBeforeStart = 0;
                if (accelerometerSamplerListener != null) {
                    accelerometerSamplerListener.onRecordingDelayOver();
                }
            }
            return;
        }
        // Perform normal sampling for a set duration
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

            // Keep track of how much of the buffer is filled
            if (filledTimeIndex < absAccelerationBuffer.length) {
                filledTimeIndex ++;
            }
            // Stop after recording for the specified duration
            if (!waitForFirstTap && filledTimeIndex >= absAccelerationBuffer.length) {
                stopSampling();
                if (accelerometerSamplerListener != null) {
                    accelerometerSamplerListener.onRecordingDone();
                }
                return;
            }
            // Attempt to locate first tap every 5/10 sec (0.5 sec)
            if (waitForFirstTap && timeIndexBeforeStop == -1 &&
                    timeIndex % (absAccelerationBuffer.length / 10) == 0) {
                TapPattern pattern = TapPattern.createPattern(this.getAbsAccelerationBuffer(), this.samplingDuration, this.samplingFrequency);
                timeIndexBeforeStop = FFTHelper.firstElementGreaterThan(pattern.pattern, 0.1, absAccelerationBuffer.length / 10);
                if (timeIndexBeforeStop != -1) {
                    Log.d("AccRecorder", "First tap found");
                }
            }
            if (waitForFirstTap && timeIndexBeforeStop > 0) {
                timeIndexBeforeStop --;
            }
            // Stop after recording for duration after first tap
            if (waitForFirstTap && this.timeIndexBeforeStop == 0) {
                stopSampling();
                if (accelerometerSamplerListener != null) {
                    accelerometerSamplerListener.onRecordingDone();
                }
                return;
            }
        }
    }

}
