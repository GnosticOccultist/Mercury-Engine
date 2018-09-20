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
    	return x * x + y * y + z * z + w * w;
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
}
