package sg.edu.nus.taptask;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import sg.edu.nus.taptask.model.TapActionCall;
import sg.edu.nus.taptask.model.TapActionManager;
import sg.edu.nus.taptask.model.TapActionSMS;
import sg.edu.nus.taptask.model.TapActionVolume;
import sg.edu.nus.taptask.util.Utils;

public class MainActivity extends ActionBarActivity {

    private SettingsToggle taptaskToggle;
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;

    private View shadowView;
    private FloatingActionsMenu fabButton;
    private View addButtonGuide;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        taptaskToggle = (SettingsToggle) this.findViewById(R.id.taptaskToggle);
        mRecyclerView = (RecyclerView) findViewById(R.id.taskList);

        // task list recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TaskAdapter(R.layout.row_task, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new SlideInLeftAnimator());

        initFabListeners();
        showAddTaskGuide();

        // Start service if enabled and not already running
        if (taptaskToggle.isOn() && !Utils.isMyServiceRunning(getBaseContext(), TaptaskService.class)) {
            startService(new Intent(this, TaptaskService.class));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list onResume
        mAdapter = new TaskAdapter(R.layout.row_task, this);
        mRecyclerView.setAdapter(mAdapter);
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

    // Determines whether to show the "Add Task here!" guide or not.
    public void showAddTaskGuide() {
        boolean tasksEmpty = false;
        if (mAdapter.getItemCount() == 0) {
            tasksEmpty = true;
        }

        if (tasksEmpty) {
            // Show guide if there are no tasks
            final View addButtonGuide = this.findViewById(R.id.addbuttonguide);
            final View myTasks = this.findViewById(R.id.myTasks);
            final View shadowView = this.findViewById(R.id.shadowView);

            myTasks.setVisibility(View.GONE);
            shadowView.setVisibility(View.VISIBLE);
            addButtonGuide.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.FlipInX)
                    .duration(1000)
                    .playOn(addButtonGuide);
        }
    }

    // Listens for scrolling/pressing on the screen and modifies the FAB accordingly
    public void initFabListeners() {
        shadowView = this.findViewById(R.id.shadowView);
        fabButton = (FloatingActionsMenu)this.findViewById(R.id.multiple_actions);
        addButtonGuide = this.findViewById(R.id.addbuttonguide);


        // floating action button
        // set shadow overlay when it's pressed
        FloatingActionsMenu.OnFloatingActionsMenuUpdateListener listener = new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                shadowView.setVisibility(View.VISIBLE);
                addButtonGuide.setVisibility(View.GONE);
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

    public void onClickTaptaskToggle(View view) {
        SettingsToggle toggle = ((SettingsToggle)view);
        if (toggle.isAnimating()) {
            return;
        }

        if (toggle.isOn() && !Utils.isMyServiceRunning(getBaseContext(), TaptaskService.class)) {
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

    public void onClickAppButton(View view){
        Log.e("Meow", "App activity button triggered");
        Intent intent;
        intent = new Intent(this, AddAppTaskActivity.class);
        startActivity(intent);
    }

    public void onClickCallButton(View view){
        TapActionCall action = new TapActionCall(null, "97936499", "zhang yiwen");
        action.performAction(MainActivity.this);
    }

    public void onClickVolButton(View view) {
        Log.e("Meow", "Volume activity button triggered");
        Intent intent;
        intent = new Intent(this, AddVolumeTaskActivity.class);
        startActivity(intent);
    }

    public void onClickSMSButton(View view) {
        Log.e("Meow", "SMS activity button triggered");
        Intent intent;
        intent = new Intent(this, AddSMSTaskActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (fabButton.isExpanded()) {
            // Collapse floating menu if open
            fabButton.collapse();
        } else {
            super.onBackPressed();
        }
    }
}
