package sg.edu.nus.taptask.util;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    // Utility function to check if a service is running
    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static final ArrayList<String> packageNames = new ArrayList<String>();
    public static ArrayList<String> appNames = new ArrayList<String>();
    public static ArrayList<Drawable> packageIcons = new ArrayList<Drawable>();
    public static String[] appNameItems;
    public static Drawable[] iconItems;
    public static void getPackageMetaData(Context context) {
        if (packageNames.size() > 0) {
            return;
        }
        synchronized (packageNames) {
            final PackageManager pm = context.getPackageManager();
            final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo packageInfo : packages) {
                boolean isLaunchable = (pm.getLaunchIntentForPackage(packageInfo.packageName) != null);
                if (isLaunchable) {
                    packageNames.add(packageInfo.packageName);
                    appNames.add((String) (packageInfo != null ? pm.getApplicationLabel(packageInfo) : "(unknown)"));
                    Drawable appIcon = packageInfo.loadIcon(context.getPackageManager());
                    appIcon = resizeImage(context, appIcon, 144, 144);
                    packageIcons.add(appIcon);
                }
            }

            appNameItems = Utils.appNames.toArray(new String[Utils.appNames.size()]);
            iconItems = Utils.packageIcons.toArray(new Drawable[Utils.packageIcons.size()]);
        }
    }


    public static Drawable resizeImage (Context context, Drawable image, int sizeX, int sizeY) {
        if ((image == null) || !(image instanceof BitmapDrawable)) {
            return image;
        }
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, sizeX, sizeY, false);
        image = new BitmapDrawable(context.getResources(), bitmapResized);
        return image;
    }

}
