package sg.edu.nus.taptask;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapActionManager;
import sg.edu.nus.taptask.model.TapPattern;

public class RecordFragment extends Fragment implements AccelerometerSamplerListener {
    // Views
    private AccelerometerRecordSurfaceView accelerometerRecordSurfaceView = null;
    private TextView instructionsText = null;
    private Button startButton = null;
    private Button confirmButton = null;
    private Button resetButton = null;
    private Button addButton = null;


    // Accelerometer
    AccelerometerSampler accelerometerSampler = null;

    TapPattern firstPattern = null;
    TapPattern secondPattern = null;

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
        startButton = (Button) view.findViewById(R.id.startButton);
        confirmButton = (Button) view.findViewById(R.id.confirmButton);
        resetButton = (Button) view.findViewById(R.id.resetButton);
        addButton = (Button) view.findViewById(R.id.addButton);

        // Add listeners
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStartButton(v);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickConfirmButton(v);
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickResetButton(v);
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddButton(v);
            }
        });


        setViewState(0);

        return view;
    }

    public void onClickStartButton(View view) {
        if (instructionsText.getText().equals("Press start to record pattern.")) {
            startRecording();
        } else if (instructionsText.getText().equals("Press start to confirm pattern.")) {
            startSecondRecording();
        }
    }

    public void onClickConfirmButton(View view) {
        setViewState(3);
    }

    public void onClickResetButton(View view) {
        firstPattern = null;
        accelerometerRecordSurfaceView.setFirstPattern(null);
        setViewState(0);
    }

    public void onClickAddButton(View view) {
        // Save stuff
        TapActionManager tapActionManager = TapActionManager.getInstance(this.getActivity().getBaseContext());

        TapAction tapAction = tapActionManager.getCurrentTapAction();
        if (tapAction == null) {
            Log.e("onClickAddButton", "Null tapAction");
            tapAction = new TapAction(firstPattern);
        }
        tapAction.setPattern(firstPattern);

        //tapActionManager.removeAllTasks();
        tapActionManager.addTapAction(tapAction);

        // Return to main activity
        ((RecordActivity)this.getActivity()).returnToMainActivity();
    }

    private void setViewState(int state) {
        switch(state) {
            case 0:
                // Waiting for first recording to start
                instructionsText.setText("Press start to record pattern.");
                startButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.GONE);
                confirmButton.setVisibility(View.GONE);
                addButton.setVisibility(View.GONE);
                break;
            case 1:
                // Recording
                instructionsText.setText("Hold on...");
                startButton.setVisibility(View.INVISIBLE);
                resetButton.setVisibility(View.GONE);
                confirmButton.setVisibility(View.GONE);
                addButton.setVisibility(View.GONE);
                break;
            case 2:
                // Done recording first
                instructionsText.setText("Pattern recorded!");
                confirmButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.VISIBLE);
                startButton.setVisibility(View.INVISIBLE);
                addButton.setVisibility(View.GONE);
                break;
            case 3:
                // Waiting for second recording to start
                instructionsText.setText("Press start to confirm pattern.");
                startButton.setVisibility(View.VISIBLE);
                confirmButton.setVisibility(View.GONE);
                resetButton.setVisibility(View.GONE);
                addButton.setVisibility(View.GONE);
                break;
            case 4:
                // Done recording second, success
                instructionsText.setText("All Done!");
                addButton.setVisibility(View.VISIBLE);
                startButton.setVisibility(View.GONE);
                confirmButton.setVisibility(View.GONE);
                resetButton.setVisibility(View.GONE);
                break;
            default:
        }

    }

    private void startRecording() {
        // Start accelerometer
        accelerometerSampler = new AccelerometerRecorder(this.getActivity());
        accelerometerSampler.setAccelerometerSamplerListener(this);
        accelerometerSampler.calibrateSamplingRate();
        accelerometerSampler.startSampling(5);

        // Set visualizer for absAcceleration
        accelerometerRecordSurfaceView.setAccelerationSampler(accelerometerSampler);

        // Hide start button
        setViewState(1);
    }


    private void startSecondRecording() {
        this.accelerometerSampler = new AccelerometerRecorder(this.getActivity());
        this.accelerometerSampler.setAccelerometerSamplerListener(this);
        this.accelerometerSampler.startSampling(5); // 5 sec buffer

        // Reset visualizer
        accelerometerRecordSurfaceView.setAccelerationSampler(accelerometerSampler);

        setViewState(1);
    }

    @Override
    public void onDestroy() {
        if (accelerometerSampler != null) {
            accelerometerSampler.stopSampling();
        }
        super.onDestroy();
    }

    @Override
    public void onSamplingStart() {
        Log.i("RecordFragment", "onSamplingStart() called");
    }

    @Override
    public void onSamplingStop() {
        Log.i("RecordFragment", "onSamplingStop() called");
    }

    @Override
    public void onCalibrationDone() {}

    @Override
    public void onRecordingDelayOver() {
        Log.i("RecordFragment", "onRecordingDelayOver() called");

        // Set instructions text
        this.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                instructionsText.setText("Start Tapping!");
            }
        });
    }

    @Override
    public void onRecordingDone() {
        Log.i("RecordFragment", "onRecordingDone() called");
        TapPattern pattern = TapPattern.createPattern(accelerometerSampler.getAbsAccelerationBuffer(),
                accelerometerSampler.samplingDuration,
                accelerometerSampler.samplingFrequency);

        if (firstPattern == null) {
            // Enforce minimum of 2 taps
            if (pattern.tapPositions.size() < 2) {
                Log.e("RecordFragment", "Lesser taps than minimum requirement. " + pattern.tapPositions.size());
                this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity().getBaseContext(),
                                "Minimum of 2 taps required!", Toast.LENGTH_SHORT).show();
                    }
                });
                accelerometerSampler.clearBuffer();
            } else {
                firstPattern = pattern;
                accelerometerRecordSurfaceView.setFirstPattern(firstPattern);
                accelerometerSampler.clearBuffer();
            }
        } else {
            secondPattern = pattern;
            accelerometerRecordSurfaceView.setSecondPattern(secondPattern);
            accelerometerSampler.clearBuffer();
        }

        // Run recorder again
        if (firstPattern == null) {
            this.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    setViewState(0);
                }
            });
        } else if (secondPattern == null) {
            this.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    setViewState(2);
                }
            });
        } else {
            // Find match percentage
            final double matchPct = firstPattern.matchPatternPercentage(secondPattern) * 100;
            // Set instructions text
            this.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    setViewState(4);
                    instructionsText.setText("Pattern match: " + (int)matchPct + "%");
                }
            });
        }

    }

    @Override
    public void onMatchFound(TapAction tapAction, TapPattern signalPattern, double matchPct) {
    }
}
