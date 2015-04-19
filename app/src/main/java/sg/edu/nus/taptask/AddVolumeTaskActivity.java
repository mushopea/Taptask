package sg.edu.nus.taptask;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import sg.edu.nus.taptask.model.TapActionManager;
import sg.edu.nus.taptask.model.TapActionVolume;


public class AddVolumeTaskActivity extends ActionBarActivity {

    private TapActionManager tapActionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_volume_task);
        tapActionManager = TapActionManager.getInstance(getBaseContext());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_call_task, menu);
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


    // go to add new task screen
    public void onClickContinueButton(View view) {
        Log.e("Meow", "Recording volume activity screen activated");

        TapActionVolume action = new TapActionVolume(null);
        tapActionManager.setCurrentTapAction(action);

        Intent intent;
        intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }


}
