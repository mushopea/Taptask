package sg.edu.nus.taptask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.taptask.model.TapActionApp;
import sg.edu.nus.taptask.model.TapActionManager;
import sg.edu.nus.taptask.util.ArrayAdapterWithIcon;


public class AddAppTaskActivity extends ActionBarActivity {

    private TextView appNameField;
    private String appPackageName;
    private TapActionManager tapActionManager;
    private List<String> packageNames;
    private List<String> appNames;
    private List<Integer> packageIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_app_task);
        appNameField = (TextView) findViewById(R.id.appName);
        tapActionManager = TapActionManager.getInstance(getBaseContext());
        appNameField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectApp(v);
            }
        });
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
        Log.e("Meow", "Recording call activity screen activated");
        String appName = appNameField.getText().toString();
        TapActionApp action = new TapActionApp(null, appName, appPackageName);
        action.performAction(this);
        /**
         * Commented out for testing

         tapActionManager.setCurrentTapAction(action);

         Intent intent;
         intent = new Intent(this, RecordActivity.class);
         startActivity(intent);

         */
    }

    public void selectApp(View view){

        packageNames = new ArrayList<>();
        appNames = new ArrayList<>();
        packageIcons = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);



        for (ApplicationInfo packageInfo : packages) {
            boolean isLaunchable = (pm.getLaunchIntentForPackage(packageInfo.packageName) != null );
            if (isLaunchable) {
                packageNames.add(packageInfo.packageName);
                appNames.add((String) (packageInfo != null ? pm.getApplicationLabel(packageInfo) : "(unknown)"));
                if(true){
                    packageIcons.add(R.drawable.reject);
                } else {
                    packageIcons.add(packageInfo.icon);
                }
            }

        }
        System.out.println(packageIcons);

        final String[] appNameItems = appNames.toArray(new String[appNames.size()]);
        final Integer[] iconItems = packageIcons.toArray(new Integer[packageIcons.size()]);

        ListAdapter adapter = new ArrayAdapterWithIcon(this, appNameItems, iconItems);

        builder.setTitle("Select an App")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        appNameField.setText((appNameItems[which]).toString());
                        appPackageName = packageNames.get(which);
                    }
                });

        AlertDialog choose = builder.create();
        choose.show();
    }

}
