package sg.edu.nus.taptask;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecordFragment extends Fragment {
    AudioBufferVisualizerSurfaceView audioBufferVisualizerSurfaceView = null;
    SoundSampler soundSampler = null;
    short[] buffer = null;

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

        return view;
    }

}
