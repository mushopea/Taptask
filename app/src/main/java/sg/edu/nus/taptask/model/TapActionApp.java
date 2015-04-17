package sg.edu.nus.taptask.model;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Yiwen on 12/4/2015.
 */
public class TapActionApp extends TapAction {

    private String appPackageName;
    private String appName;

    public TapActionApp(TapPattern pattern, String appName, String appPackageName) {
        super(pattern);
        this.appPackageName = appPackageName;
        this.appName = appName;
    }

    public String getName() {
        return "Open " + appName;
    }

    public String getImage() {
        return "task_icon_volume";
    }

    public boolean performAction(Context context) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
        System.out.println(appPackageName);
        context.startActivity(launchIntent);
        return true;
    }


}
