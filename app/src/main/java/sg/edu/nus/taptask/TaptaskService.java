package sg.edu.nus.taptask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapActionManager;

public class TaptaskService extends Service implements AccelerometerSamplerListener {

    AccelerometerMatcher accelerometerMatcher = null;
    private NotificationManager notificationManager;

    public TaptaskService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Taptask service created", Toast.LENGTH_SHORT).show();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Taptask service started", Toast.LENGTH_SHORT).show();

        /*

        Notification notification = new Notification(R.drawable.reject, "Taptask", System.currentTimeMillis());
        Intent i=new Intent(this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi=PendingIntent.getActivity(this, 0, i, 0);

        notification.setLatestEventInfo(this, "Taptask", "Taptask", pi);
        notification.flags|=Notification.FLAG_NO_CLEAR;
        startForeground(0, notification);

        */

        notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        int icon = R.drawable.ic_stat_name;
        CharSequence tickerText = "TapTask is enabled";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        notification.flags|=Notification.FLAG_NO_CLEAR;
        Context context = getApplicationContext();
        CharSequence contentTitle = "TapTask is enabled";
        CharSequence contentText = "Big brother is watching you";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle,
                contentText, contentIntent);

        notificationManager.notify(1, notification);


        /*
        int dot = 200;
        int dash = 500;
        int short_gap = 200;
        int medium_gap = 500;
        int long_gap = 1000;
        // Beeeeep
        Vibrator v = (Vibrator) this.getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(dash);

        // Keep vibrating.
        long[] pattern = {long_gap, dot};
        v.vibrate(pattern, 0);
        */

        accelerometerMatcher = new AccelerometerMatcher(this.getBaseContext());
        accelerometerMatcher.calibrateSamplingRate();
        this.accelerometerMatcher.setAccelerometerSamplerListener(this);
        this.accelerometerMatcher.startSampling(10); // 10 sec buffer

        // Set patterns to match
        TapActionManager tapActionManager = TapActionManager.getInstance(getBaseContext());
        Log.e("Taptask Service", "Tap Actions: " + tapActionManager.tapActions.size());

        if (tapActionManager.tapActions.size() > 0) {
            accelerometerMatcher.setTapActionToMatch(tapActionManager.tapActions.get(0));
        }

        return START_STICKY;
    }

    @Override
    public void onMatchFound(TapAction tapAction, double[] signal, double matchPct) {
        Log.e("Taptask Service", "Match! " + matchPct);
        accelerometerMatcher.clearBuffer();
        tapAction.performAction(getBaseContext());
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
    public void onDestroy() {
        accelerometerMatcher.stopSampling();
        Toast.makeText(this, "Taptask service stopped", Toast.LENGTH_SHORT).show();
        notificationManager.cancel(1);
        //stopForeground(true);
    }
}
