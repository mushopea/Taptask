package sg.edu.nus.taptask;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

public class SoundSampler {
    private static final int SAMPLING_FREQUENCY = 16000;
    private static final int CHANNELS = 16;
    private static final int AUDIO_ENCODING = 2;

    private AudioRecord audioRecord = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    private int bufferSize = AudioRecord.getMinBufferSize(SAMPLING_FREQUENCY, CHANNELS, AUDIO_ENCODING);
    private int bufferSizeShort = bufferSize/2;
    private short[] buffer = null;

    /**
     * SoundSampler Constructor
     * @throws Exception
     */
    public void SoundSampler() throws Exception {
        init();
    }

    private void init() throws Exception{
        try {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
            }
            audioRecord = new AudioRecord(1, SAMPLING_FREQUENCY, CHANNELS, AUDIO_ENCODING, bufferSize);
        } catch (Exception e) {
            Log.d("SoundSampler", "Error in SoundSampler" + e.getMessage());
            throw new Exception();
        }
    }

    /**
     * Starts audio recording, writes audio data into buffer
     */
    public void startRecording() {
        try {
            init();
        } catch (Exception e) {
            Log.d("SoundSampler", "Error in SoundSampler" + e.getMessage());
        }
        audioRecord.startRecording();
        isRecording = true;
        recordingThread = new Thread()
        {
            short[] soundSamplerBuffer = new short[bufferSizeShort];
            public void run()
            {
                while (isRecording) {
                    audioRecord.read(soundSamplerBuffer, 0, bufferSizeShort);
                    synchronized (buffer) {
                        System.arraycopy(soundSamplerBuffer, 0, buffer, 0, bufferSizeShort);
                    }
                }
            }
        };
        recordingThread.start();
    }

    /**
     * Stops audio recording
     */
    public void stop() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
        if (recordingThread.isAlive()) {
            isRecording = false;
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                Log.d("SoundSampler", "SoundSampler Interrupted" + e.getMessage());
            }
        }
    }

    /**
     * Gets required buffer size for buffer for a short array
     * @return bufferSize
     */
    public int getBufferSize() {
        return bufferSizeShort;
    }

    /**
     * Sets buffer
     * @param buffer buffer to set audio data into
     */
    public void setBuffer(short[] buffer) {
        this.buffer = buffer;
    }
}
