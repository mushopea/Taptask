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

import java.util.Arrays;

import sg.edu.nus.taptask.model.AccelerometerConfig;

public class AccelerometerSampler implements SensorEventListener {


    protected Context context;
    protected SensorManager sensorManager;
    protected Sensor accelerometerSensor;
    protected AccelerometerSamplerListener accelerometerSamplerListener;
    protected volatile int timeIndex = 0;
    protected volatile double[] absAccelerationBuffer;

    protected AccelerometerConfig accelerometerConfig = null;

    protected volatile boolean isSampling = false;
    protected double samplingFrequency = 0;
    protected double samplingPeriod = 0;
    protected double samplingDuration = 0;

    public AccelerometerSampler(Context context) {
        // Initialize
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.context = context;
        this.accelerometerConfig = AccelerometerConfig.getInstance(context);
    }

    // TODO: Call this in a separate thread as it may take some time, depending on sampleSize
    /**
     * Calibrate and find out the sampling rates for each sensorDelay parameter.
     * Need to calibrate as different devices support different sampling rates.
     */
    public void calibrateSamplingRate() {
        accelerometerConfig.readAccelerometerConfig();
        if (accelerometerConfig.getSamplingFrequencyToUse() != 0) {
            // Already calibrated
            accelerometerSamplerListener.onCalibrationDone();
            return;
        }

        Log.d("accSampler", "calibrateSamplingRate: Calibrating...");
        for (int i=0 ; i<accelerometerConfig.sensorDelayList.length ; i++) {
            int sensorDelay = accelerometerConfig.sensorDelayList[i];
            double frequency = calibrateSamplingRate(sensorDelay, 101 / (sensorDelay+1)); // TODO: adjust sampleSize accordingly
            accelerometerConfig.sensorDelaySamplingRate[i] = frequency;
            Log.d("accSampler", "calibrateSamplingRate: " + sensorDelay + ", frequency: " + frequency + " hertz.");
        }

        accelerometerConfig.calculateFrequencyToUse();
        // Store results after calibration
        accelerometerConfig.saveAccelerometerConfig();
        
        accelerometerSamplerListener.onCalibrationDone();
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
        if (this.isSampling) {
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
        while (this.isSampling) {
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
        Log.d("accSampler", "startSampling: Start sampling using " + accelerometerConfig.getSensorDelayToUse() + ", " + accelerometerConfig.getSamplingFrequencyToUse() + " Hz.");
        startSampling(accelerometerConfig.getSensorDelayToUse(), accelerometerConfig.getSamplingFrequencyToUse(), bufferSizeInSeconds);
    }

    public void startSampling(int sensorDelay, double samplingFrequency, double bufferSizeInSeconds) {
        if (!isSampling) {
            isSampling = true;
            this.timeIndex = 0;
            this.samplingFrequency = samplingFrequency;
            this.samplingPeriod = 1.0 / samplingFrequency;
            this.samplingDuration = bufferSizeInSeconds;

            // Set up buffer
            double period = 1.0 / samplingFrequency;
            int bufferSize = (int) Math.ceil(bufferSizeInSeconds / period);
            absAccelerationBuffer = new double[bufferSize];
            Arrays.fill(absAccelerationBuffer, 0);

            // Use a separate thread to receive sensor readings asynchronously
            HandlerThread handlerThread = new HandlerThread("sensorThread");
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper());

            // Register listener
            Log.d("accSampler", "startSampling: Start sampling");
            sensorManager.registerListener(this, accelerometerSensor, sensorDelay, handler);

            // Send event
            if (accelerometerSamplerListener != null) {
                accelerometerSamplerListener.onSamplingStart();
            }
        } else {
            Log.w("accSampler", "startSampling: Already sampling");
        }
    }

    public void stopSampling() {
        if (this.isSampling) {
            Log.d("accSampler", "stopSampling: Stop sampling");
            this.sensorManager.unregisterListener(this);
            this.isSampling = false;

            // Send event
            if (accelerometerSamplerListener != null) {
                accelerometerSamplerListener.onSamplingStop();
            }
        } else {
            Log.w("accSampler", "stopSampling: Not sampling");
        }
    }

    private double[] absAccelerationBufferCopy = null;
    public double[] getAbsAccelerationBufferSafe() {
        if (absAccelerationBufferCopy == null || absAccelerationBufferCopy.length != absAccelerationBuffer.length) {
            absAccelerationBufferCopy = new double[absAccelerationBuffer.length];
        }
        synchronized (this) {
            // Rotate and return copy of buffer
            System.arraycopy(absAccelerationBuffer, timeIndex, absAccelerationBufferCopy, 0, absAccelerationBuffer.length - timeIndex);
            System.arraycopy(absAccelerationBuffer, 0, absAccelerationBufferCopy, absAccelerationBuffer.length - timeIndex, timeIndex);
        }
        return absAccelerationBufferCopy;
    }

    public double[] getAbsAccelerationBuffer() {
        if (absAccelerationBufferCopy == null || absAccelerationBufferCopy.length != absAccelerationBuffer.length) {
            absAccelerationBufferCopy = new double[absAccelerationBuffer.length];
        }
        // Rotate and return copy of buffer
        System.arraycopy(absAccelerationBuffer, timeIndex, absAccelerationBufferCopy, 0, absAccelerationBuffer.length - timeIndex);
        System.arraycopy(absAccelerationBuffer, 0, absAccelerationBufferCopy, absAccelerationBuffer.length - timeIndex, timeIndex);
        return absAccelerationBufferCopy;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (isSampling) {
                if (isCalibrating) {
                    calibrationStep(sensorEvent);
                } else {
                    samplingStep(sensorEvent);
                }
            }
        }
    }

    protected void calibrationStep(SensorEvent sensorEvent) {
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
    }

    protected void samplingStep(SensorEvent sensorEvent) {
        // Normal sampling
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        double absAcceleration = Math.sqrt(x * x + y * y + z * z);
        synchronized (this) {
            absAccelerationBuffer[timeIndex] = absAcceleration - accelerometerConfig.gravityOffset;
            timeIndex += 1;
            timeIndex %= absAccelerationBuffer.length;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        }
    }

    public void setAccelerometerSamplerListener(AccelerometerSamplerListener accelerometerSamplerListener) {
        this.accelerometerSamplerListener = accelerometerSamplerListener;
    }
}
