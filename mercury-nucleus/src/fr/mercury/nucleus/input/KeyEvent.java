package fr.mercury.nucleus.input;

import static fr.mercury.nucleus.input.Input.Modifiers.ALT_DOWN;
import static fr.mercury.nucleus.input.Input.Modifiers.CONTROL_DOWN;
import static fr.mercury.nucleus.input.Input.Modifiers.SHIFT_DOWN;
import static fr.mercury.nucleus.input.Input.Modifiers.hasModifiers;

import fr.alchemy.utilities.event.EventType;

public class KeyEvent extends InputEvent implements Input {

	/**
	 * The event type returned whenever a keyboard key has been pressed, it the key is held 
	 * down during multiple events polling it will raise a {@link #KEY_REPEATED} instead until
	 * the key is released.
	 */
    public static final EventType<KeyEvent> KEY_PRESSED =
           EventType.create("KEY_PRESSED", KeyEvent.class);

    /**
	 * The event type returned whenever a keyboard key has been released. This event type
	 * will reset the repeating count. 
	 */
    public static final EventType<KeyEvent> KEY_RELEASED =
           EventType.create("KEY_RELEASED", KeyEvent.class);
    
    /**
	 * The event type returned whenever a keyboard key has been pressed and maintained down
	 * during multiple events polling without releasing it.
	 */
    public static final EventType<KeyEvent> KEY_REPEATED =
           EventType.create("KEY_REPEATED", KeyEvent.class);

    /**
     * The event type returned whenever a keyboard key has been typed. This event must
     * be reserved to text input feature because it only provides the key character that was
     * typed or pressed continuously.
     */
    public static final EventType<KeyEvent> KEY_TYPED =
            EventType.create("KEY_TYPED", KeyEvent.class);
	
    /**
	 * The code of the key.
	 */
	private final int code;
	/**
	 * The character of the key.
	 */
	private final char keyChar;
	/**
	 * The set of key modifiers interacted.
	 */
	private final int modifiers;
	/**
	 * The time the repeated event type has been repeated.
	 */
	private final int repeatCount;
    
	/**
	 * Instantiates a new <code>KeyEvent</code> with the given {@link EventType} and other informations
	 * such as key code, character or modifiers.
	 * <p>
	 * Note that the interaction depends on the event type, for example it will be pressed if the event 
	 * type is {@link #KEY_PRESSED}.
	 * 
	 * @param type   	The mouse event type.
	 * @param code	 	The code of the key.
	 * @param keyChar	The character of the key.
	 * @param modifiers The set of modifiers key interacted.
	 */
	public KeyEvent(EventType<KeyEvent> type, int code, char keyChar, int modifiers) {
		this(type, code, keyChar, modifiers, 0);
	}
	
	/**
	 * Instantiates a new <code>KeyEvent</code> with the given {@link EventType} and other informations
	 * such as key code, character or modifiers.
	 * <p>
	 * Note that the interaction depends on the event type, for example it will be pressed if the event 
	 * type is {@link #KEY_PRESSED}.
	 * 
	 * @param type   	  The mouse event type.
	 * @param code	 	  The code of the key.
	 * @param keyChar	  The character of the key.
	 * @param modifiers   The set of modifiers key interacted.
	 * @param repeatCount The count of repeated key pressed, only for repeated event types.
	 */
	public KeyEvent(EventType<KeyEvent> type, int code, char keyChar, int modifiers, int repeatCount) {
		super(type);
		this.code = code;
		this.keyChar = keyChar;
		this.modifiers = modifiers;
		this.repeatCount = repeatCount;
	}
	
	/**
	 * Return the code of the key which has raised the <code>KeyEvent</code>.
	 * Only provided for {@link #KEY_PRESSED}, {@link #KEY_RELEASED} and {@link #KEY_REPEATED}.
	 * 
	 * @return The code of the key.
	 */
	public int getCode() {
		return code;
	}
	
	/**
	 * Return the character of the key which has raised the <code>KeyEvent</code>.
	 * Only provided for {@link #KEY_TYPED} events.
	 * 
	 * @return The character of the key, or '\0' if not provided for the event type.
	 */
	public char getKeyChar() {
		return keyChar;
	}
	
	/**
	 * Return the set of modifier keys which were held down during the <code>KeyEvent</code>.
	 * 
	 * @return The set of modifier keys.
	 */
	public int getModifiers() {
		return modifiers;
	}
	
	/**
	 * Whether the shift key was held down during the <code>KeyEvent</code>.
	 * 
	 * @return Whether the shift key was held down.
	 */
	public boolean isShiftDown() {
		return hasModifiers(modifiers, SHIFT_DOWN);
	}
	
	/**
	 * Whether the control key was held down during the <code>KeyEvent</code>.
	 * 
	 * @return Whether the control key was held down.
	 */
	public boolean isControlDown() {
		return hasModifiers(modifiers, CONTROL_DOWN);
	}
	
	/**
	 * Whether the alt key was held down during the <code>KeyEvent</code>.
	 * 
	 * @return Whether the alt key was held down.
	 */
	public boolean isAltDown() {
		return hasModifiers(modifiers, ALT_DOWN);
	}
	
	/**
	 * Return how many time the {@link #KEY_REPEATED} event has been called since the {@link #KEY_PRESSED}
	 * event. Note that it doesn't count the first time the key was pressed but only how many times the pressed
	 * was extended.
	 * 
	 * @return The time count of repeated key pressed event.
	 * 
	 * @throws IllegalStateException Thrown if the method is called for an event type different of {@link #KEY_REPEATED}.
	 */
	public int getRepeatCount() {
		if(!type.equals(KEY_REPEATED)) {
			throw new IllegalStateException("Can only access the repeat "
					+ "events count on KEY_REPEATED event type!");
		}
		
		return repeatCount;
	}
	
	@Override
	public String toString() {
		String modStr = ", modifiers= ";
		modStr += isShiftDown() ? "SHIFT " : "";
		modStr += isControlDown() ? "CTRL " : "";
		modStr += isAltDown() ? "ALT" : "";
		return getClass().getSimpleName() + "[ type= " + type + ", code= " + code + ", char= " + keyChar + modStr + ", repeatCount= " + repeatCount + " ]";
	}
}
