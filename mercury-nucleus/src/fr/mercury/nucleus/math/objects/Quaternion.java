package fr.mercury.nucleus.math.objects;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.pool.Reusable;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.readable.ReadableQuaternion;

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
public final class Quaternion implements ReadableQuaternion, Comparable<Quaternion>, Reusable {
	
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
	public Quaternion set(ReadableQuaternion other) {
		Validator.nonNull(other, "The quaternion cannot be null!");
		
		this.x = other.x();
		this.y = other.y();
		this.z = other.z();
		this.w = other.w();
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
    public Quaternion mul(float qx, float qy, float qz, float qw) {
        float x1 = x * qw + y * qz - z * qy + w * qx;
        float y1 = -x * qz + y * qw + z * qx + w * qy;
        float z1 = x * qy - y * qx + z * qw + w * qz;
        w = -x * qx - y * qy - z * qz + w * qw;
        x = x1;
        y = y1;
        z = z1;
    	
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
     * Multiplies the <code>Quaternion</code> by the provided one and store the result in
     * the given quaternion store or a new instance one if null.
     * 
     * @param q		The quaternion to multiply by.
     * @param store	The quaternion to store the result.
     * @return		The result in the store or a new quaternion instance.
     */
    public Quaternion mul(Quaternion q, Quaternion store) {
    	Validator.nonNull(q, "The quaternion can't be null!");
    	var result = (store == null) ? new Quaternion() : store;
    	
        float qw = q.w, qx = q.x, qy = q.y, qz = q.z;
        result.x = x * qw + y * qz - z * qy + w * qx;
        result.y = -x * qz + y * qw + z * qx + w * qy;
        result.z = x * qy - y * qx + z * qw + w * qz;
        result.w = -x * qx - y * qy - z * qz + w * qw;
        return result;
    }
    
    /**
     * Multiplies the <code>Quaternion</code> with the provided {@link Vector3f}. The result
     * is stored in the given vector.
     * <p>
	 * The provided vector cannot be null.
     * 
     * @param vector The vector to multiply the quaternion by.
     * @return		 The storing vector with the result.
     */
    public Vector3f mul(Vector3f vector) {
    	Validator.nonNull(vector, "The vector cannot be null!");
    	
    	float tempX, tempY;
        tempX = w * w * vector.x + 2 * y * w * vector.z - 2 * z * w * vector.y + x * x * vector.x
                + 2 * y * x * vector.y + 2 * z * x * vector.z - z * z * vector.x - y * y * vector.x;
        tempY = 2 * x * y * vector.x + y * y * vector.y + 2 * z * y * vector.z + 2 * w * z
                * vector.x - z * z * vector.y + w * w * vector.y - 2 * x * w * vector.z - x * x
                * vector.y;
        vector.z = 2 * x * z * vector.x + 2 * y * z * vector.y + z * z * vector.z - 2 * w * y * vector.x
                - y * y * vector.z + 2 * w * x * vector.y - x * x * vector.z + w * w * vector.z;
    	vector.x = tempX;
    	vector.y = tempY;
    	
    	return vector;
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
		var invNorm = MercuryMath.invSqrt(norm());
		x *= invNorm;
		y *= invNorm;
		z *= invNorm;
		w *= invNorm;
		return this;
	}
    
    /**
     * Retrieve the given column of the rotation matrix represented by the <code>Quaternion</code>.
     * The index must be between 0 and 2 included:
     * <li>0: the left-axis of the rotation.</li>
     * <li>1: the up-axis of the rotation.</li>
     * <li>2: the direction which is facing the rotation.</li>
     * <p>
     * 
     * @param index The index of the column to retrieve (&ge;0, &le;2).
     * @param store	The vector to store the result.
     * @return		The rotation column either the store or a new vector instance.
     */
    @Override
    public Vector3f getRotationColumn(int index, Vector3f store) {
    	Validator.inRange(index, "The index of the column is out of range!", 0, 2);
    	var result = (store == null) ? new Vector3f() : store;
    	
    	var norm = norm();
    	/*
    	 * Check first if our norm is equal to one or zero to avoid useless division.
    	 */
        var s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;
        /*
         * Compute xs, ys and zs first to save save 6 multiplications, since xs/ys/zs
         * will be used 2-4 times each.
         */
        var xs = x * s;
        var ys = y * s;
        var zs = z * s;
        var xx = x * xs;
        var xy = x * ys;
        var xz = x * zs;
        var xw = w * xs;
        var yy = y * ys;
        var yz = y * zs;
        var yw = w * ys;
        var zz = z * zs;
        var zw = w * zs;
        
        /*
         * Using s = 2/norm (instead of 1/norm) saves 9 multiplications by 2 here.
         */
        switch (index) {
            case 0:
            	result.x = 1.0F - (yy + zz);
            	result.y = (xy + zw);
                result.z = (xz - yw);
                break;
            case 1:
            	result.x = (xy - zw);
                result.y = 1.0F - (xx + zz);
                result.z = (yz + xw);
                break;
            case 2:
            	result.x = (xz + yw);
                result.y = (yz - xw);
                result.z = 1.0F - (xx + yy);
                break;
        }
        return result;
    }
    
    /**
	 * Sets the <code>Quaternion</code> components to match the provided rotation angles.
	 * 
	 * @param xAngle The angle around the X axis in radians.
	 * @param yAngle The angle around the Y axis in radians.
	 * @param zAngle The angle around the Z axis in radians.
	 * @return		 The updated quaternion for chaining purposes.
	 */
    public Quaternion fromAngles(float xAngle, float yAngle, float zAngle) {
        float angle;
        float sinY, sinZ, sinX, cosY, cosZ, cosX;
        
        angle = zAngle * 0.5f;
        sinZ = (float) Math.sin(angle);
        cosZ = (float) Math.cos(angle);
        angle = yAngle * 0.5f;
        sinY = (float) Math.sin(angle);
        cosY = (float) Math.cos(angle);
        angle = xAngle * 0.5f;
        sinX = (float) Math.sin(angle);
        cosX = (float) Math.cos(angle);

        /*
         * Use some variables to reduce the amount of multiplications.
         */
        float cosYXcosZ = cosY * cosZ;
        float sinYXsinZ = sinY * sinZ;
        float cosYXsinZ = cosY * sinZ;
        float sinYXcosZ = sinY * cosZ;

        w = (cosYXcosZ * cosX - sinYXsinZ * sinX);
        x = (cosYXcosZ * sinX + sinYXsinZ * cosX);
        y = (sinYXcosZ * cosX + cosYXsinZ * sinX);
        z = (cosYXsinZ * cosX - sinYXcosZ * sinX);

        normalize();
        return this;
    }
    
    /**
     * Converts the <code>Quaternion</code> to a rotation {@link Matrix4f}, stored into the provided matrix.
     * 
     * @param result The matrix to store the result.
     * @return		 A rotation matrix either the store or a new matrix instance.
     * 
     * @see #toRotationMatrix(Matrix3f)
     */
    @Override
    public Matrix4f toRotationMatrix(Matrix4f store) {
    	var result = (store == null) ? new Matrix4f() : store;
    	
    	var norm = norm();
    	/*
    	 * Check first if our norm is equal to one or zero to avoid useless division.
    	 */
        var s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;
        /*
         * Compute xs, ys and zs first to save save 6 multiplications, since xs/ys/zs
         * will be used 2-4 times each.
         */
        var xs = x * s;
        var ys = y * s;
        var zs = z * s;
        var xx = x * xs;
        var xy = x * ys;
        var xz = x * zs;
        var xw = w * xs;
        var yy = y * ys;
        var yz = y * zs;
        var yw = w * ys;
        var zz = z * zs;
        var zw = w * zs;

        /*
         * Using s = 2/norm (instead of 1/norm) saves 9 multiplications by 2 here.
         */
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
     * Converts the <code>Quaternion</code> to a rotation {@link Matrix3f}, stored into the provided matrix.
     * 
     * @param result The matrix to store the result.
     * @return		 A rotation matrix either the store or a new matrix instance.
     * 
     * @see #toRotationMatrix(Matrix4f)
     */
    @Override
    public Matrix3f toRotationMatrix(Matrix3f store) {
    	var result = (store == null) ? new Matrix3f() : store;
    	
    	var norm = norm();
    	/*
    	 * Check first if our norm is equal to one or zero to avoid useless division.
    	 */
        var s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;
        /*
         * Compute xs, ys and zs first to save save 6 multiplications, since xs/ys/zs
         * will be used 2-4 times each.
         */
        var xs = x * s;
        var ys = y * s;
        var zs = z * s;
        var xx = x * xs;
        var xy = x * ys;
        var xz = x * zs;
        var xw = w * xs;
        var yy = y * ys;
        var yz = y * zs;
        var yw = w * ys;
        var zz = z * zs;
        var zw = w * zs;
        
        /*
         * Using s = 2/norm (instead of 1/norm) saves 9 multiplications by 2 here.
         */
        result.m00 = 1.0F - (yy + zz);
        result.m01 = (xy - zw);
        result.m02 = (xz + yw);
        
        result.m10 = (xy + zw);
        result.m11 = 1.0F - (xx + zz);
        result.m12 = (yz - xw);
        
        result.m20 = (xz - yw);
        result.m21 = (yz + xw);
        result.m22 = 1.0F - (xx + yy);
        
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
	
	/**
	 * Return the X-component of the <code>Quaternion</code>,
	 * as a single-precision float.
	 * 
	 * @return The X-coordinate value of the quaternion.
	 */
	@Override
	public float x() {
		return x;
	}
	
	/**
	 * Return the Y-component of the <code>Quaternion</code>,
	 * as a single-precision float.
	 * 
	 * @return The Y-coordinate value of the quaternion.
	 */
	@Override
	public float y() {
		return y;
	}
	
	/**
	 * Return the Z-component of the <code>Quaternion</code>,
	 * as a single-precision float.
	 * 
	 * @return The Z-coordinate value of the quaternion.
	 */
	@Override
	public float z() {
		return z;
	}
	
	/**
	 * Return the W-component of the <code>Quaternion</code>,
	 * as a single-precision float.
	 * 
	 * @return The W-coordinate value of the quaternion.
	 */
	@Override
	public float w() {
		return w;
	}
    
    /**
	 * Sets all the components of the <code>Quaternion</code> to the {@link #identity()},
	 * before retrieving it from a pool.
	 */
	@Override
	public void reuse() {
		identity();
	}
	
	/**
	 * Sets all the components of the <code>Quaternion</code> to the {@link #identity()},
	 * before storing it into a pool.
	 */
	@Override
	public void free() {
		identity();
	}
    
	/**
	 * Clone the <code>Quaternion</code> to a new quaternion instance with the same
	 * components as the caller.
	 * 
	 * @return A cloned instance of the quaternion.
	 */
	@Override
	public Quaternion clone() {
		return new Quaternion(this);
	}
	
    /**
	 * Compare this quaternion with the provided <code>Quaternion</code>. It will first
	 * compare the X-component, then the Y-component, the Z-component and so on.
	 * 
	 * @param  The other quaternion to compare with (not null).
	 * @return 0 &rarr; the 2 quaternions are equal, negative &rarr; this quaternion comes before 
	 * 		   the other, negative &rarr; this quaternion comes after the other.
	 */
	@Override
	public int compareTo(Quaternion other) {
		int result = Float.compare(x, other.x);
		if(result == 0) {
			result = Float.compare(y, other.y);
		}
        if(result == 0) {
        	result = Float.compare(z, other.z);
        }
        if(result == 0) {
        	result = Float.compare(w, other.w);
        }
        
        return result;
	}
	
	/**
	 * Return a unique code for the <code>Quaternion</code> based on each component value.
	 * If two quaternions are logically equivalent, they will return the same hash code value.
	 * 
	 * @return The hash code value of the quaternion.
	 */
	@Override
	public int hashCode() {
		return Float.floatToIntBits(x) * Float.floatToIntBits(y) 
				* Float.floatToIntBits(z) * Float.floatToIntBits(w);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if (!(o instanceof ReadableQuaternion)) {
			return false;
		}

		var other = (ReadableQuaternion) o;
		if (Float.compare(x, other.x()) != 0) {
			return false;
		}
		if (Float.compare(y, other.y()) != 0) {
			return false;
		}
		if (Float.compare(z, other.z()) != 0) {
			return false;
		}
		
		return Float.compare(w, other.w()) == 0;
	}
}
