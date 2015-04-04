package sg.edu.nus.taptask;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecordFragment extends Fragment {
    // SurfaceView
    private AudioBufferVisualizerSurfaceView audioBufferVisualizerSurfaceView = null;

    // Audio
    private SoundSampler soundSampler = null;
    private short[] buffer = null;

    // Accelerometer
    AccelerometerSampler accelerometerSampler = null;

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
        accelerometerSampler = new AccelerometerSampler(this.getActivity());
        accelerometerSampler.calibrateSamplingRate();
        accelerometerSampler.startSampling(5);

        // Set visualizer for absAcceleration
        audioBufferVisualizerSurfaceView.setAccelerationSampler(accelerometerSampler);

        return view;
    }

    @Override
    public void onDestroy() {
        soundSampler.stopRecording();
        super.onDestroy();
    }
}
