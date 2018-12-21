package fr.mercury.nucleus.math.readable;

import fr.mercury.nucleus.math.objects.Vector3f;

/**
 * <code>ReadableTransform</code> is an interface to implement a readable-only
 * 3-dimensional vector of single-precision floats, meaning its fields can be accessed but not modified.
 * The actual implementation of this interface is the {@link Vector3f} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableVector3f {
	
	/**
	 * Return the X-component of the <code>ReadableVector3f</code>,
	 * as a single-precision float.
	 * 
	 * @return The X-coordinate value of the vector.
	 */
	float x();
	
	/**
	 * Return the Y-component of the <code>ReadableVector3f</code>,
	 * as a single-precision float.
	 * 
	 * @return The Y-coordinate value of the vector.
	 */
	float y();
	
	/**
	 * Return the Z-component of the <code>ReadableVector3f</code>,
	 * as a single-precision float.
	 * 
	 * @return The Z-coordinate value of the vector.
	 */
	float z();
}
