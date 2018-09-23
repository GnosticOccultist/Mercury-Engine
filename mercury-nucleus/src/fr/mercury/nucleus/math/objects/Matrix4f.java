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
	public Matrix4f set(Matrix4f other) {
		Validator.nonNull(other, "The matrix cannot be null!");
		
		m00 = other.m00; m01 = other.m01; m02 = other.m02; m03 = other.m03;
		m10 = other.m10; m11 = other.m11; m12 = other.m12; m13 = other.m13;
		m20 = other.m20; m21 = other.m21; m22 = other.m22; m23 = other.m23;
		m30 = other.m30; m31 = other.m31; m32 = other.m32; m33 = other.m33;
		
		return this;
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
	
	/**
	 * Multiplies the <code>Matrix4f</code> with the provided one.
	 * The result is stored inside the second provided matrix or a new one if null.
	 * 
	 * @param other The other matrix to do the multiplication.
	 * @param store The matrix to store the result.
	 * @return		The resulting matrix.
	 */
	public Matrix4f mult(Matrix4f other, Matrix4f store) {
        if (store == null) {
            store = new Matrix4f();
        }
       
        float[] m = new float[16];

        m[0] = m00 * other.m00
                + m01 * other.m10
                + m02 * other.m20
                + m03 * other.m30;
        m[1] = m00 * other.m01
                + m01 * other.m11
                + m02 * other.m21
                + m03 * other.m31;
        m[2] = m00 * other.m02
                + m01 * other.m12
                + m02 * other.m22
                + m03 * other.m32;
        m[3] = m00 * other.m03
                + m01 * other.m13
                + m02 * other.m23
                + m03 * other.m33;

        m[4] = m10 * other.m00
                + m11 * other.m10
                + m12 * other.m20
                + m13 * other.m30;
        m[5] = m10 * other.m01
                + m11 * other.m11
                + m12 * other.m21
                + m13 * other.m31;
        m[6] = m10 * other.m02
                + m11 * other.m12
                + m12 * other.m22
                + m13 * other.m32;
        m[7] = m10 * other.m03
                + m11 * other.m13
                + m12 * other.m23
                + m13 * other.m33;
        m[8] = m20 * other.m00
                + m21 * other.m10
                + m22 * other.m20
                + m23 * other.m30;
        m[9] = m20 * other.m01
                + m21 * other.m11
                + m22 * other.m21
                + m23 * other.m31;
        m[10] = m20 * other.m02
                + m21 * other.m12
                + m22 * other.m22
                + m23 * other.m32;
        m[11] = m20 * other.m03
                + m21 * other.m13
                + m22 * other.m23
                + m23 * other.m33;

        m[12] = m30 * other.m00
                + m31 * other.m10
                + m32 * other.m20
                + m33 * other.m30;
        m[13] = m30 * other.m01
                + m31 * other.m11
                + m32 * other.m21
                + m33 * other.m31;
        m[14] = m30 * other.m02
                + m31 * other.m12
                + m32 * other.m22
                + m33 * other.m32;
        m[15] = m30 * other.m03
                + m31 * other.m13
                + m32 * other.m23
                + m33 * other.m33;

        store.m00 = m[0];
        store.m01 = m[1];
        store.m02 = m[2];
        store.m03 = m[3];
        store.m10 = m[4];
        store.m11 = m[5];
        store.m12 = m[6];
        store.m13 = m[7];
        store.m20 = m[8];
        store.m21 = m[9];
        store.m22 = m[10];
        store.m23 = m[11];
        store.m30 = m[12];
        store.m31 = m[13];
        store.m32 = m[14];
        store.m33 = m[15];
        
        return store;
    }
	
	/**
	 * Set the translation components of the <code>Matrix4f</code> to the ones
	 * specified in the provided <code>Vector3f</code>.
	 * 
	 * @param translation The translation vector to set.
	 */
    public void setTranslation(Vector3f translation) {
        m03 = translation.x;
        m13 = translation.y;
        m23 = translation.z;
    }
    
    /**
     * Set the rotation components of the <code>Matrix4f</code> to the ones
	 * specified in the provided <code>Quaternion</code>.
     * 
     * @param quaternion The rotation quaternion to set.
     */
    public void setRotation(Quaternion quaternion) {
		quaternion.toRotationMatrix(this);
	}
	
    /**
     * Set the scaling components of the <code>Matrix4f</code> to the ones
     * specified in the provided <code>Vector3f</code>.
     * 
     * @param scale The scaling vector to set.
     */
	public void setScale(Vector3f scale) {
        setScale(scale.x, scale.y, scale.z);
	}
	
	 /**
     * Set the scaling components of the <code>Matrix4f</code> to the ones
     * specified.
     * 
     * @param scale The scaling vector to set.
     */
	public void setScale(float x, float y, float z) {
        float length = m00 * m00 + m10 * m10 + m20 * m20;
        if (length != 0f) {
            length = length == 1 ? x : (x / MercuryMath.sqrt(length));
            m00 *= length;
            m10 *= length;
            m20 *= length;
        }

        length = m01 * m01 + m11 * m11 + m21 * m21;
        if (length != 0f) {
            length = length == 1 ? y : (y / MercuryMath.sqrt(length));
            m01 *= length;
            m11 *= length;
            m21 *= length;
        }
        
        length = m02 * m02 + m12 * m12 + m22 * m22;
        if (length != 0f) {
            length = length == 1 ? z : (z / MercuryMath.sqrt(length));
            m02 *= length;
            m12 *= length;
            m22 *= length;
        }
	}
	
	/**
	 * Apply a scale to the <code>Matrix4f</code> using the provided 
	 * <code>Vector3f</code>.
	 * 
	 * @param scale The scale to apply to the matrix.
	 */
    public void scale(Vector3f scale) {
        m00 *= scale.x;
        m10 *= scale.x;
        m20 *= scale.x;
        m30 *= scale.x;
        m01 *= scale.y;
        m11 *= scale.y;
        m21 *= scale.y;
        m31 *= scale.y;
        m02 *= scale.z;
        m12 *= scale.z;
        m22 *= scale.z;
        m32 *= scale.z;
    }
    
    /**
     * Return a scaling <code>Vector3f</code> from the <code>Matrix4f</code>
     * components.
     * 
     * @param store The vector to store the scale into.
     * @return		The scaling vector.
     */
    public Vector3f getScale(Vector3f store) {
		float scaleX = (float) Math.sqrt(m00 * m00 + m10 * m10 + m20 * m20);
		float scaleY = (float) Math.sqrt(m01 * m01 + m11 * m11 + m21 * m21);
		float scaleZ = (float) Math.sqrt(m02 * m02 + m12 * m12 + m22 * m22);
        store.set(scaleX, scaleY, scaleZ);
        return store;
    }
}
