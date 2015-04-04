package sg.edu.nus.taptask;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecordFragment extends Fragment implements SensorEventListener {
    // SurfaceView
    private AudioBufferVisualizerSurfaceView audioBufferVisualizerSurfaceView = null;

    // Audio
    private SoundSampler soundSampler = null;
    private short[] buffer = null;

    // Accelerometer
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private volatile float[] accelerationBuffer = {0.0f, 0.0f, 0.0f};


    public RecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        audioBufferVisualizerSurfaceView = (AudioBufferVisualizerSurfaceView) view.findViewById(R.id.surfaceView);

        // Set audio recording to start
        soundSampler = new SoundSampler();
        buffer = new short[soundSampler.getBufferSize()];
        soundSampler.setBuffer(buffer);
        soundSampler.startRecording();

        // Set visualizer buffer
        audioBufferVisualizerSurfaceView.setAudioBuffer(buffer);


        // Start accelerometer
        senSensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);

        // Set visualizer for absAcceleration
        audioBufferVisualizerSurfaceView.setAccelerationValuesBuffer(accelerationBuffer);

        return view;
    }

    @Override
    public void onDestroy() {
        soundSampler.stopRecording();
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            accelerationBuffer[0] = x;
            accelerationBuffer[1] = y;
            accelerationBuffer[2] = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
