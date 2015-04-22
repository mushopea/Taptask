package sg.edu.nus.taptask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;
import sg.edu.nus.taptask.model.TapActionApp;
import sg.edu.nus.taptask.model.TapActionManager;
import sg.edu.nus.taptask.util.ArrayAdapterWithIcon;
import sg.edu.nus.taptask.util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class AddAppTaskActivity extends ActionBarActivity {

    private Activity activity;
    private EditText appNameField;
    private String appPackageName;
    private TapActionManager tapActionManager;
    private AlertDialog choose;
    private FancyButton continueButton;

    private boolean isFormValid(){
        boolean isAppNameValid = appNameField.getText().toString().length() > 0;
        boolean isValid = isAppNameValid;
        return isValid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_app_task);
        appNameField = (EditText) findViewById(R.id.appName);
        continueButton = (FancyButton) findViewById(R.id.button);
        continueButton.setEnabled(false);
        continueButton.setVisibility(View.GONE);
        appNameField.setFocusable(false);
        appNameField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectApp(v);
            }
        });
        tapActionManager = TapActionManager.getInstance(getBaseContext());

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] appNameItems;
        final Drawable[] iconItems;
        synchronized (Utils.packageNames) {
            appNameItems = Utils.appNameItems;
            iconItems = Utils.iconItems;
        }

        ListAdapter adapter = new ArrayAdapterWithIcon(activity, appNameItems, iconItems);
        builder.setTitle("Select an App")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        appNameField.setText((appNameItems[which]));
                        appPackageName = Utils.packageNames.get(which);
                        Log.e("appPackageName", appPackageName);
                        if (isFormValid()) {
                            continueButton.setEnabled(true);
                            continueButton.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.FadeInUp)
                                    .duration(1000)
                                    .playOn(continueButton);
                        } else {
                            continueButton.setEnabled(false);
                            continueButton.setVisibility(View.GONE);
                        }
                    }
                });
        choose = builder.create();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        choose = null;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    // go to add new task screen
    public void onClickContinueButton(View view) {
        Log.e("Meow", "Recording app activity screen activated");
        String appName = appNameField.getText().toString();
        TapActionApp action = new TapActionApp(null, appName, appPackageName);
        tapActionManager.setCurrentTapAction(action);
        Intent intent;
        intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }

    public void selectApp(final View v){
        try {
            choose.show();
        } catch (Exception e) {
            Log.e("AddAppTaskActivity", "Error:" + e.getMessage());
            return;
        }
        if (isFormValid()) {
            continueButton.setEnabled(true);
            continueButton.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.FadeInUp)
                    .duration(1000)
                    .playOn(continueButton);
        } else {
            continueButton.setEnabled(false);
            continueButton.setVisibility(View.GONE);
        }
    }

}
