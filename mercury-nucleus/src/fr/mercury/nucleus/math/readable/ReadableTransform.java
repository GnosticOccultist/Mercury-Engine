package fr.mercury.nucleus.math.readable;

import fr.mercury.nucleus.math.objects.FloatBufferPopulator;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Transform;

/**
 * <code>ReadableTransform</code> is an interface to implement a readable-only transform with a translation, rotation and scale,
 * meaning its fields can be accessed but not modified. 
 * The actual implementation of this interface is the {@link Transform} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableTransform extends FloatBufferPopulator {

    /**
     * Return the translation {@link ReadableVector3f} of the
     * <code>ReadableTransform</code>
     * 
     * @return The readable-only translation vector.
     */
    ReadableVector3f getTranslation();

    /**
     * Return the rotation {@link ReadableMatrix3f} of the
     * <code>ReadableTransform</code>
     * 
     * @return The readable-only rotation matrix.
     */
    ReadableMatrix3f getRotation();

    /**
     * Return the scaling {@link ReadableVector3f} of the
     * <code>ReadableTransform</code>
     * 
     * @return The readable-only scaling vector.
     */
    ReadableVector3f getScale();

    /**
     * Return the transformation matrix of the <code>ReadableTransform</code>,
     * usable by the <code>OpenGL</code> context.
     * 
     * @param The matrix to store the transform.
     * 
     * @return The transformation matrix.
     */
    Matrix4f asModelMatrix(Matrix4f store);

    /**
     * Return whether all 3 components (translation, rotation and scale) of the
     * <code>ReadableTransform</code> are equal to an identity matrix, meaning this
     * transform is an identity-transform.
     * 
     * @return Whether the transform is an identity-transform.
     */
    default boolean isIdentity() {
        return getTranslation().isZero() && getRotation().isIdentity() && getScale().isIdentity();
    }

    /**
     * Return whether the matrix of the <code>ReadableTransform</code> is only
     * representing a rotation or combines a scale as well.
     * 
     * @return Whether the transform's matrix represent only a rotation.
     */
    boolean isRotationMatrix();

    /**
     * Return whether all the scale component of the <code>ReadableTransform</code>
     * is a uniform vector, meaning all 3 of its components are equal.
     * 
     * @return Whether the transform's scaling vector is uniform.
     */
    default boolean isUniformScale() {
        return getScale().isUniform();
    }
}
