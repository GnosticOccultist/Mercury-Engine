package fr.mercury.nucleus.input;

/**
 * <code>InputState</code> enumerates the available states for a specific input.
 * It is described as a three-valued logic, either being off, positive or
 * negative state.
 * 
 * @author GnosticOccultist
 */
public enum InputState {

    /**
     * The off input state represents an input being inactive.
     */
    OFF(0),
    /**
     * The positive input state represents an input being active and described by a
     * positive value.
     */
    POSITIVE(1),
    /**
     * The positive input state represents an input being active and described by a
     * negative value.
     */
    NEGATIVE(-1);

    /**
     * The numeric value describing the state.
     */
    private int value;

    private InputState(int value) {
        this.value = value;
    }

    /**
     * Return a numeric value describing the <code>InputState</code>. <br>
     * <li>{@link #OFF} represented by 0.
     * <li>{@link #POSITIVE} represented by 1.
     * <li>{@link #NEGATIVE} represented by -1.
     * 
     * @return A numeric value to describe the state.
     */
    public int asValue() {
        return value;
    }

    /**
     * Return the <code>InputState</code> value represented by the provided value.
     * 
     * @param value The numeric value (&ge;-1, &le;1).
     * @return The state enum matching the provided index.
     * 
     * @throws IllegalArgumentException Thrown if the index is out of range.
     */
    public static InputState fromValue(int value) {
        switch (value) {
        case -1:
            return InputState.NEGATIVE;
        case 0:
            return InputState.OFF;
        case 1:
            return InputState.POSITIVE;
        default:
            throw new IllegalArgumentException("Value is out of range!");
        }
    }

    /**
     * Return the <code>InputState</code> value represented by the provided value.
     * 
     * @param value The numeric value (&ge;-1, &le;1).
     * @return The state enum matching the provided index.
     */
    public static InputState fromValue(double value) {
        if (value < -0.01) {
            return InputState.NEGATIVE;
        } else if (value > 0.01) {
            return InputState.POSITIVE;
        } else {
            return InputState.OFF;
        } 
    }
}
