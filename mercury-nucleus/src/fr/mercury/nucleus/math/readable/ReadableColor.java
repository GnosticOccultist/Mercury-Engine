package fr.mercury.nucleus.math.readable;

import fr.mercury.nucleus.math.objects.Color;

/**
 * <code>ReadableColor</code> is an interface to implement a readable-only RGBA color composed of single-precision floats, 
 * meaning its fields can be accessed but not modified.
 * The actual implementation of this interface is the {@link Color} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableColor {

	/**
	 * Return the red component of the <code>ReadableColor</code>,
	 * as a single-precision float.
	 * 
	 * @return The red component value of the color.
	 */
	float r();
	
	/**
	 * Return the green component of the <code>ReadableColor</code>,
	 * as a single-precision float.
	 * 
	 * @return The green component value of the color.
	 */
	float g();
	
	/**
	 * Return the blue component of the <code>ReadableColor</code>,
	 * as a single-precision float.
	 * 
	 * @return The blue component value of the color.
	 */
	float b();
	
	/**
	 * Return the alpha component of the <code>ReadableColor</code>,
	 * as a single-precision float.
	 * 
	 * @return The alpha component value of the color.
	 */
	float a();
}
