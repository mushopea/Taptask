package sg.edu.nus.taptask;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import at.markushi.ui.CircleButton;
import mehdi.sakout.fancybuttons.FancyButton;
import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapActionManager;
import sg.edu.nus.taptask.model.TapPattern;

public class RecordFragment extends Fragment implements AccelerometerSamplerListener {

    TapActionManager tapActionManager;

    // Views
    private AccelerometerRecordSurfaceView accelerometerRecordSurfaceView = null;
    private TextView instructionsText = null;
    private CircleButton startButton = null;
    private FancyButton confirmButton = null;
    private FancyButton resetButton = null;
    private FancyButton addButton = null;


    // Accelerometer
    AccelerometerSampler accelerometerSampler = null;

    TapPattern firstPattern = null;
    TapPattern secondPattern = null;

    public RecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tapActionManager = TapActionManager.getInstance(this.getActivity().getBaseContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        accelerometerRecordSurfaceView = (AccelerometerRecordSurfaceView) view.findViewById(R.id.surfaceView);
        instructionsText = (TextView) view.findViewById(R.id.instructionsText);
        startButton = (CircleButton) view.findViewById(R.id.startButton);
        confirmButton = (FancyButton) view.findViewById(R.id.confirmButton);
        resetButton = (FancyButton) view.findViewById(R.id.resetButton);
        addButton = (FancyButton) view.findViewById(R.id.addButton);

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

    @Override
    public void onPause() {
        accelerometerRecordSurfaceView.drawFlag = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        accelerometerRecordSurfaceView.init();
    }

    public void onClickStartButton(View view) {
        if (instructionsText.getText().equals(getString(R.string.instructions_start))) {
            startRecording();
        } else if (instructionsText.getText().equals(getString(R.string.instructions_confirm))) {
            startSecondRecording();
        }
    }

    public void onClickConfirmButton(View view) {
        setViewState(3);
    }

    public void onClickResetButton(View view) {
        firstPattern = null;
        secondPattern = null;
        accelerometerRecordSurfaceView.setFirstPattern(null);
        accelerometerRecordSurfaceView.setSecondPattern(null);
        accelerometerSampler.clearBuffer();
        setViewState(0);
    }

    public void onClickAddButton(View view) {
        // Save stuff
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
                instructionsText.setText(R.string.instructions_start);
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
                instructionsText.setText("Awesome! Pattern recorded!");
                confirmButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.VISIBLE);
                startButton.setVisibility(View.INVISIBLE);
                addButton.setVisibility(View.GONE);
                break;
            case 3:
                // Waiting for second recording to start
                accelerometerRecordSurfaceView.setSecondPattern(null);
                instructionsText.setText(R.string.instructions_confirm);
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
            // Check similarity with existing patterns and warn.
            double highestMatchPct = 0;
            TapAction closestMatchTapAction = null;
            for (TapAction tapAction : tapActionManager.tapActions) {
                double matchPct = tapAction.getPattern().matchPatternSimilarityPercentage(firstPattern);
                if (matchPct > highestMatchPct) {
                    highestMatchPct = matchPct;
                    closestMatchTapAction = tapAction;
                }
            }

            final int finalMatchPct = (int)(highestMatchPct * 100);
            final TapAction finalTapAction = closestMatchTapAction;
            this.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    setViewState(2);
                    if (finalMatchPct > TapPattern.MATCH_PERCENTAGE_THRESHOLD * 100) {
                        instructionsText.setText("WARNING!\n" + finalMatchPct + "% Similarity with an existing task: " + finalTapAction.getName());
                        Log.i("RecordFragment", "Similar existing pattern: " + finalTapAction.getName());
                    }
                }
            });

        } else {
            // Find match percentage
            final double matchPct = firstPattern.matchPatternPercentage(secondPattern) * 100;

            if (matchPct < TapPattern.MATCH_RECORD_CONFIRM_THRESHOLD) {


                this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        setViewState(2);
                        instructionsText.setText("Pattern match: " + (int) matchPct + "%\nPlease try again!");
                    }
                });
            } else {
                // Set instructions text
                this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        setViewState(4);
                        instructionsText.setText("Pattern match: " + (int) matchPct + "%");
                    }
                });
            }
        }


    }
    @Override
    public void onMatchFound(TapAction tapAction, TapPattern signalPattern, double matchPct) {
    }






}
