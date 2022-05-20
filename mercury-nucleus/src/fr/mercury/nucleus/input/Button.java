package fr.mercury.nucleus.input;

public enum Button {

    UNDEFINED,
    
    MOUSE_LEFT,
    
    MOUSE_RIGHT,
    
    MOUSE_MIDDLE;
    
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
