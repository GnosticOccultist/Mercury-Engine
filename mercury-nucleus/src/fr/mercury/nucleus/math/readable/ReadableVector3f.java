package fr.mercury.nucleus.math.readable;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Vector3f;

/**
 * <code>ReadableVector3f</code> is an interface to implement a readable-only 3-dimensional vector of single-precision floats, 
 * meaning its fields can be accessed but not modified. 
 * The actual implementation of this interface is the {@link Vector3f} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableVector3f {

    /**
     * Return the X-component of the <code>ReadableVector3f</code>, as a
     * single-precision float.
     * 
     * @return The X-coordinate value of the vector.
     */
    float x();

    /**
     * Return the Y-component of the <code>ReadableVector3f</code>, as a
     * single-precision float.
     * 
     * @return The Y-coordinate value of the vector.
     */
    float y();

    /**
     * Return the Z-component of the <code>ReadableVector3f</code>, as a
     * single-precision float.
     * 
     * @return The Z-coordinate value of the vector.
     */
    float z();

    /**
     * Return the length of the <code>ReadableVector3f</code>.
     * 
     * @return The length of the vector.
     */
    default float length() {
        return (float) Math.sqrt(x() * x() + y() * y() + z() * z());
    }

    /**
     * Return whether the <code>ReadableVector3f</code> is a unit vector, meaning
     * its norm ({@link #length()}) is equal to 1.
     * 
     * @return Whether the vector is a unit vector.
     */
    default boolean isUnitVector() {
        var length = length();
        // Add a tolerance range, otherwise it will never pass.
        return length > 0.99F && length < 1.01F;
    }

    /**
     * Calculates the dot product of the <code>ReadableVector3f</code> with the
     * provided one. If dot product = 0, the vectors are orthogonal.
     * 
     * @param other The vector to get the dot product with (not null).
     * @return The resulting scalar from the dot product.
     */
    default float dot(ReadableVector3f other) {
        Validator.nonNull(other, "The vector cannot be null!");

        return x() * other.x() + y() * other.y() + z() * other.z();
    }

    /**
     * Calculates the squared distance between the <code>ReadableVector3f</code> and
     * the provided one.
     *
     * @param other The vector to determine the distance squared from (not null).
     * @return      The distance squared between the two vectors.
     */
    default float distanceSquared(ReadableVector3f other) {
        Validator.nonNull(other, "The vector cannot be null!");

        double dx = x() - other.x();
        double dy = y() - other.y();
        double dz = z() - other.z();
        return (float) (dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculates the distance between the <code>ReadableVector3f</code> and the
     * provided one.
     *
     * @param other The vector to determine the distance from (not null).
     * @return      The distance between the two vectors.
     */
    default double distance(ReadableVector3f other) {
        Validator.nonNull(other, "The vector cannot be null!");

        return Math.sqrt(distanceSquared(other));
    }

    /**
     * Return whether all 3 single-precision components of the
     * <code>ReadableVector3f</code> are equal, meaning this vector is uniformized.
     * 
     * @return Whether the vector is uniformized.
     */
    default boolean isUniform() {
        return x() == y() && y() == z();
    }

    /**
     * Return whether all 3 single-precision components of the
     * <code>ReadableVector3f</code> are equal to zero, meaning this vector is a
     * zero-vector.
     * 
     * @return Whether the vector is a zero-vector.
     */
    default boolean isZero() {
        return x() == 0 && y() == 0 && z() == 0;
    }

    /**
     * Return whether all 3 single-precision components of the
     * <code>ReadableVector3f</code> are equal to one, meaning this vector is an
     * identity-vector.
     * 
     * @return Whether the vector is an identity-vector (used for scaling).
     */
    default boolean isIdentity() {
        return x() == 1 && y() == 1 && z() == 1;
    }
}
