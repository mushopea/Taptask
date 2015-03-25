package sg.edu.nus.taptask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;

import sg.edu.nus.taptask.model.TaskList;


public class MainActivity extends ActionBarActivity {

    private SettingsToggle taptaskToggle;
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        // enable taptask toggle
        taptaskToggle = (SettingsToggle)this.findViewById(R.id.taptaskToggle);

        // task list recycler view
        mRecyclerView = (RecyclerView)findViewById(R.id.taskList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new TaskAdapter(TaskList.getInstance().getTasks(), R.layout.row_task, this);
        mRecyclerView.setAdapter(mAdapter);

        // floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);
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
