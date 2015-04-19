package sg.edu.nus.taptask.model;

import android.content.Context;
import android.util.Log;


/**
 * Class that describes an action to be done when a tap pattern is matched
 * Create child classes to add more actions
 */
public class TapAction {
    private TapPattern pattern; // Pattern that triggers action

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
}
