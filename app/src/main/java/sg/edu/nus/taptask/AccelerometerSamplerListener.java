package sg.edu.nus.taptask;

public interface AccelerometerSamplerListener {
    public void onSamplingStart();
    public void onSamplingStop();
    public void onRecordingDelayOver();
    public void onRecordingDone();
}
