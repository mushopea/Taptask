package sg.edu.nus.taptask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapActionManager;
import sg.edu.nus.taptask.model.TapPattern;

public class TaptaskService extends Service implements AccelerometerSamplerListener {

    public static final String REFRESH_APP_INTENT = "REFRESH_APP_INTENT";

    AccelerometerMatcher accelerometerMatcher = null;
    private TapActionManager tapActionManager;
    private NotificationManager notificationManager;

    public TaptaskService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.tapActionManager = TapActionManager.getInstance(getBaseContext());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        // Start notification
        notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        int icon = R.drawable.ic_stat_name;
        CharSequence tickerText = "TapTask is enabled";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        notification.flags|=Notification.FLAG_NO_CLEAR;
        Context context = getApplicationContext();
        CharSequence contentTitle = "TapTask is enabled";
        CharSequence contentText = "Click to go to Taptask";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle,
                contentText, contentIntent);

        notificationManager.notify(1, notification);


        // Start accelerometer

        accelerometerMatcher = new AccelerometerMatcher(this.getBaseContext());
        accelerometerMatcher.calibrateSamplingRate();
        this.accelerometerMatcher.setAccelerometerSamplerListener(this);
        this.accelerometerMatcher.startSampling(5);

        // Set patterns to match
        TapActionManager tapActionManager = TapActionManager.getInstance(getBaseContext());
        Log.e("Taptask Service", "Number of Tap Actions: " + tapActionManager.tapActions.size());

        if (tapActionManager.tapActions.size() > 0) {
            accelerometerMatcher.setTapActionsToMatch(tapActionManager.tapActions);
        }

        return START_STICKY;
    }


    @Override
    public void onMatchFound(TapAction tapAction, TapPattern signalPattern, double matchPct) {
        Log.e("Taptask Service", "Match! " + matchPct);
        tapAction.updateLastTriggerTime();
        new Thread(new Runnable() {
            public void run(){
                tapActionManager.saveTapActionManager();
                sendBroadcast(new Intent(REFRESH_APP_INTENT));
            }
        }).start();

        accelerometerMatcher.clearBuffer();
        tapAction.performAction(getBaseContext());

        Toast.makeText(this, "Taptask Action: " + tapAction.getName(), Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) this.getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);
    }

    @Override
    public void onSamplingStart() {
        Log.e("Taptask Service", "Sampling Started");
    }
    @Override
    public void onSamplingStop() {
        Log.e("Taptask Service", "Sampling Stopped");
    }
    @Override
    public void onRecordingDone() {}
    @Override
    public void onRecordingDelayOver() {}
    @Override
    public void onCalibrationDone() {
        Log.e("Taptask Service", "Calibration Done");
    }

    @Override
    public void onDestroy() {
        Log.e("Taptask Service", "Service Destroyed");
        accelerometerMatcher.stopSampling();
        notificationManager.cancel(1);
        stopForeground(true);
    }
}
