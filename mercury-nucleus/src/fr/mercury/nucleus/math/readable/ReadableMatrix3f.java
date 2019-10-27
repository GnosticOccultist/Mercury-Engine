package fr.mercury.nucleus.math.readable;

import fr.mercury.nucleus.math.objects.Matrix3f;

/**
 * <code>ReadableMatrix3f</code> is an interface to implement a readable-only 3x3 matrix of single-precision floats, 
 * meaning its fields can be accessed but not modified.
 * The actual implementation of this interface is the {@link Matrix3f} class.
 * 
 * @author GnosticOccultist
 */
public interface ReadableMatrix3f {
	
	/**
	 * Return the row 0 - column 0 component of the <code>ReadableMatrix3f</code>.
	 * 
	 * @return The row 0 - column 0 component.
	 */
	float m00();
	
	/**
	 * Return the row 0 - column 1 component of the <code>ReadableMatrix3f</code>.
	 * 
	 * @return The row 0 - column 1 component.
	 */
	float m01();
	
	/**
	 * Return the row 0 - column 2 component of the <code>ReadableMatrix3f</code>.
	 * 
	 * @return The row 0 - column 2 component.
	 */
	float m02();
	
	/**
	 * Return the row 1 - column 0 component of the <code>ReadableMatrix3f</code>.
	 * 
	 * @return The row 1 - column 0 component.
	 */
	float m10();
	
	/**
	 * Return the row 1 - column 1 component of the <code>ReadableMatrix3f</code>.
	 * 
	 * @return The row 1 - column 1 component.
	 */
	float m11();
	
	/**
	 * Return the row 1 - column 2 component of the <code>ReadableMatrix3f</code>.
	 * 
	 * @return The row 1 - column 2 component.
	 */
	float m12();
	
	/**
	 * Return the row 2 - column 0 component of the <code>ReadableMatrix3f</code>.
	 * 
	 * @return The row 2 - column 0 component.
	 */
	float m20();
	
	/**
	 * Return the row 2 - column 1 component of the <code>ReadableMatrix3f</code>.
	 * 
	 * @return The row 2 - column 1 component.
	 */
	float m21();
	
	/**
	 * Return the row 2 - column 2 component of the <code>ReadableMatrix3f</code>.
	 * 
	 * @return The row 2 - column 2 component.
	 */
	float m22();
	
	/**
	 * Return whether the <code>ReadableMatrix3f</code> is orthonormal.
	 * 
	 * @return Whether the matrix is orthonormal.
	 */
	boolean isOrthonormal();
	
	/**
	 * Return the determinant of the <code>ReadableMatrix3f</code>.
	 * 
	 * @return The determinant of the matrix.
	 */
	default float determinant() {
		return m00() * m11() * m22() + m01() * m12() * m20() + m02() * m10() * m21() - 
				m02() * m11() * m20() - m01() * m10() * m22() - m00() * m12() * m21();
	}
	
	/**
	 * Return whether the <code>ReadableMatrix3f</code> is an identity one.
	 * 
	 * @return Whether the matrix is an identity one.
	 */
	default boolean isIdentity() {
		if(m00() == 1.0F && m11() == 1.0F && m22() == 1.0F) {
			if(m01() == 0.0F && m02() == 0.0F && m10() == 0.0F && 
					m12() == 0.0F && m20() == 0.0F && m21() == 0.0F) {
				return true;
			}
		}
		return false;
	}
}
