package sg.edu.nus.taptask.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Yiwen on 12/4/2015.
 */
public class TapActionApp extends TapAction {

    private String appPackageName;
    private String appName;

    public TapActionApp(TapPattern pattern, String appName, String appPackageName) {
        super(pattern);
        Log.e("TapActionApp", appPackageName);
        this.appPackageName = appPackageName;
        this.appName = appName;
    }

    public String getName() {
        return "Open " + appName;
    }

    public String getImage() {
        return "task_icon_open_app";
    }

    public boolean performAction(Context context) {
        Log.e("TapActionApp Performed", appPackageName);
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
        context.startActivity(launchIntent);
        return true;
    }


}
