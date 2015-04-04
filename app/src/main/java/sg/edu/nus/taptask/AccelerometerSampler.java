package sg.edu.nus.taptask;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class AccelerometerSampler implements SensorEventListener {

    public static final int[] sensorDelayList = {SensorManager.SENSOR_DELAY_NORMAL,
                                                 SensorManager.SENSOR_DELAY_UI,
                                                 SensorManager.SENSOR_DELAY_GAME,
                                                 SensorManager.SENSOR_DELAY_FASTEST};
    public static final double[] sensorDelaySamplingRate = new double[sensorDelayList.length];
    public static final double gravityOffset = 10.0; // Rough estimate is enough

    private Activity activity;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private volatile int timeIndex = 0;
    private volatile double[] absAccelerationBuffer;

    private boolean isSampling = false;
    private double samplingFrequency = 0;


    public AccelerometerSampler(Activity activity) {
        // Initialize
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    // TODO: Better to call this in a separate thread as it may take some time, depending on sampleSize
    public void calibrateSamplingRate() {
        // TODO: Read from file if available
        for (int i=0 ; i<sensorDelayList.length ; i++) {
            int sensorDelay = sensorDelayList[i];
            double frequency = calibrateSamplingRate(sensorDelay, 101 / (sensorDelay+1)); // TODO: adjust sampleSize accordingly
            sensorDelaySamplingRate[i] = frequency;
            Log.d("accSampler", "calibrateSamplingRate: " + sensorDelay + ", frequency: " + frequency + " hertz.");
        }
        // TODO: Store results after calibration
    }


    long lastTimeStamp = 0;
    double period = 0;
    int sampleCount = 0;
    int sampleSize = 0;
    boolean isCalibrating = false;
    /**
     * Finds the sampling rate of given sensorDelay
     * @param sensorDelay sensoeDelay to calibrate
     * @param sampleSize number of samples to use
     * @return frequency in hertz (1/sec)
     */
    public double calibrateSamplingRate(int sensorDelay, int sampleSize) {
        if (isSampling) {
            Log.e("accSampler", "calibrateSamplingRate: Cannot calibrate, already sampling.");
            return -1;
        }
        // Set calibration variables
        this.lastTimeStamp = 0;
        this.period = 0;
        this.sampleCount = 0;
        this.sampleSize = sampleSize;
        this.isCalibrating = true;

        // Start sampling
        this.startSampling(sensorDelay, 0, 0);

        // Wait for sampling to complete
        while (isSampling) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Log.w("accSampler", "calibrateSamplingRate: InterruptedException: \n" + e.getMessage());
            }
        }

        // Calculate frequency
        double periodInSec = this.period / 1000000000.0;
        double frequency = (1.0 / periodInSec);

        // Reset variables
        this.lastTimeStamp = 0;
        this.period = 0;
        this.sampleCount = 0;
        this.sampleSize = 0;
        this.isCalibrating = false;

        return frequency;
    }

    // Calibrate first before calling this.
    public void startSampling(double bufferSizeInSeconds) {
        int minSamplingFrequency = 90;
        int minSensorDelay = sensorDelayList[sensorDelayList.length-1];
        double samplingFrequency = 0;
        for (int i=0 ; i<sensorDelayList.length ; i++) {
            if (sensorDelaySamplingRate[i] >= minSamplingFrequency) {
                minSensorDelay = sensorDelayList[i];
                samplingFrequency = sensorDelaySamplingRate[i];
                break;
            }
        }
        if (samplingFrequency < minSamplingFrequency) {
            Log.w("accSampler", "startSampling: Sampling frequency of " + samplingFrequency + " less than minimum of " + minSamplingFrequency + "\n");
        }

        Log.d("accSampler", "startSampling: Start sampling using " + minSensorDelay + ", " + samplingFrequency + " Hz.");
        startSampling(minSensorDelay, samplingFrequency, bufferSizeInSeconds);
    }

    public void startSampling(int sensorDelay, double samplingFrequency, double bufferSizeInSeconds) {
        if (!isSampling) {
            isSampling = true;
            this.timeIndex = 0;
            this.samplingFrequency = samplingFrequency;

            // Set up buffer
            double period = 1.0 / samplingFrequency;
            int bufferSize = (int) Math.ceil(bufferSizeInSeconds / period);
            absAccelerationBuffer = new double[bufferSize];

            // Use a separate thread to receive sensor readings asynchronously
            HandlerThread handlerThread = new HandlerThread("sensorThread");
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper());

            // Register listener
            Log.d("accSampler", "startSampling: Start sampling");
            sensorManager.registerListener(this, accelerometerSensor, sensorDelay, handler);

        } else {
            Log.w("accSampler", "startSampling: Already sampling");
        }
    }

    public void stopSampling() {
        if (this.isSampling) {
            Log.d("accSampler", "stopSampling: Stop sampling");
            this.sensorManager.unregisterListener(this);
            this.timeIndex = 0;
            this.isSampling = false;
            this.samplingFrequency = 0;

        } else {
            Log.w("accSampler", "stopSampling: Not sampling");
        }
    }

    public double[] getAbsAccelerationBuffer() {
        double[] absAccelerationBufferCopy = new double[absAccelerationBuffer.length];
        synchronized (this) {
            // Rotate and return copy of buffer
            System.arraycopy(absAccelerationBuffer, 0, absAccelerationBufferCopy, timeIndex, absAccelerationBuffer.length - timeIndex);
            System.arraycopy(absAccelerationBuffer, timeIndex, absAccelerationBufferCopy, 0, timeIndex);
        }
        return absAccelerationBufferCopy;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (isCalibrating) {
                // Calibration
                long timestamp = sensorEvent.timestamp;
                if (this.lastTimeStamp == 0) {
                    this.lastTimeStamp = timestamp;
                } else {
                    // Calculate period
                    long period = timestamp - this.lastTimeStamp;
                    double avgPeriod = (double)period / (double)(this.sampleSize - 1);
                    this.period += avgPeriod;
                    this.sampleCount += 1;
                    this.lastTimeStamp = timestamp;

                    // Stop sampling
                    if (this.sampleCount >= this.sampleSize) {
                        stopSampling();
                    }
                }

            } else {
                // Normal sampling
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                double absAcceleration = Math.sqrt(x * x + y * y + z * z);
                synchronized (this) {
                    absAccelerationBuffer[timeIndex] = absAcceleration - gravityOffset;
                    timeIndex += 1;
                    timeIndex %= absAccelerationBuffer.length;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        }
    }
}
