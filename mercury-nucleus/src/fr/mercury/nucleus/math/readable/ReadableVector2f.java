package fr.mercury.nucleus.math.readable;

import fr.mercury.nucleus.math.objects.Vector2f;

/**
 * <code>ReadableVector2f</code> is an interface to implement a readable-only
 * 2-dimensional vector of single-precision floats, meaning its fields can be accessed but not modified.
 * The actual implementation of this interface is the {@link Vector2f} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableVector2f {
	
	/**
	 * Return the X-component of the <code>ReadableVector2f</code>,
	 * as a single-precision float.
	 * 
	 * @return The X-coordinate value of the vector.
	 */
	float x();
	
	/**
	 * Return the Y-component of the <code>ReadableVector2f</code>,
	 * as a single-precision float.
	 * 
	 * @return The Y-coordinate value of the vector.
	 */
	float y();
	
	/**
	 * Return whether both single-precision components of the <code>ReadableVector2f</code> 
	 * are equal, meaning this vector is uniformized.
	 * 
	 * @return Whether the vector is uniformized.
	 */
	default boolean isUniform() {
		return x() == y();
	}
	
	/**
	 * Return whether both single-precision components of the <code>ReadableVector2f</code> 
	 * are equal to zero, meaning this vector is a zero-vector.
	 * 
	 * @return Whether the vector is a zero-vector.
	 */
	default boolean isZero() {
		return x() == 0 && y() == 0;
	}
	
	/**
	 * Return whether both single-precision components of the <code>ReadableVector2f</code> 
	 * are equal to one, meaning this vector is an identity-vector.
	 * 
	 * @return Whether the vector is an identity-vector (used for scaling).
	 */
	default boolean isIdentity() {
		return x() == 1 && y() == 1;
	}

	/**
     * Calculates the distance between the <code>ReadableVector2f</code> and 
     * the provided one.
     *
     * @param other The vector to determine the distance from.
     * @return 		The distance between the two vectors.
     */
	double distance(ReadableVector2f translation);
}
