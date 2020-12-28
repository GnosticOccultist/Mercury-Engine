package fr.mercury.nucleus.math.readable;

import fr.mercury.nucleus.math.objects.FloatBufferPopulator;
import fr.mercury.nucleus.math.objects.Matrix4f;

/**
 * <code>ReadableMatrix4f</code> is an interface to implement a readable-only 4x4 matrix of single-precision floats, 
 * meaning its fields can be accessed but not modified.
 * The actual implementation of this interface is the {@link Matrix4f} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableMatrix4f extends FloatBufferPopulator {

	/**
	 * Return the row 0 - column 0 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 0 - column 0 component.
	 */
	float m00();
	
	/**
	 * Return the row 0 - column 1 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 0 - column 1 component.
	 */
	float m01();
	
	/**
	 * Return the row 0 - column 2 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 0 - column 2 component.
	 */
	float m02();
	
	/**
	 * Return the row 0 - column 3 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 0 - column 3 component.
	 */
	float m03();
	
	/**
	 * Return the row 1 - column 0 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 1 - column 0 component.
	 */
	float m10();
	
	/**
	 * Return the row 1 - column 1 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 1 - column 1 component.
	 */
	float m11();
	
	/**
	 * Return the row 1 - column 2 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 1 - column 2 component.
	 */
	float m12();
	
	/**
	 * Return the row 1 - column 3 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 1 - column 3 component.
	 */
	float m13();
	
	/**
	 * Return the row 2 - column 0 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 2 - column 0 component.
	 */
	float m20();
	
	/**
	 * Return the row 2 - column 1 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 2 - column 1 component.
	 */
	float m21();
	
	/**
	 * Return the row 2 - column 2 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 2 - column 2 component.
	 */
	float m22();
	
	/**
	 * Return the row 2 - column 3 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 2 - column 3 component.
	 */
	float m23();
	
	/**
	 * Return the row 3 - column 0 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 3 - column 0 component.
	 */
	float m30();
	
	/**
	 * Return the row 3 - column 1 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 3 - column 1 component.
	 */
	float m31();
	
	/**
	 * Return the row 3 - column 2 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 3 - column 2 component.
	 */
	float m32();
	
	/**
	 * Return the row 3 - column 3 component of the <code>ReadableMatrix4f</code>.
	 * 
	 * @return The row 3 - column 3 component.
	 */
	float m33();
}
