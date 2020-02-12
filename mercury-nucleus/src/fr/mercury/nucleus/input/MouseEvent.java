package fr.mercury.nucleus.input;

import static fr.mercury.nucleus.input.Input.Buttons.BUTTON_UNDEFINED;
import static fr.mercury.nucleus.input.Input.Modifiers.ALT_DOWN;
import static fr.mercury.nucleus.input.Input.Modifiers.CONTROL_DOWN;
import static fr.mercury.nucleus.input.Input.Modifiers.SHIFT_DOWN;
import static fr.mercury.nucleus.input.Input.Modifiers.hasModifiers;

import fr.alchemy.utilities.event.EventType;

/**
 * <code>MouseEvent</code> is an implementation of {@link InputEvent} for handling mouse related events which
 * occurred when the mouse is on-screen. It contains data about the mouse cursor position.
 * 
 * @author GnosticOccultist
 */
public class MouseEvent extends InputEvent implements Input {
	
	/**
	 * The event type returned whenever the mouse position has changed.
	 */
	public static final EventType<MouseEvent> MOUSE_MOVED = 
			EventType.create("MOUSE_MOVED", MouseEvent.class);
	/**
	 * The event type returned whenever a mouse button is pressed.
	 */
	public static final EventType<MouseEvent> MOUSE_PRESSED = 
			EventType.create("MOUSE_PRESSED", MouseEvent.class);
	/**
	 * The event type returned whenever a mouse button is released.
	 */
	public static final EventType<MouseEvent> MOUSE_RELEASED = 
			EventType.create("MOUSE_RELEASED", MouseEvent.class);
	/**
	 * The event type returned whenever a mouse button is being clicked 
	 * (pressed and released in a small time interval). 
	 */
	public static final EventType<MouseEvent> MOUSE_CLICKED = 
			EventType.create("MOUSE_CLICKED", MouseEvent.class);
	/**
	 * The event type returned whenever the mouse is scrolled.
	 */
	public static final EventType<MouseEvent> MOUSE_SCROLL = 
			EventType.create("MOUSE_SCROLL", MouseEvent.class);
	
	/**
	 * The mouse button being interacted.
	 */
	private final int button;
	/**
	 * The set of key modifiers interacted.
	 */
	private final int modifiers;
	/**
	 * The current X-axis mouse coordinate.
	 */
	private final int x;
	/**
	 * The current Y-axis mouse coordinate.
	 */
	private final int y;
	/**
	 * The delta X-axis mouse coordinate.
	 */
	private final int dx;
	/**
	 * The delta Y-axis mouse coordinate.
	 */
	private final int dy;
	/**
	 * The delta of the mouse wheel movement.
	 */
	private final int dWheel;
	
	/**
	 * Instantiates a new <code>MouseEvent</code> with the given {@link EventType} and other informations
	 * such as position, button interaction.
	 * <p>
	 * Note that the interaction depends on the event type, for example it will be pressed if the event 
	 * type is {@link #MOUSE_PRESSED}.
	 * 
	 * @param type   The mouse event type.
	 * @param x      The X coordinate of the mouse in screen coordinates.
	 * @param y      The Y coordinate of the mouse in screen coordinates.
	 * @param dx     The change amount of the mouse X-coordinate in screen coordinates.
	 * @param dy     The change amount of the mouse Y-coordinate in screen coordinates.
	 * @param dWheel The change amount in the wheel movement.
	 */
	public MouseEvent(EventType<MouseEvent> type, int x, int y, int dx, int dy, int dWheel) {
		this(type, BUTTON_UNDEFINED, 0, x, y, dx, dy, dWheel);
	}
	
	/**
	 * Instantiates a new <code>MouseEvent</code> with the given {@link EventType} and other informations
	 * such as position, button interaction.
	 * <p>
	 * Note that the interaction depends on the event type, for example it will be pressed if the event 
	 * type is {@link #MOUSE_PRESSED}.
	 * 
	 * @param type   	The mouse event type.
	 * @param button 	The button being pressed, clicked or released.
	 * @param modifiers The set of modifiers key interacted.
	 * @param x  	 	The X coordinate of the mouse in screen coordinates.
	 * @param y  	 	The Y coordinate of the mouse in screen coordinates.
	 * @param dx 	 	The change amount of the mouse X-coordinate in screen coordinates.
	 * @param dy 	 	The change amount of the mouse Y-coordinate in screen coordinates.
	 * @param dWheel 	The change amount in the wheel movement.
	 */
	public MouseEvent(EventType<MouseEvent> type, int button, int modifiers, int x, int y, int dx, int dy, int dWheel) {
		super(type);
		this.button = button;
		this.modifiers = modifiers;
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.dWheel = dWheel;
	}
	
	/**
	 * Return the button being interacted in the <code>MouseEvent</code> or {@link GLFWMouseInput#BUTTON_UNDEFINED}
	 * if none button is currently being interacted.
	 * 
	 * @return The button being interacted or none.
	 */
	public int getButton() {
		return button;
	}
	
	/**
	 * Return the set of modifier keys which were held down during the <code>MouseEvent</code>.
	 * 
	 * @return The set of modifier keys.
	 */
	public int getModifiers() {
		return modifiers;
	}
	
	/**
	 * Whether the shift key was held down during the <code>MouseEvent</code>.
	 * 
	 * @return Whether the shift key was held down.
	 */
	public boolean isShiftDown() {
		return hasModifiers(modifiers, SHIFT_DOWN);
	}
	
	/**
	 * Whether the control key was held down during the <code>MouseEvent</code>.
	 * 
	 * @return Whether the control key was held down.
	 */
	public boolean isControlDown() {
		return hasModifiers(modifiers, CONTROL_DOWN);
	}
	
	/**
	 * Whether the alt key was held down during the <code>MouseEvent</code>.
	 * 
	 * @return Whether the alt key was held down.
	 */
	public boolean isAltDown() {
		return hasModifiers(modifiers, ALT_DOWN);
	}
	
	/**
	 * Return the X-axis current coordinate of the mouse in screen coordinates, 
	 * relative to the upper-left corner of the window.
	 * 
	 * @return The X coordinate of the mouse in screen coordinates.
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Return the Y-axis current coordinate of the mouse in screen coordinates, 
	 * relative to the upper-left corner of the window.
	 * 
	 * @return The Y coordinate of the mouse in screen coordinates.
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Return the change amount on the X-axis based on the last <code>MouseEvent</code> in 
	 * screen coordinates, relative to the upper-left corner of the window.
	 * 
	 * @return The change amount of the mouse X-coordinate in screen coordinates.
	 */
	public int getDX() {
		return dx;
	}
	
	/**
	 * Return the change amount on the Y-axis based on the last <code>MouseEvent</code> in 
	 * screen coordinates, relative to the upper-left corner of the window.
	 * 
	 * @return The change amount of the mouse Y-coordinate in screen coordinates.
	 */
	public int getDY() {
		return dy;
	}
	
	/**
	 * Return the change amount of the wheel movement based on the last <code>MouseEvent</code>.
	 * 
	 * @return The change amount of the wheel movement.
	 */
	public int getDWheel() {
		return dWheel;
	}
	
	@Override
	public String toString() {
		String modStr = ", modifiers= ";
		modStr += isShiftDown() ? "SHIFT " : "";
		modStr += isControlDown() ? "CTRL " : "";
		modStr += isAltDown() ? "ALT" : "";
		return getClass().getSimpleName() + "[ type= " + type + ", button= " + button + modStr + ", x= " + x + ", y= " + y + ", dWheel= " + dWheel + " ]";
	}
}
