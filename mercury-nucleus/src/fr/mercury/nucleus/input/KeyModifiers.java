package fr.mercury.nucleus.input;

/**
 * <code>KeyModifiers</code> is an interface for providing access to the key modifiers bit mask, 
 * based on <code>GLFW</code>.
 * 
 * @author GnosticOccultist
 */
public interface KeyModifiers {

	/**
	 * The bit to set when the shift key is down.
	 */
	int SHIFT_DOWN = 0x1;
	/**
	 * The bit to set when the CTRL key is down.
	 */
	int CONTROL_DOWN = 0x2;
	/**
	 * The bit to set when the ALT key is down.
	 */
	int ALT_DOWN = 0x4;
	
	/**
	 * Return whether the provided int value contains the specified bit mask
	 * modifier.
	 * 
	 * @param modifiers The set of modifiers.
	 * @param mask		The bit mask of the modifier to check presence of.
	 * @return			Whether the modifier is present in the set.
	 */
	public static boolean hasModifiers(int modifiers, int mask) {
		return (modifiers & mask) == mask;
	}
}
