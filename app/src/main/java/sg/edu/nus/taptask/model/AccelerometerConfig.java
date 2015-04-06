package sg.edu.nus.taptask.model;


import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class AccelerometerConfig {
    public String saveFileName = "AccelerometerConfig.json";
    private transient static AccelerometerConfig ourInstance = new AccelerometerConfig();
    private transient Context context = null;
    public static AccelerometerConfig getInstance(Context context) {
        if (ourInstance.context == null) {
            ourInstance.context = context;
        }
        return ourInstance;
    }

    public int[] sensorDelayList = {SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI,
            SensorManager.SENSOR_DELAY_GAME,
            SensorManager.SENSOR_DELAY_FASTEST};
    public double[] sensorDelaySamplingRate = new double[sensorDelayList.length];
    public double gravityOffset = 10.0; // Rough estimate is enough
    public double minSamplingFrequency = 90; // TODO: Test to see how low the sampling rate can go. ~200 works, 49 does not seem to work.
    public double samplingFrequencyToUse = 0;
    public int sensorDelayToUse = 0;


    private AccelerometerConfig() {
        // TODO: Read from file
    }

    public int getSensorDelayToUse() {

        return sensorDelayToUse;
    }

    public double getSamplingFrequencyToUse() {
        return samplingFrequencyToUse;
    }

    public void saveAccelerometerConfig() {
        Gson gson = new Gson();
        String jsonAccelerometerConfig = gson.toJson(this);
        Log.e("WRITING", "start" + jsonAccelerometerConfig);
        writeToFile(jsonAccelerometerConfig);
    }

    public void readAccelerometerConfig() {
        Log.e("READING", "start");
        String jsonAccelerometerConfig = readFromFile();
        if (jsonAccelerometerConfig.equals("")) {
            return;
        }
        Gson gson = new Gson();
        AccelerometerConfig accelerometerConfig = gson.fromJson(jsonAccelerometerConfig, AccelerometerConfig.class);

        this.saveFileName = accelerometerConfig.saveFileName;
        this.sensorDelayList = accelerometerConfig.sensorDelayList;
        this.sensorDelaySamplingRate = accelerometerConfig.sensorDelaySamplingRate;
        this.gravityOffset = accelerometerConfig.gravityOffset;
        this.minSamplingFrequency = accelerometerConfig.minSamplingFrequency;
        this.samplingFrequencyToUse = accelerometerConfig.samplingFrequencyToUse;
        this.sensorDelayToUse = accelerometerConfig.sensorDelayToUse;
    }

    public void calculateFrequencyToUse() {
        int minSensorDelay = sensorDelayList[sensorDelayList.length-1];
        double samplingFrequency = 0;
        for (int i=0 ; i<sensorDelayList.length ; i++) {
            if (sensorDelaySamplingRate[i] >= minSamplingFrequency) {
                minSensorDelay = sensorDelayList[i];
                samplingFrequency = sensorDelaySamplingRate[i];
                break;
            }
        }
        if (samplingFrequency < minSamplingFrequency) {
            Log.w("AccelerometerConfig", "Sampling frequency of " + samplingFrequency + " less than minimum of " + minSamplingFrequency + "\n");
        }
        samplingFrequencyToUse = samplingFrequency;
        sensorDelayToUse = minSensorDelay;
    }

    // From: http://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android
    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(saveFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("AccelerometerConfig", "File write failed: " + e.toString());
        }
    }

    // From: http://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android
    private String readFromFile() {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(saveFileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("AccelerometerConfig", "File not found: " + e.toString());
            return "";
        } catch (IOException e) {
            Log.e("AccelerometerConfig", "Can not read file: " + e.toString());
            return "";
        }
        return ret;
    }
}
