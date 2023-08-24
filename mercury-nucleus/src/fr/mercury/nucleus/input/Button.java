package fr.mercury.nucleus.input;

/**
 * <code>Button</code> enumerates the recognized input buttons.
 * 
 * @author GnosticOccultist
 */
public enum Button {

    /**
     * An undefined input button.
     */
    UNDEFINED,
    /**
     * The mouse left input button.
     */
    MOUSE_LEFT,
    /**
     * The mouse right input button.
     */
    MOUSE_RIGHT,
    /**
     * The mouse middle input button.
     */
    MOUSE_MIDDLE;

    /**
     * Return the <code>Button</code> value represented by the provided index.
     * 
     * @param index The index value (&ge;-1, &le;2).
     * @return A button enum matching the provided index.
     * 
     * @throws IllegalArgumentException Thrown if the index is out of range.
     */
    public static Button fromButtonIndex(int index) {
        switch (index) {
            case -1:
                return Button.UNDEFINED;
            case 0:
                return Button.MOUSE_LEFT;
            case 1:
                return Button.MOUSE_RIGHT;
            case 2:
                return Button.MOUSE_MIDDLE;
            default:
                throw new IllegalArgumentException("Index is out of range!");
        }
    }
}
