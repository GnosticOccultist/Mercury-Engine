package fr.mercury.nucleus.math.readable;

import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Transform;

/**
 * <code>ReadableTransform</code> is an interface to implement a readable-only
 * transform, meaning its fields can be accessed but not modified.
 * The actual implementation of this interface is the {@link Transform} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableTransform {
	
	/**
	 * Return the translation {@link ReadableVector3f} of the <code>ReadableTransform</code>
	 * 
	 * @return The readable-only translation vector.
	 */
	ReadableVector3f getTranslation();

	/**
	 * Return the rotation {@link ReadableQuaternion} of the <code>ReadableTransform</code>
	 * 
	 * @return The readable-only rotation quaternion.
	 */
	ReadableQuaternion getRotation();

	/**
	 * Return the scaling {@link ReadableVector3f} of the <code>ReadableTransform</code>
	 * 
	 * @return The readable-only scaling vector.
	 */
	ReadableVector3f getScale();
	
	/**
	 * Return the transformation matrix of the <code>ReadableTransform</code>, usable by
	 * the <code>OpenGL</code> context.
	 * 
	 * @param The matrix to store the transform.
	 * 
	 * @return The transformation matrix.
	 */
	Matrix4f asModelMatrix(Matrix4f store);
}
