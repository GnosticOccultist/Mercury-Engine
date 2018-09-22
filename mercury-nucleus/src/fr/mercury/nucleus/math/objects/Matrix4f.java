package fr.mercury.nucleus.math.objects;

import java.nio.FloatBuffer;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.MercuryMath;

/**
 * <code>Matrix4f</code> defines a 4x4 matrix, which is mainly used to store
 * translation or rotational informations. The matrix can therefore be create 
 * from multiple ways and transformed into multiple mathematical object or buffer.
 * The storage pattern is the following:
 * <pre> 
 * For example m13 is the zeroth row and third column corresponding to 
 * the "y" translation vector part. The storage is then in column major.
 * </pre>
 * 
 * @author GnosticOccultist
 */
public final class Matrix4f {
	
    public float m00, m01, m02, m03;
    public float m10, m11, m12, m13;
    public float m20, m21, m22, m23;
    public float m30, m31, m32, m33;
    
    /**
     * Instantiates a new <code>Matrix4f</code> with the identity 
     * values ({@link #identity()}).
     */
    public Matrix4f() {
		identity();
	}
    
    /**
	 * Instantiates a new <code>Matrix4f</code> with the provided
	 * matrixes components.
	 * 
	 * @param other The other matrix to get the components.
	 */
    public Matrix4f(Matrix4f other) {
		set(other);
	}

    /**
	 * Set the components values of the provided matrix to this 
	 * <code>Matrix4f</code> components.
	 * <p>
	 * The provided matrix cannot be null.
	 * 
	 * @param other The other matrix to copy from.
	 */
	public void set(Matrix4f other) {
		Validator.nonNull(other, "The matrix cannot be null!");
		
		m00 = other.m00; m01 = other.m01; m02 = other.m02; m03 = other.m03;
		m10 = other.m10; m11 = other.m11; m12 = other.m12; m13 = other.m13;
		m20 = other.m20; m21 = other.m21; m22 = other.m22; m23 = other.m23;
		m30 = other.m30; m31 = other.m31; m32 = other.m32; m33 = other.m33;
	}

	/**
     * Set the <code>Matrix4f</code> to its identity values.
     * It is all zeros except the diagonal values which are set to 1.
     * <pre>
     * | 1 | 0 | 0 | 0 |
     * | 0 | 1 | 0 | 0 |
     * | 0 | 0 | 1 | 0 |
     * | 0 | 0 | 0 | 1 |
     * </pre>
     */
    public void identity() {
        m01 = m02 = m03 = 0.0f;
        m10 = m12 = m13 = 0.0f;
        m20 = m21 = m23 = 0.0f;
        m30 = m31 = m32 = 0.0f;
        m00 = m11 = m22 = m33 = 1.0f;
	}

    /**
     * Build the view matrix with the <code>Matrix4f</code> with the location, direction,
     * up-axis and left-axis of the <code>Camera</code>.
     * <p>
     * It is used in the shader to adjust the <code>Transform</code> of an object depending
     * on the <code>Camera</code>.
     * 
     * @param location	The location of the camera.
     * @param direction The direction the camera is facing.
     * @param up		The up-axis of the camera.
     * @param left      The left-axis of the camera.
     */
	public void viewMatrix(Vector3f location, Vector3f direction, Vector3f up, Vector3f left) {
    	Vector3f vec1 = MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class).set(direction);
    	Vector3f vec2 = MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class).set(vec1).cross(up);
    	Vector3f vec3 = MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class).set(vec2).cross(vec1);
    	
    	m00 = vec2.x;
    	m01 = vec2.y;
    	m02 = vec2.z;
    	m03 = -vec2.dot(location);
    	
    	m10 = vec3.x;
    	m11 = vec3.y;
    	m12 = vec3.z;
    	m13 = -vec3.dot(location);
    	
    	m20 = -vec1.x;
    	m21 = -vec1.y;
    	m22 = -vec1.z;
    	m23 = vec1.dot(location);
    	
    	m30 = 0f;
    	m31 = 0f;
    	m32 = 0f;
    	m33 = 0f;
    }

	/**
	 * Build the projection matrix with the <code>Matrix4f</code> with each frustum plane
	 * distance from the <code>Camera</code>. 
	 * <p>
	 * It is used in the shader to adjust an object depending on the window size.
	 * 
	 * @param near   The near frustum plane distance from the camera.
	 * @param far    The far frustum plane distance from the camera.
	 * @param left   The left frustum plane distance from the camera.
	 * @param right  The right frustum plane distance from the camera.
	 * @param top    The top frustum plane distance from the camera.
	 * @param bottom The bottom frustum plane distance from the camera.
	 */
	public void projection(float near, float far, float left, float right, float top, float bottom) {
		identity();
		m00 = (2.0f * near) / (right - left);
		m11 = (2.0f * near) / (top - bottom);
		m32 = -1.0f;
		m33 = -0.0f;
		
		m02 = (right + left) / (right - left);
		m12 = (top + bottom) / (top - bottom);
		m22 = -(far + near) / (far - near);
		m23 = -(2.0f * far * near) / (far - near);
	}

	/**
	 * Fill the provided <code>FloatBuffer</code> with the <code>Matrix4f</code> values.
	 * It can be ordered in column major order or row major ordered.
	 * 
	 * @param fb		  The float buffer to fill with the matrix (16 float space left).
	 * @param columnMajor Whether to fill in column major order or in row major order.
	 * @return			  The filled float buffer.
	 */
	public FloatBuffer fillFloatBuffer(FloatBuffer fb, boolean columnMajor) {
		float[] matrix = new float[16];
		fillFloatArray(matrix, columnMajor);
		fb.put(matrix, 0, 16);
		
		return fb;
	}

	/**
	 * Fill the float array with the <code>Matrix4f</code> values.
	 * It can be ordered in column major order or row major ordered.
	 * 
	 * @param array		  The float array to fill with the matrix (16 float space left).
	 * @param columnMajor Whether to fill in column major order or in row major order.
	 */
	public void fillFloatArray(float[] array, boolean columnMajor) {
        if (columnMajor) {
            array[ 0] = m00;
            array[ 1] = m10;
            array[ 2] = m20;
            array[ 3] = m30;
            array[ 4] = m01;
            array[ 5] = m11;
            array[ 6] = m21;
            array[ 7] = m31;
            array[ 8] = m02;
            array[ 9] = m12;
            array[10] = m22;
            array[11] = m32;
            array[12] = m03;
            array[13] = m13;
            array[14] = m23;
            array[15] = m33;
        } else {
            array[ 0] = m00;
            array[ 1] = m01;
            array[ 2] = m02;
            array[ 3] = m03;
            array[ 4] = m10;
            array[ 5] = m11;
            array[ 6] = m12;
            array[ 7] = m13;
            array[ 8] = m20;
            array[ 9] = m21;
            array[10] = m22;
            array[11] = m23;
            array[12] = m30;
            array[13] = m31;
            array[14] = m32;
            array[15] = m33;
        }
	}
}
