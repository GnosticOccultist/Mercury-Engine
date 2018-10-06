package fr.mercury.nucleus.math.objects;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.MercuryMath;

/**
 * <code>Quaternion</code> defines a rotation in 4 dimensions instead of 3, using hypercomplex numbers.
 * It avoids "gimbal lock" by allowing a smooth rotation without any movement blocking. 
 * Simpler to compose than the <code>Euler angles</code> and more compact and efficient than the rotation
 * matrices.
 * <p>
 * Example of representation:
 * <i><li>A rotation in 3-axis.
 * </i></li>
 * <p>
 * 
 * @author GnosticOccultist
 */
public final class Quaternion {
	
	/**
	 * The X-component of the vector.
	 */
	public float x;
	/**
	 * The Y-component of the vector.
	 */
	public float y;
	/**
	 * The Z-component of the vector.
	 */
	public float z;
	/**
	 * The W-component of the vector.
	 */
	public float w;
	
	/**
	 * Instantiates a new <code>Quaternion</code> with identity 
	 * values {0,0,0,1}.
	 */
	public Quaternion() {
		set(0, 0, 0, 1);
	}
	
	/**
	 * Instantiates a new <code>Quaternion</code> with the provided
	 * components.
	 * 
	 * @param x The X-component of the quaternion.
	 * @param y The Y-component of the quaternion.
	 * @param z The Z-component of the quaternion.
	 * @param w The W-component of the quaternion.
	 */
	public Quaternion(float x, float y, float z, float w) {
		set(x, y, z, w);
	}
	
	/**
	 * Instantiates a new <code>Quaternion</code> with the provided
	 * quaternion's components.
	 * 
	 * @param other The other quaternion to get the components.
	 */
	public Quaternion(Quaternion other) {
		set(other);
	}

	/**
	 * Set the provided components values to this <code>Quaternion</code> 
	 * components.
	 * 
	 * @param x The X-component to copy from.
	 * @param y The Y-component to copy from.
	 * @param z The Z-component to copy from.
	 * @param w The W-component to copy from.
	 */
	public Quaternion set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	/**
	 * Set the components values of the provided quaternion to this 
	 * <code>Quaternion</code> components.
	 * <p>
	 * The provided quaternion cannot be null.
	 * 
	 * @param other The other quaternion to copy from.
	 */
	public Quaternion set(Quaternion other) {
		Validator.nonNull(other, "The quaternion cannot be null!");
		
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
		this.w = other.w;
		return this;
	}
	
	/**
	 * Add the provided components to this <code>Quaternion</code> components.
	 * 
	 * @param x The X-component to addition.
	 * @param y The Y-component to addition.
	 * @param z The Z-component to addition.
	 * @param w The W-component to addition.
	 * 
	 * @return	The quaternion with its new components values.
	 */
	public Quaternion add(float x, float y, float z, float w) {
		this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
	}
	
	/**
	 * Add the quaternion vector to this <code>Quaternion</code> components.
	 * <p>
	 * The provided quaternion cannot be null.
	 * 
	 * @param other The other quaternion to addition.
	 * 
	 * @return		The quaternion with its new components values.
	 */
	public Quaternion add(Quaternion other) {
        Validator.nonNull(other, "The quaternion cannot be null!");
		
        return add(other.x, other.y, other.z, other.w);
	}
	
	/**
	 * Subtracts the provided components to this <code>Quaternion</code> components.
	 * 
	 * @param x The X-component to subtract.
	 * @param y The Y-component to subtract.
	 * @param z The Z-component to subtract.
	 * @param w The W-component to subtract.
	 * 
	 * @return	The quaternion with its new components values.
	 */
	public Quaternion sub(float x, float y, float z, float w) {
		this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
	}
	
	/**
	 * Subtracts the provided quaternion to this <code>Quaternion</code> components.
	 * <p>
	 * The provided quaternion cannot be null.
	 * 
	 * @param other The other quaternion to subtract.
	 * 
	 * @return		The quaternion with its new components values.
	 */
	public Quaternion sub(Quaternion other) {
        Validator.nonNull(other, "The quaternion cannot be null!");
		
		return sub(other.x, other.y, other.z, other.w);
	}
	
	/**
	 * Multiplies the provided components to this <code>Quaternion</code> components.
	 * 
	 * @param x The X-component to multiply.
	 * @param y The Y-component to multiply.
	 * @param z The Z-component to multiply.
	 * @param w The W-component to multiply.
	 * 
	 * @return 	The quaternion with its new components values. 
	 */
    public Quaternion mul(float x, float y, float z, float w) {
    	this.x =  this.x * w + this.y * z - this.z * y + this.w * x;
    	this.y = -this.x * z + this.y * w + this.z * x + this.w * y;
    	this.z =  this.x * y - this.y * x + this.z * w + this.w * z;
    	this.w = -this.x * x - this.y * y - this.z * z + this.w * w;
    	
    	return this;
    }
    
    /**
	 * Multiplies the provided quaternion to this <code>Quaternion</code> components.
	 * <p>
	 * The provided quaternion cannot be null.
	 * 
	 * @param other The other quaternion to multiply.
	 * 
	 * @return 		The quaternion with its new components values. 
	 */
    public Quaternion mul(Quaternion other) {
    	Validator.nonNull(other, "The quaternion cannot be null!");
    	
    	return mul(other.x, other.y, other.z, other.w);
    }
    
    /**
     * Return the norm of the <code>Quaternion</code>. 
     * 
     * @return The norm of the quaternion.
     */
    public float norm() {
    	return w * w + x * x + y * y + z * z;
    }
	
    /**
     * Normalizes the <code>Quaternion</code>. It will will divide
     * the quaternion's components by its {@link #norm()}.
     * 
     * @return The normalized quaternion.
     */
    public Quaternion normalize() {
		float invNorm = MercuryMath.invSqrt(norm());
		x *= invNorm;
		y *= invNorm;
		z *= invNorm;
		w *= invNorm;
		return this;
	}
    
    /**
     * Converts the <code>Quaternion</code> to a rotation <code>Matrix4f</code>, 
     * stored into the provided matrix.
     * <p>
     * Note that the 4th row and columns are leaved untouched and that this operation
     * preserve the scale of the <code>Matrix4f</code>.
     * 
     * @param result The matrix to store the result.
     * @return		 The rotation matrix with scaling preserved.
     */
    public Matrix4f toRotationMatrix(Matrix4f result) {
       
    	// Saving the original scale before applying the rotation, to be restored
    	// at the end.
    	Vector3f originalScale = MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class);
    	originalScale.set(0, 0, 0);
        result.getScale(originalScale);
        result.setScale(1, 1, 1);
        
        float norm = norm();
        // Check first if the norm is equal to one to avoid the division,
        // don't know if it's really necessary?
        float s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;

        // Compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
        // will be used 2-4 times each.
        float xs = x * s;
        float ys = y * s;
        float zs = z * s;
        float xx = x * xs;
        float xy = x * ys;
        float xz = x * zs;
        float xw = w * xs;
        float yy = y * ys;
        float yz = y * zs;
        float yw = w * ys;
        float zz = z * zs;
        float zw = w * zs;

        // Using s = 2/norm (instead of 1/norm) saves 9 multiplications by 2 here.
        result.m00 = 1 - (yy + zz);
        result.m01 = (xy - zw);
        result.m02 = (xz + yw);
        result.m10 = (xy + zw);
        result.m11 = 1 - (xx + zz);
        result.m12 = (yz - xw);
        result.m20 = (xz - yw);
        result.m21 = (yz + xw);
        result.m22 = 1 - (xx + yy);

        // Finally restore the scale of the matrix.
        result.setScale(originalScale);

        return result;
    }
    
    public Matrix4f rotationMatrix(Matrix4f result) {
        float norm = norm();
        // Check first if the norm is equal to one to avoid the division,
        // don't know if it's really necessary?
        float s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;

        // Compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
        // will be used 2-4 times each.
        float xs = x * s;
        float ys = y * s;
        float zs = z * s;
        float xx = x * xs;
        float xy = x * ys;
        float xz = x * zs;
        float xw = w * xs;
        float yy = y * ys;
        float yz = y * zs;
        float yw = w * ys;
        float zz = z * zs;
        float zw = w * zs;

        // Using s = 2/norm (instead of 1/norm) saves 9 multiplications by 2 here.
        result.m00 = 1 - (yy + zz);
        result.m01 = (xy - zw);
        result.m02 = (xz + yw);
        result.m10 = (xy + zw);
        result.m11 = 1 - (xx + zz);
        result.m12 = (yz - xw);
        result.m20 = (xz - yw);
        result.m21 = (yz + xw);
        result.m22 = 1 - (xx + yy);
        
        return result;
    }
    
	/**
	 * Return whether the <code>Quaternion</code> is an identity one.
	 * <p>
	 * If the quaternion is [0,0,0,1].
	 * 
	 * @return Whether the quaternion is an identity one.
	 */
	public boolean isIdentity() {
		return x == 0 && y == 0 && z == 0 && w == 1;
	}
	
	/**
	 * Set the <code>Quaternion</code> to its identity values.
	 * <p>
	 * Same as calling <code>set(0,0,0,1)</code>.
	 * 
	 * @return The identity quaternion.
	 */
	public Quaternion identity() {
		x = y = z = 0;
		w = 1;
		return this;
	}
	
	/**
	 * Set each component value of the <code>Quaternion</code> to 0.
	 * 
	 * @return The zero quaternion.
	 */
	public Quaternion zero() {
		x = y = z = w = 0;
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Quaternion)) {
			return false;
		}

		if (this == o) {
			return true;
		}

		Quaternion other = (Quaternion) o;
		if (Float.compare(x, other.x) != 0) {
			return false;
		}
		if (Float.compare(y, other.y) != 0) {
			return false;
		}
		if (Float.compare(z, other.z) != 0) {
			return false;
		}
		return Float.compare(w, other.w) == 0;
	}

	public Quaternion fromAxes(Vector3f xAxis, Vector3f yAxis, Vector3f zAxis) {
		return fromRotationMatrix(xAxis.x, yAxis.x, zAxis.x, xAxis.y, yAxis.y,
                zAxis.y, xAxis.z, yAxis.z, zAxis.z);
	}

	public Quaternion fromRotationMatrix(float m00, float m01, float m02,
            float m10, float m11, float m12, float m20, float m21, float m22) {
        // first normalize the forward (F), up (U) and side (S) vectors of the rotation matrix
        // so that the scale does not affect the rotation
        float lengthSquared = m00 * m00 + m10 * m10 + m20 * m20;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = 1.0f / MercuryMath.sqrt(lengthSquared);
            m00 *= lengthSquared;
            m10 *= lengthSquared;
            m20 *= lengthSquared;
        }
        lengthSquared = m01 * m01 + m11 * m11 + m21 * m21;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = 1.0f / MercuryMath.sqrt(lengthSquared);
            m01 *= lengthSquared;
            m11 *= lengthSquared;
            m21 *= lengthSquared;
        }
        lengthSquared = m02 * m02 + m12 * m12 + m22 * m22;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = 1.0f / MercuryMath.sqrt(lengthSquared);
            m02 *= lengthSquared;
            m12 *= lengthSquared;
            m22 *= lengthSquared;
        }

        // Use the Graphics Gems code, from 
        // ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z
        // *NOT* the "Matrix and Quaternions FAQ", which has errors!

        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        float t = m00 + m11 + m22;

        // we protect the division by s by ensuring that s>=1
        if (t >= 0) { // |w| >= .5
            float s = MercuryMath.sqrt(t + 1); // |s|>=1 ...
            w = 0.5f * s;
            s = 0.5f / s;                // so this division isn't bad
            x = (m21 - m12) * s;
            y = (m02 - m20) * s;
            z = (m10 - m01) * s;
        } else if ((m00 > m11) && (m00 > m22)) {
            float s = MercuryMath.sqrt(1.0f + m00 - m11 - m22); // |s|>=1
            x = s * 0.5f; // |x| >= .5
            s = 0.5f / s;
            y = (m10 + m01) * s;
            z = (m02 + m20) * s;
            w = (m21 - m12) * s;
        } else if (m11 > m22) {
            float s = MercuryMath.sqrt(1.0f + m11 - m00 - m22); // |s|>=1
            y = s * 0.5f; // |y| >= .5
            s = 0.5f / s;
            x = (m10 + m01) * s;
            z = (m21 + m12) * s;
            w = (m02 - m20) * s;
        } else {
            float s = MercuryMath.sqrt(1.0f + m22 - m00 - m11); // |s|>=1
            z = s * 0.5f; // |z| >= .5
            s = 0.5f / s;
            x = (m02 + m20) * s;
            y = (m21 + m12) * s;
            w = (m10 - m01) * s;
        }
        return this;
	}
}
