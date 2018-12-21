package fr.mercury.nucleus.math.readable;

import fr.mercury.nucleus.math.objects.Quaternion;

/**
 * <code>ReadableQuaternion</code> is an interface to implement a readable-only
 * quaternion of single-precision floats, meaning its fields can be accessed but not modified.
 * The actual implementation of this interface is the {@link Quaternion} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableQuaternion {
	
	/**
	 * Return the X-component of the <code>ReadableQuaternion</code>,
	 * as a single-precision float.
	 * 
	 * @return The X-coordinate value of the quaternion.
	 */
	float x();
	
	/**
	 * Return the Y-component of the <code>ReadableQuaternion</code>,
	 * as a single-precision float.
	 * 
	 * @return The Y-coordinate value of the quaternion.
	 */
	float y();
	
	/**
	 * Return the Z-component of the <code>ReadableQuaternion</code>,
	 * as a single-precision float.
	 * 
	 * @return The Z-coordinate value of the quaternion.
	 */
	float z();
	
	/**
	 * Return the W-component of the <code>ReadableQuaternion</code>,
	 * as a single-precision float.
	 * 
	 * @return The W-coordinate value of the quaternion.
	 */
	float w();
}
