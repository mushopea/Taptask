package sg.edu.nus.taptask;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapActionManager;
import sg.edu.nus.taptask.model.TapPattern;

public class RecordTestFragment extends Fragment implements AccelerometerSamplerListener {
    // SurfaceView
    private TestVisualizerSurfaceView testVisualizerSurfaceView = null;

    // Audio
    private SoundSampler soundSampler = null;
    private short[] buffer = null;

    // Accelerometer
    AccelerometerSampler accelerometerSampler = null;

    public RecordTestFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        testVisualizerSurfaceView = (TestVisualizerSurfaceView) view.findViewById(R.id.surfaceView);

        // Set audio recording to start
        soundSampler = new SoundSampler();
        buffer = new short[soundSampler.getBufferSize()];
        soundSampler.setBuffer(buffer);
        soundSampler.startRecording();

        // Set visualizer buffer
        testVisualizerSurfaceView.setAudioBuffer(buffer);


        // Start accelerometer
        accelerometerSampler = new AccelerometerRecorder(this.getActivity());
        accelerometerSampler.setAccelerometerSamplerListener(this);
        accelerometerSampler.calibrateSamplingRate();
        accelerometerSampler.startSampling(5);

        // Set visualizer for absAcceleration
        testVisualizerSurfaceView.setAccelerationSampler(accelerometerSampler);

        return view;
    }

    @Override
    public void onDestroy() {
        soundSampler.stopRecording();
        accelerometerSampler.stopSampling();
        super.onDestroy();
    }

    @Override
    public void onSamplingStart() {
        Log.e("RecordFragment", "onSamplingStart() called");
    }

    @Override
    public void onSamplingStop() {
        Log.e("RecordFragment", "onSamplingStop() called");
    }

    @Override
    public void onRecordingDelayOver() {
        Log.e("RecordFragment", "onRecordingDelayOver() called");
    }

    @Override
    public void onRecordingDone() {
        Log.e("RecordFragment", "onRecordingDone() called");
        TapPattern pattern = TapPattern.createPattern(accelerometerSampler.getAbsAccelerationBuffer(),
                accelerometerSampler.samplingDuration,
                accelerometerSampler.samplingFrequency);
        TapAction tapAction = new TapAction(pattern);
        TapActionManager tapActionManager = TapActionManager.getInstance(this.getActivity().getBaseContext());
        tapActionManager.removeAllTasks();
        tapActionManager.addTapAction(tapAction);

        // Run matcher to match pattern with itself
        pattern.matchPatternPercentage(pattern);


        // Run AccelerometerMatcher to match pattern
        this.accelerometerSampler = new AccelerometerMatcher(this.getActivity());
        this.accelerometerSampler.setAccelerometerSamplerListener(this);
        ((AccelerometerMatcher)this.accelerometerSampler).setTapActionToMatch(tapAction);
        this.accelerometerSampler.startSampling(10); // 10 sec buffer

        // Reset visualizer
        testVisualizerSurfaceView.setAccelerationSampler(accelerometerSampler);

    }

    @Override
    public void onMatchFound(TapAction tapAction, double[] signal, double matchPct) {
        Log.e("RecordFragment", "Match found!!");
        accelerometerSampler.stopSampling();
    }
}
