package fr.mercury.nucleus.math.readable;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Vector4f;

/**
 * <code>ReadableVector4f</code> is an interface to implement a readable-only 4-dimensional vector of single-precision floats, 
 * meaning its fields can be accessed but not modified.
 * The actual implementation of this interface is the {@link Vector4f} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableVector4f {

	/**
	 * Return the X-component of the <code>ReadableVector4f</code>,
	 * as a single-precision float.
	 * 
	 * @return The X-coordinate value of the vector.
	 */
	float x();
	
	/**
	 * Return the Y-component of the <code>ReadableVector4f</code>,
	 * as a single-precision float.
	 * 
	 * @return The Y-coordinate value of the vector.
	 */
	float y();
	
	/**
	 * Return the Z-component of the <code>ReadableVector4f</code>,
	 * as a single-precision float.
	 * 
	 * @return The Z-coordinate value of the vector.
	 */
	float z();
	
	/**
	 * Return the W-component of the <code>ReadableVector4f</code>,
	 * as a single-precision float.
	 * 
	 * @return The W-coordinate value of the vector.
	 */
	float w();
	
	/**
	 * Return the length of the <code>ReadableVector4f</code>.
	 * 
	 * @return The length of the vector.
	 */
	default float length() {
		return (float) Math.sqrt(x() * x() + y() * y() + z() * z() + w() * w());
	}
	
	/**
	 * Return whether the <code>ReadableVector4f</code> is a unit vector, 
	 * meaning its norm ({@link #length()}) is equal to 1.
	 * 
	 * @return Whether the vector is a unit vector.
	 */
	default boolean isUnitVector() {
		return length() == 1;
	}
	
	/**
     * Calculates the dot product of the <code>ReadableVector4f</code> with the provided one.
     * If dot product = 0, the vectors are orthogonal.
     * 
     * @param other The vector to get the dot product with (not null).
     * @return		The resulting scalar from the dot product.
     */
	default float dot(ReadableVector4f other) {
    	Validator.nonNull(other, "The vector cannot be null!");
    	
    	return x() * other.x() + y() * other.y() + z() * other.z() + w() * other.w();
    }
	
	/**
     * Calculates the squared distance between the <code>ReadableVector4f</code> and 
     * the provided one.
     *
     * @param other The vector to determine the distance squared from (not null).
     * @return 		The distance squared between the two vectors.
     */
	default float distanceSquared(ReadableVector4f other) {
		Validator.nonNull(other, "The vector cannot be null!");
		
        double dx = x() - other.x();
        double dy = y() - other.y();
        double dz = z() - other.z();
        double dw = w() - other.w();
        return (float) (dx * dx + dy * dy + dz * dz + dw * dw);
    }
    
    /**
     * Calculates the distance between the <code>ReadableVector4f</code> and 
     * the provided one.
     *
     * @param other The vector to determine the distance from (not null).
     * @return 		The distance between the two vectors.
     */
    default double distance(ReadableVector4f other) {
    	Validator.nonNull(other, "The vector cannot be null!");
    	
        return Math.sqrt(distanceSquared(other));
    }
	
	/**
	 * Return whether all 4 single-precision components of the <code>ReadableVector4f</code> 
	 * are equal, meaning this vector is uniformized.
	 * 
	 * @return Whether the vector is uniformized.
	 */
	default boolean isUniform() {
		return x() == y() && y() == z() && z() == w();
	}
	
	/**
	 * Return whether all 4 single-precision components of the <code>ReadableVector4f</code> 
	 * are equal to zero, meaning this vector is a zero-vector.
	 * 
	 * @return Whether the vector is a zero-vector.
	 */
	default boolean isZero() {
		return x() == 0 && y() == 0 && z() == 0 && w() == 0;
	}
	
	/**
	 * Return whether all 4 single-precision components of the <code>ReadableVector4f</code> 
	 * are equal to one, meaning this vector is an identity-vector.
	 * 
	 * @return Whether the vector is an identity-vector (used for scaling).
	 */
	default boolean isIdentity() {
		return x() == 1 && y() == 1 && z() == 1 && w() == 1;
	}
}
