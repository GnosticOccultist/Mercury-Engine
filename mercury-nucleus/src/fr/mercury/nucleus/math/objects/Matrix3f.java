package fr.mercury.nucleus.math.objects;

import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.pool.Reusable;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.readable.ReadableMatrix3f;
import fr.mercury.nucleus.math.readable.ReadableQuaternion;
import fr.mercury.nucleus.math.readable.ReadableVector3f;

public class Matrix3f implements ReadableMatrix3f, Reusable, Comparable<Matrix3f> {

	/**
	 * The <code>Matrix3f</code> identity &rarr; {@link #identity()}
	 */
	public static final ReadableMatrix3f IDENTITY_MATRIX = new Matrix3f();
	
    public float m00, m01, m02;
    public float m10, m11, m12;
    public float m20, m21, m22;
    
    /**
     * Instantiates a new <code>Matrix3f</code> with the identity values ({@link #identity()}).
     */
    public Matrix3f() {
		identity();
	}
    
    /**
     * Instantiates a new <code>Matrix3f</code> with the component values of the provided matrix.
     * 
     * @param other The other matrix to copy from (not null).
     */
    public Matrix3f(ReadableMatrix3f other) {
		set(other);
	}
    
    /**
	 * Sets the components values of the <code>Matrix3f</code> to the ones from the provided matrix.
	 * 
	 * @param other The other matrix to copy from (not null).
	 * @return 		The matrix with its new components values, used for chaining methods.
	 */
    public Matrix3f set(ReadableMatrix3f other) {
    	Validator.nonNull(other, "The matrix to copy from can't be null!");
    	return set(other.m00(), other.m01(), other.m02(), other.m10(), other.m11(), other.m12(), other.m20(), other.m21(), other.m22());
    }
    
    /**
	 * Sets the components values of the <code>Matrix3f</code> to the ones from the provided {@link Quaternion}.
	 * 
	 * @param quaternion The quaternion to copy from (not null).
	 * @return 	   		 The matrix with its new components values, used for chaining methods.
	 */
    public Matrix3f set(ReadableQuaternion quaternion) {
    	Validator.nonNull(quaternion, "The quaternion to copy from can't be null!");
    	return quaternion.toRotationMatrix(this);
    }
    
    /**
     * Sets the components values of the <code>Matrix3f</code> to the provided ones.
     * 
     * @param m00 The desired row 0 - column 0 component.
     * @param m01 The desired row 0 - column 1 component.
     * @param m02 The desired row 0 - column 2 component.
     * @param m10 The desired row 1 - column 0 component.
     * @param m11 The desired row 1 - column 1 component.
     * @param m12 The desired row 1 - column 2 component.
     * @param m20 The desired row 2 - column 0 component.
     * @param m21 The desired row 2 - column 1 component.
     * @param m22 The desired row 2 - column 2 component.
     * @return    The matrix with its new components values, used for chaining methods.
     */
    public Matrix3f set(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
    	this.m00 = m00;
    	this.m01 = m01;
    	this.m02 = m02;
    	this.m10 = m10;
    	this.m11 = m11;
    	this.m12 = m12;
    	this.m20 = m20;
    	this.m21 = m21;
    	this.m22 = m22;
    	return this;
    }
    
    /**
     * Multiplies the <code>Matrix3f</code> with the provided matrix.
     * 
     * @param matrix The matrix to multiply this one by (not null).
     * @return		 The matrix multiplied with the given one.
     * 
     * @see #mul(ReadableMatrix3f, Matrix3f)
     */
    public Matrix3f mul(ReadableMatrix3f matrix) {
    	return mul(matrix, this);
    }
    
    /**
     * Multiplies the <code>Matrix3f</code> with the provided matrix and store the result
     * in the given store matrix, or a new instance if null.
     * 
     * @param matrix The matrix to multiply this one by (not null).
     * @param store	 The store for the result. 
     * @return		 The multiplication of the two matrices in the given store matrix, or a new instance one.
     */
    public Matrix3f mul(ReadableMatrix3f matrix, Matrix3f store) {
    	Validator.nonNull(matrix, "The matrix can't be null!");
    	var result = (store == null) ? new Matrix3f() : store;
    	
    	var temp00 = m00 * matrix.m00() + m01 * matrix.m10() + m02 * matrix.m20();
    	var temp01 = m00 * matrix.m01() + m01 * matrix.m11() + m02 * matrix.m21();
    	var temp02 = m00 * matrix.m02() + m01 * matrix.m12() + m02 * matrix.m22();
    	var temp10 = m10 * matrix.m00() + m11 * matrix.m10() + m12 * matrix.m20();
    	var temp11 = m10 * matrix.m01() + m11 * matrix.m11() + m12 * matrix.m21();
    	var temp12 = m10 * matrix.m02() + m11 * matrix.m12() + m12 * matrix.m22();
    	var temp20 = m20 * matrix.m00() + m21 * matrix.m10() + m22 * matrix.m20();
    	var temp21 = m20 * matrix.m01() + m21 * matrix.m11() + m22 * matrix.m21();
    	var temp22 = m20 * matrix.m02() + m21 * matrix.m12() + m22 * matrix.m22();
        
    	return result.set(temp00, temp01, temp02, temp10, temp11, temp12, temp20, temp21, temp22);
    }
    
    /**
     * Sets the <code>Matrix3f</code> to the provided rotation components in the X, Y and Z axis.
     * 
     * @param x The X axis angle to apply in radians (aka yaw).
     * @param y The Y axis angle to apply in radians (aka roll).
     * @param z The Z axis angle to apply in radians (aka pitch).
     * @return	The updated matrix with the given euler angles, for chaining purposes.
     */
    public Matrix3f fromAngles(double x, double y, double z) {
        var cy = (float) Math.cos(x);
        var sy = (float) Math.sin(x);
        var ch = (float) Math.cos(y);
        var sh = (float) Math.sin(y);
        var cp = (float) Math.cos(z);
        var sp = (float) Math.sin(z);

        m00 = ch * cp;
        m01 = sh * sy - ch * sp * cy;
        m02 = ch * sp * sy + sh * cy;
        m10 = sp;
        m11 = cp * cy;
        m12 = -cp * sy;
        m20 = -sh * cp;
        m21 = sh * sp * cy + ch * sy;
        m22 = -sh * sp * sy + ch * cy;
        return this;
    }
    
    public Matrix3f multiplyDiagonalPost(ReadableVector3f vector, Matrix3f store) {
    	var result = (store == null) ? new Matrix3f() : store;

        result.set( 
                vector.x() * m00, vector.y() * m01, vector.z() * m02,
                vector.x() * m10, vector.y() * m11, vector.z() * m12,
                vector.x() * m20, vector.y() * m21, vector.z() * m22);
        return result;
    }
    
    public Vector3f applyPost(ReadableVector3f vec, Vector3f store) {
    	var result = (store == null) ? new Vector3f() : store;
    	
    	result.setX(m00 * vec.x() + m01 * vec.y() + m02 * vec.z());
        result.setY(m10 * vec.x() + m11 * vec.y() + m12 * vec.z());
        result.setZ(m20 * vec.x() + m21 * vec.y() + m22 * vec.z());
    	return result;
    }
    
    public Matrix3f rotateX(float angle) {
    	if(angle == 0.0F) {
    		return this;
    	}
    	
    	float m01 = this.m01, m02 = this.m02, 
    	m11 = this.m11, m12 = this.m12, 
    	m21 = this.m21, m22 = this.m22;

    	var cosAngle = (float) Math.cos(angle);
    	var sinAngle = (float) Math.sin(angle);

    	this.m01 = m01 * cosAngle + m02 * sinAngle;
    	this.m02 = m02 * cosAngle - m01 * sinAngle;

    	this.m11 = m11 * cosAngle + m12 * sinAngle;
    	this.m12 = m12 * cosAngle - m11 * sinAngle;

    	this.m21 = m21 * cosAngle + m22 * sinAngle;
    	this.m22 = m22 * cosAngle - m21 * sinAngle;

    	return this;
    }
    
    public Matrix3f rotateY(float angle) {
    	if(angle == 0.0F) {
    		return this;
    	}
    	
    	float m00 = this.m00, m02 = this.m02,
    	m10 = this.m10, m12 = this.m12,
    	m20 = this.m20, m22 = this.m22;

    	var cosAngle = (float) Math.cos(angle);
    	var sinAngle = (float) Math.sin(angle);
        
    	this.m00 = m00 * cosAngle - m02 * sinAngle;
    	this.m02 = m00 * sinAngle + m02 * cosAngle;
        
    	this.m10 = m10 * cosAngle - m12 * sinAngle;
    	this.m12 = m10 * sinAngle + m12 * cosAngle;
        
    	this.m20 = m20 * cosAngle - m22 * sinAngle;
    	this.m22 = m20 * sinAngle + m22 * cosAngle;

    	return this;
    }
    
    public Matrix3f rotateZ(float angle) {
    	if(angle == 0.0F) {
    		return this;
    	}
    	
    	float m00 = this.m00, m01 = this.m01,
    	m10 = this.m10, m11 = this.m11,
    	m20 = this.m20, m21 = this.m21;

    	var cosAngle = (float) Math.cos(angle);
    	var sinAngle = (float) Math.sin(angle);

    	this.m00 = m00 * cosAngle + m01 * sinAngle;
    	this.m01 = m01 * cosAngle - m00 * sinAngle;

    	this.m10 = m10 * cosAngle + m11 * sinAngle;
    	this.m11 = m11 * cosAngle - m10 * sinAngle;

    	this.m20 = m20 * cosAngle + m21 * sinAngle;
    	this.m21 = m21 * cosAngle - m20 * sinAngle;

        return this;
    }
    
    /**
     * Set the <code>Matrix3f</code> to its identity values.
     * It is all zeros except the diagonal values which are set to 1.
     * <pre>
     * | 1 | 0 | 0 |
     * | 0 | 1 | 0 |
     * | 0 | 0 | 1 |
     * </pre>
     */
    public void identity() {
        m01 = m02 = 0.0f;
        m10 = m12 = 0.0f;
        m20 = m21 = 0.0f;
        m00 = m11 = m22 = 1.0f;
	}
    
    @Override
    public boolean isIdentity() {
    	return equals(IDENTITY_MATRIX);
    }
    
    @Override
    public boolean isOrthonormal() {
        if(Math.abs(m00 * m00 + m01 * m01 + m02 * m02 - 1.0) > MercuryMath.EPSILON) {
            return false;
        } else if (Math.abs(m00 * m10 + m01 * m11 + m02 * m12 - 0.0) > MercuryMath.EPSILON) {
            return false;
        } else if (Math.abs(m00 * m20 + m01 * m21 + m02 * m22 - 0.0) > MercuryMath.EPSILON) {
            return false;
        } else if (Math.abs(m10 * m00 + m11 * m01 + m12 * m02 - 0.0) > MercuryMath.EPSILON) {
            return false;
        } else if (Math.abs(m10 * m10 + m11 * m11 + m12 * m12 - 1.0) > MercuryMath.EPSILON) {
            return false;
        } else if (Math.abs(m10 * m20 + m11 * m21 + m12 * m22 - 0.0) > MercuryMath.EPSILON) {
            return false;
        } else if (Math.abs(m20 * m00 + m21 * m01 + m22 * m02 - 0.0) > MercuryMath.EPSILON) {
            return false;
        } else if (Math.abs(m20 * m10 + m21 * m11 + m22 * m12 - 0.0) > MercuryMath.EPSILON) {
            return false;
        } else if (Math.abs(m20 * m20 + m21 * m21 + m22 * m22 - 1.0) > MercuryMath.EPSILON) {
            return false;
        }
        
        return true;
    }
    
    /**
	 * Populates the given {@link FloatBuffer} with the data from the <code>Matrix3f</code> in column 
	 * major order.
	 * <p>
	 * The method is using relative put method, meaning the float data is written at the current 
	 * buffer's position and the position is incremented by 9.
	 * <p>
	 * The populated buffer can be used safely to transfer data to shaders as mat3 uniforms.
	 * 
	 * @param store The buffer to populate with the data (not null). 
	 * @return 		The given store populated with the matrix data.
	 * 
	 * @throws BufferOverflowException Thrown if there isn't enough space to write all 9 floats.
	 * 
	 * @see #populate(FloatBuffer, boolean)
	 */
    @Override
    public FloatBuffer populate(FloatBuffer store) {
    	Validator.nonNull(store, "The float buffer can't be null!");
    	return populate(store, true);
    }
    
    /**
	 * Populates the given {@link FloatBuffer} with the data from the <code>Matrix3f</code>.
	 * <p>
	 * The method is using relative put method, meaning the float data is written at the current 
	 * buffer's position and the position is incremented by 9.
	 * <p>
	 * The populated buffer can be used safely to transfer data to shaders as mat3 uniforms.
	 * 
	 * @param store 	  The buffer to populate with the data (not null). 
	 * @param columnMajor Whether to write the data in column or row major order.
	 * @return 			  The given store populated with the matrix data.
	 * 
	 * @throws BufferOverflowException Thrown if there isn't enough space to write all 9 floats.
	 * 
	 * @see #populate(FloatBuffer)
	 */
    public FloatBuffer populate(FloatBuffer store, boolean columnMajor) {
    	Validator.nonNull(store, "The float buffer can't be null!");
    	
    	if(columnMajor) {
    		store.put(m00);
			store.put(m10);
			store.put(m20);
			store.put(m01);
			store.put(m11);
			store.put(m21);
			store.put(m02);
			store.put(m12);
			store.put(m22);
		} else {
			store.put(m00);
			store.put(m01);
			store.put(m02);
			store.put(m10);
			store.put(m11);
			store.put(m12);
			store.put(m20);
			store.put(m21);
			store.put(m22);
		}
    	
    	return store;
    }

	@Override
	public float m00() {
		return m00;
	}

	@Override
	public float m01() {
		return m01;
	}

	@Override
	public float m02() {
		return m02;
	}

	@Override
	public float m10() {
		return m10;
	}

	@Override
	public float m11() {
		return m11;
	}

	@Override
	public float m12() {
		return m12;
	}

	@Override
	public float m20() {
		return m20;
	}

	@Override
	public float m21() {
		return m21;
	}

	@Override
	public float m22() {
		return m22;
	}
	
	@Override
	public void reuse() {
		identity();
	}
	
	@Override
	public void free() {
		identity();
	}
	
	@Override
	public int compareTo(Matrix3f other) {
		if(isIdentity() && other.isIdentity()) {
			return 0;
		}
		
		int result = Float.compare(m00, other.m00);
		if(result == 0) {
			result = Float.compare(m01, other.m01);
		}
        if(result == 0) {
        	result = Float.compare(m02, other.m02);
        }
        
        if(result == 0) {
			result = Float.compare(m10, other.m10);
		}
        if(result == 0) {
			result = Float.compare(m11, other.m11);
		}
        if(result == 0) {
        	result = Float.compare(m12, other.m12);
        }
        
        if(result == 0) {
			result = Float.compare(m20, other.m20);
		}
        if(result == 0) {
			result = Float.compare(m21, other.m21);
		}
        if(result == 0) {
        	result = Float.compare(m22, other.m22);
        }
        
        return result;
	}
	
	@Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        
        if (!(o instanceof Matrix3f)) {
            return false;
        }

        Matrix3f comp = (Matrix3f) o;
        if (Float.compare(m00, comp.m00) != 0) {
            return false;
        }
        if (Float.compare(m01, comp.m01) != 0) {
            return false;
        }
        if (Float.compare(m02, comp.m02) != 0) {
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

        if (Float.compare(m20, comp.m20) != 0) {
            return false;
        }
        if (Float.compare(m21, comp.m21) != 0) {
            return false;
        }
        if (Float.compare(m22, comp.m22) != 0) {
            return false;
        }

        return true;
    }
}
