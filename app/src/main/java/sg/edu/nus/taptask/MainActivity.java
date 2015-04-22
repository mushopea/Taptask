package sg.edu.nus.taptask;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import sg.edu.nus.taptask.model.TapActionManager;
import sg.edu.nus.taptask.util.RecyclerViewAdapter;
import sg.edu.nus.taptask.util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends ActionBarActivity {

    private TapActionManager tapActionManager;
    private Activity activity;
    private DataUpdateReceiver dataUpdateReceiver;

    private SettingsToggle taptaskToggle;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;

    private View shadowView;
    private FloatingActionsMenu fabButton;
    private View addButtonGuide;

    private boolean onCreateFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onCreateFlag = true;
        activity = this;
        tapActionManager = TapActionManager.getInstance(this.getBaseContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        taptaskToggle = (SettingsToggle) this.findViewById(R.id.taptaskToggle);
        mRecyclerView = (RecyclerView) findViewById(R.id.taskList);

        // task list recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new FadeInLeftAnimator());
        mRecyclerView.setItemAnimator(new SlideInLeftAnimator());

        mAdapter = new RecyclerViewAdapter(R.layout.row_task, this);
        mRecyclerView.setAdapter(mAdapter);

        initFabListeners();
        showAddTaskGuide();

        new Thread(new Runnable() {
            public void run(){
                // Start service if enabled and not already running
                if (taptaskToggle.isOn() && !Utils.isMyServiceRunning(getBaseContext(), TaptaskService.class)) {
                    startService(new Intent(activity, TaptaskService.class));
                }
                // Load app icons in background
                Utils.getPackageMetaData(getBaseContext());
            }
        }).start();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!onCreateFlag) {
            mAdapter = new RecyclerViewAdapter(R.layout.row_task, activity);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            onCreateFlag = false;
        }

        if (dataUpdateReceiver == null) {
            dataUpdateReceiver = new DataUpdateReceiver();
        }
        IntentFilter intentFilter = new IntentFilter(TaptaskService.REFRESH_APP_INTENT);
        registerReceiver(dataUpdateReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dataUpdateReceiver != null) {
            unregisterReceiver(dataUpdateReceiver);
        }
    }

    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TaptaskService.REFRESH_APP_INTENT)) {
                tapActionManager.readTapActionManager();
                mAdapter = new RecyclerViewAdapter(R.layout.row_task, activity);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
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

        return super.onOptionsItemSelected(item);
    }

    // Determines whether to show the "Add Task here!" guide or not.
    public void showAddTaskGuide() {
        boolean tasksEmpty = false;

        if (mAdapter.getItemCount() == 0) {
            tasksEmpty = true;
        }

        if (tasksEmpty) {
            // Hide FAB menu if it's open
            if (fabButton.isExpanded()) {
                fabButton.collapse();
            }

            // Show guide if there are no tasks
            final View addButtonGuide = this.findViewById(R.id.addbuttonguide);
            final View myTasks = this.findViewById(R.id.myTasks);
            final View shadowView = this.findViewById(R.id.shadowView);

            myTasks.setVisibility(View.GONE);
            shadowView.setVisibility(View.VISIBLE);
            addButtonGuide.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.FlipInX)
                    .duration(1500)
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
    public void onClickCallButton(View view) {
        Log.e("Meow", "Call activity button triggered");
        Intent intent;
        intent = new Intent(this, AddCallTaskActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        fabButton.collapse();
    }

    public void onClickAppButton(View view){
        Log.e("Meow", "App activity button triggered");
        Intent intent;
        intent = new Intent(this, AddAppTaskActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        fabButton.collapse();
    }

    public void onClickVolButton(View view) {
        Log.e("Meow", "Volume activity button triggered");
        Intent intent;
        intent = new Intent(this, AddVolumeTaskActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        fabButton.collapse();
    }

    public void onClickSMSButton(View view) {
        Log.e("Meow", "SMS activity button triggered");
        Intent intent;
        intent = new Intent(this, AddSMSTaskActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        fabButton.collapse();
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
