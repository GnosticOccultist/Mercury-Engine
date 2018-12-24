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
	
	/**
	 * Return whether all 4 single-precision components of the <code>ReadableQuaternion</code> 
	 * are equal, meaning this vector is uniformized.
	 * 
	 * @return Whether the quaternion is uniformized.
	 */
	default boolean isUniform() {
		return x() == y() && y() == z() && z() == w();
	}
	
	/**
	 * Return whether all 4 single-precision components of the <code>ReadableQuaternion</code> 
	 * are equal to zero, meaning this vector is a zero-vector.
	 * 
	 * @return Whether the vector is a zero-vector.
	 */
	default boolean isZero() {
		return x() == 0 && y() == 0 && z() == 0 && w() == 0;
	}
	
	/**
	 * Return whether all first 3 single-precision components of the <code>ReadableQuaternion</code> 
	 * are equal to zero, and the last one equal to one, meaning this quaternion is an identity-quaternion.
	 * 
	 * @return Whether the vector is an identity-quaternion (used for rotation).
	 */
	default boolean isIdentity() {
		return x() == 0 && y() == 0 && z() == 0 && w() == 1;
	}
}
