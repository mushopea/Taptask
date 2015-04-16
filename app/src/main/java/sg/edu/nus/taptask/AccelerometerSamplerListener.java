package sg.edu.nus.taptask;

import sg.edu.nus.taptask.model.TapAction;

public interface AccelerometerSamplerListener {
    public void onSamplingStart();
    public void onSamplingStop();
    public void onCalibrationDone();
    public void onRecordingDelayOver();
    public void onRecordingDone();
    public void onMatchFound(TapAction tapAction, double[] signal, double matchPct);

}
