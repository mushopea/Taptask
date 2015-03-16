package sg.edu.nus.taptask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class TaptaskService extends Service {
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
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Taptask service stopped", Toast.LENGTH_SHORT).show();
    }
}
