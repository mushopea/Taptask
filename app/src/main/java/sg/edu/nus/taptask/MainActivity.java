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
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import sg.edu.nus.taptask.model.TapActionVolume;
import sg.edu.nus.taptask.model.TaskList;
import sg.edu.nus.taptask.model.TapActionCall;
import sg.edu.nus.taptask.model.TapActionSMS;

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
        taptaskToggle = (SettingsToggle) this.findViewById(R.id.taptaskToggle);
        mRecyclerView = (RecyclerView) findViewById(R.id.taskList);
        final View shadowView = this.findViewById(R.id.shadowView);
        final FloatingActionsMenu fabButton = (FloatingActionsMenu)this.findViewById(R.id.multiple_actions);
        final RelativeLayout dashView = (RelativeLayout)this.findViewById(R.id.dashboard_view);

        // task list recycler view
        // to do: disable recycler view when there are no tasks and show prompt (arrow pointing to (+))
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new TaskAdapter(TaskList.getInstance().getTasks(), R.layout.row_task, this);
        mRecyclerView.setAdapter(mAdapter);


        // floating action button
        // set shadow overlay when it's pressed
        FloatingActionsMenu.OnFloatingActionsMenuUpdateListener listener = new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                shadowView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                shadowView.setVisibility(View.GONE);
            }
        };
        fabButton.setOnFloatingActionsMenuUpdateListener(listener);

        // collapse menu when dashboard is pressed
        shadowView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // ignore all touch events
                if (fabButton.isExpanded()) {
                    fabButton.collapse();
                    return true;
                }
                return false;
            }
        });

        // hide fab when recycler view is scrolled
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int x, int y) {
                if (y > 1) {
                    fabButton.animate().translationY(300);
                } else {
                    fabButton.animate().translationY(0);
                }
            }
        });
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

        if (toggle.isOn()) {
            startService(new Intent(this, TaptaskService.class));
        } else {
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

    public void onClickCallButton(View view){
        TapActionCall action = new TapActionCall(null, "97936499");
        action.performAction(MainActivity.this);
    }

    public void onClickVolButton(View view) {
        TapActionVolume action = new TapActionVolume(null);
        action.performAction(MainActivity.this);
    }

    public void onClickSMSButton(View view) {
        TapActionSMS action = new TapActionSMS(null, "hello world", "97936499");
        action.performAction(MainActivity.this);
    }

}
