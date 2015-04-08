package sg.edu.nus.taptask;

import sg.edu.nus.taptask.model.TapPattern;

public interface AccelerometerSamplerListener {
    public void onSamplingStart();
    public void onSamplingStop();
    public void onRecordingDelayOver();
    public void onRecordingDone();
    public void onMatchFound(TapPattern pattern, double[] signal, double matchPct);
}
