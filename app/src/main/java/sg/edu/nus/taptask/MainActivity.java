package sg.edu.nus.taptask;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    private SettingsToggle taptaskToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        taptaskToggle = (SettingsToggle)this.findViewById(R.id.taptaskToggle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickTaptaskToggle(View view) {
        SettingsToggle toggle = ((SettingsToggle)view);
        if (toggle.isAnimating()) {
            return;
        }

        int dot = 200;
        int dash = 500;
        int short_gap = 200;
        int medium_gap = 500;
        int long_gap = 1000;

        Vibrator v = (Vibrator) this.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        if (toggle.isOn()) {
            // Beeeeep
            v.vibrate(dash);
            startService(new Intent(this, TaptaskService.class));
        } else {
            // Beep Beep Beep
            long[] pattern = {0, dot, short_gap, dot, short_gap, dot};
            v.vibrate(pattern, -1);
            stopService(new Intent(this, TaptaskService.class));
        }
    }

}
