package sg.edu.nus.taptask;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapActionManager;
import sg.edu.nus.taptask.model.TapPattern;

public class RecordFragment extends Fragment implements AccelerometerSamplerListener {
    // Views
    private AccelerometerRecordSurfaceView accelerometerRecordSurfaceView = null;
    private TextView instructionsText = null;

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
        accelerometerRecordSurfaceView = (AccelerometerRecordSurfaceView) view.findViewById(R.id.surfaceView);
        instructionsText = (TextView) view.findViewById(R.id.instructionsText);

        // Start accelerometer
        accelerometerSampler = new AccelerometerRecorder(this.getActivity());
        accelerometerSampler.setAccelerometerSamplerListener(this);
        accelerometerSampler.calibrateSamplingRate();
        accelerometerSampler.startSampling(5);

        // Set visualizer for absAcceleration
        accelerometerRecordSurfaceView.setAccelerationSampler(accelerometerSampler);

        // Set instructions text
        instructionsText.setText("Hold on...");

        return view;
    }

    @Override
    public void onDestroy() {
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

        // Set instructions text
        this.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                instructionsText.setText("Start Tapping!");
            }
        });
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
        ((AccelerometerMatcher)this.accelerometerSampler).setTapPatternToMatch(pattern);
        this.accelerometerSampler.startSampling(5); // 5 sec buffer

        // Reset visualizer
        accelerometerRecordSurfaceView.setAccelerationSampler(accelerometerSampler);

        // Set instructions text
        this.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                instructionsText.setText("Pattern Recorded!");
            }
        });

    }

    @Override
    public void onMatchFound(TapPattern pattern, double[] signal, double matchPct) {
        Log.e("RecordFragment", "Match found!!");
        accelerometerSampler.stopSampling();

        // Set instructions text
        this.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                instructionsText.setText("Pattern Match!");
            }
        });
    }
}
