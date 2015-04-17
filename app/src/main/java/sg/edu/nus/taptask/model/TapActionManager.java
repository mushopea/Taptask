package sg.edu.nus.taptask.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    public boolean isTasksEmpty() {
        return tapActions.isEmpty();
    }

    public void saveTapActionManager() {

        final RuntimeTypeAdapterFactory<TapAction> typeFactory = RuntimeTypeAdapterFactory
                .of(TapAction.class, "type") // Here you specify which is the parent class and what field particularizes the child class.
                .registerSubtype(TapActionCall.class, "TapActionCall") // if the flag equals the class name, you can skip the second parameter. This is only necessary, when the "type" field does not equal the class name.
                .registerSubtype(TapActionSMS.class, "TapActionSMS")
                .registerSubtype(TapActionApp.class, "TapActionApp")
                .registerSubtype(TapActionVolume.class, "TapActionVolume");

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(typeFactory)
                .create();

        String jsonTapActionManager = gson.toJson(this);
        Log.e("TapActionManager", "Saving: " + jsonTapActionManager);
        writeToFile(jsonTapActionManager);
    }

    public void readTapActionManager() {
        String jsonTapActionManager = readFromFile();
        if (jsonTapActionManager.equals("")) {
            return;
        }

        final RuntimeTypeAdapterFactory<TapAction> typeFactory = RuntimeTypeAdapterFactory
                .of(TapAction.class, "type") // Here you specify which is the parent class and what field particularizes the child class.
                .registerSubtype(TapActionCall.class, "TapActionCall") // if the flag equals the class name, you can skip the second parameter. This is only necessary, when the "type" field does not equal the class name.
                .registerSubtype(TapActionSMS.class, "TapActionSMS")
                .registerSubtype(TapActionApp.class, "TapActionApp")
                .registerSubtype(TapActionVolume.class, "TapActionVolume");

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(typeFactory)
                .create();
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
