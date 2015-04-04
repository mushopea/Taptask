package sg.edu.nus.taptask;

/**
 * Class that describes an action to be done when a tap pattern is matched
 * Create child classes to add more actions
 */
public class TapAction {
    public TapPattern pattern; // Pattern that triggers action

    /**
     * Stuff to be done when the action is triggered.
     * e.g. call someone, etc.
     */
    public void performAction() {

    }
}
