package sg.edu.nus.taptask;

/**
 * Class representing a tap pattern
 */
public class TapPattern {
    public static final double MATCH_PERCENTAGE_THRESHOLD = 0.8; // TODO: test what values are good or let user decide.

    public double duration;
    public double frequency;
    public double[] pattern;

    boolean matchPattern(TapPattern pattern) {
        return matchPatternPercentage(pattern) >= MATCH_PERCENTAGE_THRESHOLD;
    }

    double matchPatternPercentage(TapPattern pattern) {
        // TODO: implement this
        return 0;
    }

}
