package sg.edu.nus.taptask;

import android.app.Activity;
import android.hardware.SensorEvent;

/**
 * Samples and records a tap pattern for a specified duration.
 */
public class AccelerometerRecorder extends AccelerometerSampler{

    protected boolean waitForFirstTap = false;           // Wait for first tap before beginning to record
    protected double delayBeforeStart = 0;               // Delay before recording
    protected double remainingDelayBeforeStart = 0;      // Remaining delay before recording
    protected int filledTimeIndex = 0;

    public AccelerometerRecorder(Activity activity) {
        super(activity);
        this.waitForFirstTap = false;
        this.delayBeforeStart = 0;
    }

    public AccelerometerRecorder(Activity activity, boolean waitForFirstTap, double delayBeforeStart) {
        super(activity);
        this.waitForFirstTap = waitForFirstTap;
        this.delayBeforeStart = delayBeforeStart;
    }

    @Override
    public void startSampling(int sensorDelay, double samplingFrequency, double bufferSizeInSeconds) {
        this.remainingDelayBeforeStart = this.delayBeforeStart;
        this.filledTimeIndex = 0;
        super.startSampling(sensorDelay, samplingFrequency, bufferSizeInSeconds);
    }

    @Override
    protected void samplingStep(SensorEvent sensorEvent) {
        // Delay start
        if (this.remainingDelayBeforeStart > 0) {
            this.remainingDelayBeforeStart -= this.samplingPeriod;
            return;
        }
        // Perform normal sampling for a set duration
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        double absAcceleration = Math.sqrt(x * x + y * y + z * z);
        synchronized (this) {
            absAccelerationBuffer[timeIndex] = absAcceleration - gravityOffset;
            timeIndex += 1;
            timeIndex %= absAccelerationBuffer.length;
        }
        if (filledTimeIndex < absAccelerationBuffer.length) {
            filledTimeIndex += 1;
        }
        // Stop after recording for the specified duration
        if (!waitForFirstTap && filledTimeIndex >= absAccelerationBuffer.length) {
            stopSampling();
        }
        if (waitForFirstTap) {
            // TODO: implement version that waits for first tap

        }
    }

}
