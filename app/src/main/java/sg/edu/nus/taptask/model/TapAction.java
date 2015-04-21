package sg.edu.nus.taptask.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import sg.edu.nus.taptask.util.Utils;


/**
 * Class that describes an action to be done when a tap pattern is matched
 * Create child classes to add more actions
 */
public class TapAction {
    private TapPattern pattern; // Pattern that triggers action
    private boolean enabled = true;    // Enabled for matching
    private Date lastTriggerTime = null;

    public TapAction(TapPattern pattern) {
        this.pattern = pattern;
    }

    public String getName() {
        return "TapAction";
    }

    public String getImage() {
        return "task_icon_message";
    }

    public String getDetails(){
        return getName();
    }

    /**
     * Stuff to be done when the action is triggered.
     * e.g. call someone, etc.
     */
    public boolean performAction(Context context) {
        Log.e("TapAction", "Performing action!");
        return true;
    }

    public String getLastTriggerTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            TimeZone tz = TimeZone.getDefault();
            sdf.setTimeZone(tz);
            return "Last Triggered: " + sdf.format(lastTriggerTime);
        } catch(NullPointerException e) {
            return "Last Triggered: Never";
        }

    }

    public void updateLastTriggerTime(){
        lastTriggerTime = new Date();
    }

    public TapPattern getPattern() {
        return pattern;
    }

    public void setPattern(TapPattern pattern) {
        this.pattern = pattern;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
