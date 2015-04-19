package sg.edu.nus.taptask;

import android.app.Application;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class TaptaskApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-Light.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }
}