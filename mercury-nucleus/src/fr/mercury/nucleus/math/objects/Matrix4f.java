package fr.mercury.nucleus.math.objects;

import java.nio.FloatBuffer;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.pool.Reusable;
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
public final class Matrix4f implements Reusable {
	
	/**
	 * The <code>Matrix4f</code> identity &rarr; {@link #identity()}
	 */
	public static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
	
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
     * Build the view <code>Matrix4f</code> with the provided location and rotation
     * of the <code>Camera</code>. 
     * <pre>X component &rarr; roll.<p>Y component &rarr; pitch.<p>Z component &rarr; yaw.</pre>
     * <p>
     * It is used in the shader to adjust the <code>Transform</code> of an object depending
     * on the <code>Camera</code> position or rotation.
     * 
     * @param location The location of the camera.
     * @param rotation The rotation of the camera.
     * 
     * @return The builded view matrix.
     */
	public Matrix4f view(Vector3f location, Quaternion rotation) {
		
        float sinX = (float) Math.sin(rotation.x);
        float cosX = (float) Math.cos(rotation.x);
        float sinY = (float) Math.sin(rotation.y);
        float cosY = (float) Math.cos(rotation.y);
        float sinZ = (float) Math.sin(rotation.z);
        float cosZ = (float) Math.cos(rotation.z);
        
        float negSinX = -sinX;
        float negSinY = -sinY;
        float negSinZ = -sinZ;

        // Rotate on the X-axis.
        float nm11 = cosX;
        float nm12 = sinX;
        float nm21 = negSinX;
        float nm22 = cosX;
        
        // Rotate on the Y-axis.
        float nm00 = cosY;
        float nm01 = nm21 * negSinY;
        float nm02 = nm22 * negSinY;
        
        m20 = sinY;
        m21 = nm21 * cosY;
        m22 = nm22 * cosY;
        
        // Rotate on the Z-axis.
        m00 = nm00 * cosZ;
        m01 = nm01 * cosZ + nm11 * sinZ;
        m02 = nm02 * cosZ + nm12 * sinZ;
        m10 = nm00 * negSinZ;
        m11 = nm01 * negSinZ + nm11 * cosZ;
        m12 = nm02 * negSinZ + nm12 * cosZ;
        
        // Set last column to identity.
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        
        setTranslation(-location.x, -location.y, -location.z);
        return this;
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
	 * Fill the provided {@link FloatBuffer} with the <code>Matrix4f</code> values.
	 * It can be ordered in column major order or row major ordered.
	 * 
	 * @param fb		  The float buffer to fill with the matrix (space left &ge;16).
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
	 * @param array		  The float array to fill with the matrix (length left &ge;16).
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
	 * Reads value from the provided {@link FloatBuffer} and store them
	 * into the <code>Matrix4f</code> as row major order.
	 * 
	 * @param fb The float buffer to read data from (size &ge;16).
	 * @return	 The filled matrix with the data of the float buffer.
	 */
	public Matrix4f readFloatBuffer(FloatBuffer fb) {
		return readFloatBuffer(fb, false);
	}
	
	/**
	 * Reads value from the provided {@link FloatBuffer} and store them
	 * into the <code>Matrix4f</code>.
	 * 
	 * @param fb		  The float buffer to read data from (size &ge;16).
	 * @param columnMajor Whether to fill the matrix with column major data or row major data.
	 * @return			  The filled matrix with the data of the float buffer.
	 */
	public Matrix4f readFloatBuffer(FloatBuffer fb, boolean columnMajor) {
		Validator.nonNull(fb);
		
		if(columnMajor) {
            m00 = fb.get();
            m10 = fb.get();
            m20 = fb.get();
            m30 = fb.get();
            m01 = fb.get();
            m11 = fb.get();
            m21 = fb.get();
            m31 = fb.get();
            m02 = fb.get();
            m12 = fb.get();
            m22 = fb.get();
            m32 = fb.get();
            m03 = fb.get();
            m13 = fb.get();
            m23 = fb.get();
            m33 = fb.get();
		} else {
            m00 = fb.get();
            m01 = fb.get();
            m02 = fb.get();
            m03 = fb.get();
            m10 = fb.get();
            m11 = fb.get();
            m12 = fb.get();
            m13 = fb.get();
            m20 = fb.get();
            m21 = fb.get();
            m22 = fb.get();
            m23 = fb.get();
            m30 = fb.get();
            m31 = fb.get();
            m32 = fb.get();
            m33 = fb.get();
		}
		return this;
	}
	
	/**
	 * Multiplies the <code>Matrix4f</code> with the provided one.
	 * The result is stored inside the second provided matrix or a new one if null.
	 * 
	 * @param other The other matrix to do the multiplication.
	 * @param store The matrix to store the result.
	 * @return		The resulting matrix.
	 */
    public Matrix4f mult(Matrix4f in2, Matrix4f store) {
        if (store == null) {
            store = new Matrix4f();
        }

        float temp00, temp01, temp02, temp03;
        float temp10, temp11, temp12, temp13;
        float temp20, temp21, temp22, temp23;
        float temp30, temp31, temp32, temp33;

        temp00 = m00 * in2.m00
                 + m01 * in2.m10
                 + m02 * in2.m20
                 + m03 * in2.m30;
        temp01 = m00 * in2.m01
                 + m01 * in2.m11
                 + m02 * in2.m21
                 + m03 * in2.m31;
        temp02 = m00 * in2.m02
                 + m01 * in2.m12
                 + m02 * in2.m22
                 + m03 * in2.m32;
        temp03 = m00 * in2.m03
                 + m01 * in2.m13
                 + m02 * in2.m23
                 + m03 * in2.m33;

        temp10 = m10 * in2.m00
                 + m11 * in2.m10
                 + m12 * in2.m20
                 + m13 * in2.m30;
        temp11 = m10 * in2.m01
                 + m11 * in2.m11
                 + m12 * in2.m21
                 + m13 * in2.m31;
        temp12 = m10 * in2.m02
                 + m11 * in2.m12
                 + m12 * in2.m22
                 + m13 * in2.m32;
        temp13 = m10 * in2.m03
                 + m11 * in2.m13
                 + m12 * in2.m23
                 + m13 * in2.m33;

        temp20 = m20 * in2.m00
                 + m21 * in2.m10
                 + m22 * in2.m20
                 + m23 * in2.m30;
        temp21 = m20 * in2.m01
                 + m21 * in2.m11
                 + m22 * in2.m21
                 + m23 * in2.m31;
        temp22 = m20 * in2.m02
                 + m21 * in2.m12
                 + m22 * in2.m22
                 + m23 * in2.m32;
        temp23 = m20 * in2.m03
                 + m21 * in2.m13
                 + m22 * in2.m23
                 + m23 * in2.m33;

        temp30 = m30 * in2.m00
                 + m31 * in2.m10
                 + m32 * in2.m20
                 + m33 * in2.m30;
        temp31 = m30 * in2.m01
                 + m31 * in2.m11
                 + m32 * in2.m21
                 + m33 * in2.m31;
        temp32 = m30 * in2.m02
                 + m31 * in2.m12
                 + m32 * in2.m22
                 + m33 * in2.m32;
        temp33 = m30 * in2.m03
                 + m31 * in2.m13
                 + m32 * in2.m23
                 + m33 * in2.m33;

        store.m00 = temp00;
        store.m01 = temp01;
        store.m02 = temp02;
        store.m03 = temp03;
        store.m10 = temp10;
        store.m11 = temp11;
        store.m12 = temp12;
        store.m13 = temp13;
        store.m20 = temp20;
        store.m21 = temp21;
        store.m22 = temp22;
        store.m23 = temp23;
        store.m30 = temp30;
        store.m31 = temp31;
        store.m32 = temp32;
        store.m33 = temp33;

        return store;
    }
	
    /**
	 * Set the translation components of the <code>Matrix4f</code> to the ones
	 * provided.
	 * 
	 * @param x The X-component of the translation.
	 * @param y The Y-component of the translation.
	 * @param z The Z-component of the translation.
	 */
    public void setTranslation(float x, float y, float z) {
        m03 = x;
        m13 = y;
        m23 = z;
    }
    
	/**
	 * Set the translation components of the <code>Matrix4f</code> to the ones
	 * specified in the provided <code>Vector3f</code>.
	 * 
	 * @param translation The translation vector to set.
	 */
    public void setTranslation(Vector3f translation) {
       setTranslation(translation.x, translation.y, translation.z);
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
    
    /**
   	 * Sets all the components of the <code>Matrix4f</code> to the {@link #identity()},
   	 * before retrieving it from a pool.
   	 */
   	@Override
   	public void reuse() {
   		identity();
   	}
   	
   	/**
   	 * Sets all the components of the <code>Matrix4f</code> to the {@link #identity()},
   	 * before storing it into a pool.
   	 */
   	@Override
   	public void free() {
   		identity();
   	}
    
    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        
        if (!(o instanceof Matrix4f)) {
            return false;
        }

        Matrix4f comp = (Matrix4f) o;
        if (Float.compare(m00, comp.m00) != 0) {
            return false;
        }
        if (Float.compare(m01, comp.m01) != 0) {
            return false;
        }
        if (Float.compare(m02, comp.m02) != 0) {
            return false;
        }
        if (Float.compare(m03, comp.m03) != 0) {
            return false;
        }

        if (Float.compare(m10, comp.m10) != 0) {
            return false;
        }
        if (Float.compare(m11, comp.m11) != 0) {
            return false;
        }
        if (Float.compare(m12, comp.m12) != 0) {
            return false;
        }
        if (Float.compare(m13, comp.m13) != 0) {
            return false;
        }

        if (Float.compare(m20, comp.m20) != 0) {
            return false;
        }
        if (Float.compare(m21, comp.m21) != 0) {
            return false;
        }
        if (Float.compare(m22, comp.m22) != 0) {
            return false;
        }
        if (Float.compare(m23, comp.m23) != 0) {
            return false;
        }

        if (Float.compare(m30, comp.m30) != 0) {
            return false;
        }
        if (Float.compare(m31, comp.m31) != 0) {
            return false;
        }
        if (Float.compare(m32, comp.m32) != 0) {
            return false;
        }
        if (Float.compare(m33, comp.m33) != 0) {
            return false;
        }

        return true;
    }
}
