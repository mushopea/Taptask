package sg.edu.nus.taptask.model;

import android.content.Context;
import android.util.Log;

import sg.edu.nus.taptask.model.TapPattern;

/**
 * Class that describes an action to be done when a tap pattern is matched
 * Create child classes to add more actions
 */
public class TapAction {
    private TapPattern pattern; // Pattern that triggers action
    private boolean enabled = true;    // Enabled for matching

    public TapAction(TapPattern pattern) {
        this.pattern = pattern;
    }

    public String getName() {
        return "TapAction";
    }

    public String getImage() {
        return "task_icon_message";
    }

    /**
     * Stuff to be done when the action is triggered.
     * e.g. call someone, etc.
     */
    public boolean performAction(Context context) {
        Log.e("TapAction", "Performing action!");
        return true;
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
