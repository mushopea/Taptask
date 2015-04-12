package sg.edu.nus.taptask.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class TapActionManager {
    public transient String saveFileName = "TapActionManager.json";
    private transient static TapActionManager ourInstance = new TapActionManager();
    public static TapActionManager getInstance(Context context) {
        if (ourInstance.context == null) {
            ourInstance.context = context;
        }
        ourInstance.readTapActionManager();
        return ourInstance;
    }
    private transient Context context = null;

    // Attributes
    public ArrayList<TapAction> tapActions = new ArrayList<TapAction>();
    private transient TapAction currentTapAction = null;

    public TapAction getCurrentTapAction() {
        return currentTapAction;
    }

    public void setCurrentTapAction(TapAction currentTapAction) {
        this.currentTapAction = currentTapAction;
    }

    public void addTapAction(TapAction tapAction) {
        tapActions.add(tapAction);
        saveTapActionManager();
    }

    public void removeAllTasks() {
        tapActions.clear();
        saveTapActionManager();
    }

    public void saveTapActionManager() {
        Gson gson = new Gson();
        String jsonTapActionManager = gson.toJson(this);
        Log.e("TapActionManager", "Saving: " + jsonTapActionManager);
        writeToFile(jsonTapActionManager);
    }

    public void readTapActionManager() {
        String jsonTapActionManager = readFromFile();
        if (jsonTapActionManager.equals("")) {
            return;
        }
        Gson gson = new Gson();
        TapActionManager tapActionManager = gson.fromJson(jsonTapActionManager, TapActionManager.class);
        // Copy attributes
        this.tapActions = tapActionManager.tapActions;

    }

    // From: http://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android
    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(saveFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("TapActionManager", "File write failed: " + e.toString());
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
            Log.e("TapActionManager", "File not found: " + e.toString());
            return "";
        } catch (IOException e) {
            Log.e("TapActionManager", "Can not read file: " + e.toString());
            return "";
        }
        return ret;
    }

}
