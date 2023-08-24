package fr.mercury.nucleus.math.readable;

import fr.mercury.nucleus.math.objects.Matrix3f;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Quaternion;
import fr.mercury.nucleus.math.objects.Vector3f;

/**
 * <code>ReadableQuaternion</code> is an interface to implement a readable-only quaternion of single-precision floats, 
 * meaning its fields can be accessed but not modified. 
 * The actual implementation of this interface is the {@link Quaternion} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableQuaternion {

    /**
     * Retrieve the given column of the rotation matrix represented by the
     * <code>ReadableQuaternion</code>. The index must be between 0 and 2 included:
     * <li>0: the left-axis of the rotation.</li>
     * <li>1: the up-axis of the rotation.</li>
     * <li>2: the direction which is facing the rotation.</li>
     * <p>
     * 
     * @param index The index of the column to retrieve (&ge;0, &le;2).
     * @param store The vector to store the result.
     * @return The rotation column either the store or a new vector instance.
     */
    Vector3f getRotationColumn(int index, Vector3f store);

    /**
     * Converts the <code>ReadableQuaternion</code> to a rotation {@link Matrix3f},
     * stored into the provided matrix.
     * 
     * @param result The matrix to store the result.
     * @return A rotation matrix either the store or a new matrix instance.
     * 
     * @see #toRotationMatrix(Matrix4f)
     */
    Matrix4f toRotationMatrix(Matrix4f store);

    /**
     * Converts the <code>ReadableQuaternion</code> to a rotation {@link Matrix4f},
     * stored into the provided matrix.
     * 
     * @param result The matrix to store the result.
     * @return A rotation matrix either the store or a new matrix instance.
     * 
     * @see #toRotationMatrix(Matrix3f)
     */
    Matrix3f toRotationMatrix(Matrix3f store);

    /**
     * Return the X-component of the <code>ReadableQuaternion</code>, as a
     * single-precision float.
     * 
     * @return The X-coordinate value of the quaternion.
     */
    float x();

    /**
     * Return the Y-component of the <code>ReadableQuaternion</code>, as a
     * single-precision float.
     * 
     * @return The Y-coordinate value of the quaternion.
     */
    float y();

    /**
     * Return the Z-component of the <code>ReadableQuaternion</code>, as a
     * single-precision float.
     * 
     * @return The Z-coordinate value of the quaternion.
     */
    float z();

    /**
     * Return the W-component of the <code>ReadableQuaternion</code>, as a
     * single-precision float.
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
     * Return whether all 4 single-precision components of the
     * <code>ReadableQuaternion</code> are equal to zero, meaning this vector is a
     * zero-vector.
     * 
     * @return Whether the vector is a zero-vector.
     */
    default boolean isZero() {
        return x() == 0 && y() == 0 && z() == 0 && w() == 0;
    }

    /**
     * Return whether all first 3 single-precision components of the
     * <code>ReadableQuaternion</code> are equal to zero, and the last one equal to
     * one, meaning this quaternion is an identity-quaternion.
     * 
     * @return Whether the vector is an identity-quaternion (used for rotation).
     */
    default boolean isIdentity() {
        return x() == 0 && y() == 0 && z() == 0 && w() == 1;
    }
}
