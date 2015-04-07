package sg.edu.nus.taptask.model;

import sg.edu.nus.taptask.model.TapPattern;

/**
 * Class that describes an action to be done when a tap pattern is matched
 * Create child classes to add more actions
 */
public class TapAction {

    private TapPattern pattern; // Pattern that triggers action

    // TODO: Add more attributes

    public TapAction(TapPattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Stuff to be done when the action is triggered.
     * e.g. call someone, etc.
     */
    public void performAction() {

    }

    public TapPattern getPattern() {
        return pattern;
    }

    public void setPattern(TapPattern pattern) {
        this.pattern = pattern;
    }
}