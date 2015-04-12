package sg.edu.nus.taptask;


import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import sg.edu.nus.taptask.model.TaskList;
import sg.edu.nus.taptask.model.TapActionVolume;

public class MainActivity extends ActionBarActivity {

    private SettingsToggle taptaskToggle;
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        // enable taptask toggle
        taptaskToggle = (SettingsToggle)this.findViewById(R.id.taptaskToggle);

        // task list recycler view
        // to do: disable recycler view when there are no tasks and show prompt (arrow pointing to (+))
        mRecyclerView = (RecyclerView)findViewById(R.id.taskList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new TaskAdapter(TaskList.getInstance().getTasks(), R.layout.row_task, this);
        mRecyclerView.setAdapter(mAdapter);

        // floating action button


        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);

        // floating action button with submenus
        ImageView icon = new ImageView(this); // Create an icon
        icon.setImageDrawable(getResources().getDrawable(R.drawable.plus));
        FloatingActionButton actionButton = new FloatingActionButton.Builder(this).setContentView(icon).build();

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        ImageView itemIcon = new ImageView(this);
        itemIcon.setImageDrawable(getResources().getDrawable(R.drawable.task_icon_call));
        SubActionButton button_add_call = itemBuilder.setContentView(itemIcon).build();
        ImageView itemIcon2 = new ImageView(this);
        itemIcon.setImageDrawable(getResources().getDrawable(R.drawable.task_icon_call_reject));
        SubActionButton button_add_call_reject = itemBuilder.setContentView(itemIcon).build();
        ImageView itemIcon3 = new ImageView(this);
        itemIcon.setImageDrawable(getResources().getDrawable(R.drawable.task_icon_message));
        SubActionButton button_add_message = itemBuilder.setContentView(itemIcon).build();
        ImageView itemIcon4 = new ImageView(this);
        itemIcon.setImageDrawable(getResources().getDrawable(R.drawable.task_icon_volume));
        SubActionButton button_add_volume = itemBuilder.setContentView(itemIcon).build();
        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button_add_call)
                .addSubActionView(button_add_call_reject)
                .addSubActionView(button_add_message)
                .addSubActionView(button_add_volume)
                .attachTo(actionButton)
                .build();*/
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

    // go to add new task screen
    public void onClickAddButton(View view) {
        Log.e("Meow", "Add activity button triggered");

        Intent intent;
        intent = new Intent(this, AddCallTaskActivity.class);
        startActivity(intent);
    }

    public void onClickVolButton(View view) {
        TapActionVolume action = new TapActionVolume(null);
        action.performAction(MainActivity.this);
    }

}
