package fr.mercury.nucleus.math.objects;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.pool.Reusable;
import fr.mercury.nucleus.math.readable.ReadableVector3f;

/**
 * <code>Vector3f</code> is a mathematical utility class representing a vector with
 * 3 components on the 3 axis (X;Y;Z) of a 3D space. It contains some utility methods
 * for fast calculation or physical determination.
 * <p>
 * Example of representation:
 * <i><li>A vertex
 * <li>A normal
 * <li>A translation
 * <li>A scale
 * </i></li>
 * <p>
 * 
 * @author GnosticOccultist
 */
public final class Vector3f implements ReadableVector3f, Comparable<Vector3f>, Reusable {
	
	/**
	 * The zero components <code>Vector3f</code> &rarr; [0,0,0].
	 */
	public static final Vector3f ZERO = new Vector3f(0, 0, 0);
	/**
	 * The unit <code>Vector3f</code> in the X-axis &rarr; [1,0,0].
	 */
	public static final Vector3f UNIT_X = new Vector3f(1, 0, 0);
	/**
	 * The unit <code>Vector3f</code> in the Y-axis &rarr; [0,1,0].
	 */
	public static final Vector3f UNIT_Y = new Vector3f(0, 1, 0);
	/**
	 * The unit <code>Vector3f</code> in the Z-axis &rarr; [0,0,1].
	 */
	public static final Vector3f UNIT_Z = new Vector3f(0, 0, 1);
	
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
	 * Instantiates a new <code>Vector3f</code> with 0 value for 
	 * each component {0,0,0}.
	 */
	public Vector3f() {
		set(0, 0, 0);
	}
	
	/**
	 * Instantiates a new <code>Vector3f</code> with the provided
	 * components.
	 * 
	 * @param x The X-component of the vector.
	 * @param y The Y-component of the vector.
	 * @param z The Z-component of the vector.
	 */
	public Vector3f(float x, float y, float z) {
		set(x, y, z);
	}
	
	/**
	 * Instantiates a new <code>Vector3f</code> with the provided
	 * vector's components.
	 * 
	 * @param other The other vector to get the components.
	 */
	public Vector3f(Vector3f other) {
		set(other);
	}
	
	/**
	 * Set the provided components values to this <code>Vector3f</code> 
	 * components.
	 * 
	 * @param x The X-component to copy from.
	 * @param y The Y-component to copy from.
	 * @param z The Z-component to copy from.
	 */
	public Vector3f set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	/**
	 * Set the components values of the provided vector to this 
	 * <code>Vector3f</code> components.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param other The other vector to copy from.
	 */
	public Vector3f set(ReadableVector3f other) {
		Validator.nonNull(other, "The vector cannot be null!");
		
		this.x = other.x();
		this.y = other.y();
		this.z = other.z();
		return this;
	}
	
	/**
	 * Add the provided components to this <code>Vector3f</code> components.
	 * 
	 * @param x The X-component to addition.
	 * @param y The Y-component to addition.
	 * @param z The Z-component to addition.
	 * 
	 * @return	The vector with its new components values.
	 */
	public Vector3f add(float x, float y, float z) {
		this.x += x;
        this.y += y;
        this.z += z;
        return this;
	}
	
	/**
	 * Add the provided vector to this <code>Vector3f</code> components.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param other The other vector to addition.
	 * 
	 * @return		The vector with its new components values.
	 */
	public Vector3f add(Vector3f other) {
        Validator.nonNull(other, "The vector cannot be null!");
		
		this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        return this;
	}
	
	/**
	 * Subtracts the provided components to this <code>Vector3f</code> components.
	 * 
	 * @param x The X-component to subtract.
	 * @param y The Y-component to subtract.
	 * @param z The Z-component to subtract.
	 * 
	 * @return	The vector with its new components values.
	 */
	public Vector3f sub(float x, float y, float z) {
		this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
	}
	
	/**
	 * Subtracts the provided vector to this <code>Vector3f</code> components.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param other The other vector to subtract.
	 * 
	 * @return		The vector with its new components values.
	 */
	public Vector3f sub(Vector3f other) {
        Validator.nonNull(other, "The vector cannot be null!");
		
		this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
        return this;
	}
	
	/**
	 * Multiplies the provided components to this <code>Vector3f</code> components.
	 * 
	 * @param x The X-component to multiply.
	 * @param y The Y-component to multiply.
	 * @param z The Z-component to multiply.
	 * 
	 * @return 	The vector with its new components values. 
	 */
    public Vector3f mul(float x, float y, float z) {
    	this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }
    
    /**
	 * Multiplies the provided vector to this <code>Vector3f</code> components.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param other The other vector to multiply.
	 * 
	 * @return 		The vector with its new components values. 
	 */
    public Vector3f mul(Vector3f other) {
    	Validator.nonNull(other, "The vector cannot be null!");
    	
    	this.x *= other.x;
        this.y *= other.y;
        this.z *= other.z;
        return this;
    }
    
    /**
	 * Divides the provided components to this <code>Vector3f</code> components.
	 * 
	 * @param x The X-component to divide.
	 * @param y The Y-component to divide.
	 * @param z The Z-component to divide.
	 * 
	 * @return 	The vector with its new components values. 
	 */
    public Vector3f div(float x, float y, float z) {
    	this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }
    
    /**
	 * Divides the provided vector to this <code>Vector3f</code> components.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param other The other vector to divide.
	 * 
	 * @return 		The vector with its new components values. 
	 */
    public Vector3f div(Vector3f other) {
    	Validator.nonNull(other, "The vector cannot be null!");
    	
    	this.x /= other.x;
        this.y /= other.y;
        this.z /= other.z;
        return this;
    }
    
    /**
     * Calculates the dot product of the <code>Vector3f</code> with the provided one.
     * If dot product = 0, the vectors are orthogonal.
     * <p>
	 * The provided vector cannot be null.
     * 
     * @param other The vector to get the dot product with.
     * @return		The resulting scalar from the dot product.
     */
    public float dot(Vector3f other) {
    	Validator.nonNull(other, "The vector cannot be null!");
    	
    	return x * other.x + y * other.y + z * other.z;
    }
    
    /**
     * Calculates the cross product of the <code>Vector3f</code> with the provided one.
     * The resulting vector is orthogonal to the plane constitued by the two vectors, meaning
     * it will be orthogonal to the two vectors (normal vector).
     * <p>
	 * The provided vector cannot be null.
     * 
     * @param other The vector to get the cross product with.
     * @return		The resulting vector from the cross product.
     */
    public Vector3f cross(Vector3f other) {
    	Validator.nonNull(other, "The vector cannot be null!");
    	
    	this.x = (y * other.z) - (z * other.y);
    	this.y = (z * other.x) - (x * other.z);
    	this.z = (x * other.y) - (y * other.x);
    	return this;
    }
	
	/**
	 * Return the length of the <code>Vector3f</code>.
	 * 
	 * @return The length of the vector.
	 */
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	/**
	 * Return whether the <code>Vector3f</code> is a unit vector, 
	 * meaning its norm ({@link #length()}) is equal to 1.
	 * 
	 * @return Whether the vector is a unit vector.
	 */
	public boolean isUnitVector() {
		return length() == 1;
	}
	
	/**
	 * Normalizes the <code>Vector3f</code>. It will will divide
     * the vector's components by its {@link #length()}, returning 
     * a unit vector.
	 * 
	 * @return The normalized vector (unit vector).
	 */
	public Vector3f normalize() {
        float length = x * x + y * y + z * z;
        if (length != 1f && length != 0f) {
            length = (float) (1.0f / Math.sqrt(length));
            x *= length;
            y *= length;
            z *= length;
        }
        return this;
	}
	
	/**
     * Calculates the squared distance between the <code>Vector3f</code> and 
     * the provided one.
     *
     * @param other The vector to determine the distance squared from.
     * @return 		The distance squared between the two vectors.
     */
    public float distanceSquared(ReadableVector3f other) {
        double dx = x - other.x();
        double dy = y - other.y();
        double dz = z - other.z();
        return (float) (dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Calculates the distance between the <code>Vector3f</code> and 
     * the provided one.
     *
     * @param other The vector to determine the distance from.
     * @return 		The distance between the two vectors.
     */
    public double distance(ReadableVector3f other) {
        return Math.sqrt(distanceSquared(other));
    }
    
	/**
	 * Return the X-component of the <code>ReadableVector3f</code>,
	 * as a single-precision float.
	 * 
	 * @return The X-coordinate value of the vector.
	 */
    @Override
    public float x() {
    	return x;
    }
    
    /**
	 * Return the Y-component of the <code>ReadableVector3f</code>,
	 * as a single-precision float.
	 * 
	 * @return The Y-coordinate value of the vector.
	 */
    @Override
    public float y() {
    	return y;
    }

    /**
	 * Return the Z-component of the <code>ReadableVector3f</code>,
	 * as a single-precision float.
	 * 
	 * @return The Z-coordinate value of the vector.
	 */
    @Override
    public float z() {
    	return z;
    }
	
	/**
	 * Set each component value of the <code>Vector3f</code> to 0.
	 * 
	 * @return The zero vector.
	 */
	public Vector3f zero() {
		x = y = z = 0;
		return this;
	}
	
	/**
	 * Set each component value of the <code>Vector3f</code> to their absolute equivalent.
	 * The method call {@link Math#abs(float)} on all components of the vector.
	 * 
	 * @return The vector with all absolute components.
	 */
	public Vector3f abs() {
		this.x = Math.abs(x);
		this.y = Math.abs(y);
		this.z = Math.abs(z);
		return this;
	}
	
	/**
	 * Sets all the components of the <code>Vector3f</code> to 0,
	 * before retrieving it from a pool.
	 */
	@Override
	public void reuse() {
		zero();
	}
	
	/**
	 * Sets all the components of the <code>Vector3f</code> to 0,
	 * before storing it into a pool.
	 */
	@Override
	public void free() {
		zero();
	}
	
	/**
	 * Clone the <code>Vector3f</code> to a new vector instance with the same
	 * components as the caller.
	 * 
	 * @return A cloned instance of the vector.
	 */
	@Override
	public Vector3f clone() {
		return new Vector3f(this);
	}
	
	/**
	 * Compare this vector with the provided <code>Vector3f</code>. It will first
	 * compare the X-component, then the Y-component and finally the Z-component.
	 * 
	 * @param  The other vector to compare with (not null).
	 * @return 0 &rarr; the 2 vectors are equal, negative &rarr; this vector comes before 
	 * 		   the other, negative &rarr; this vector comes after the other.
	 */
	@Override
	public int compareTo(Vector3f other) {
		int result = Float.compare(x, other.x);
		if(result == 0) {
			result = Float.compare(y, other.y);
		}
        if(result == 0) {
        	result = Float.compare(z, other.z);
        }
        
        return result;
	}
	
	/**
	 * Return a unique code for the <code>Vector3f</code> based on each component value.
	 * If two vectors are logically equivalent, they will return the same hash code value.
	 * 
	 * @return The hash code value of the vector.
	 */
	@Override
	public int hashCode() {
		return Float.floatToIntBits(x) * Float.floatToIntBits(y) * Float.floatToIntBits(z);
	}
	
	/**
	 * Return whether the provided object is equal to the <code>Vector3f</code>.
	 * It checks that all 3 component values are the same.
	 * 
	 * @param o The object to check equality with the vector.
	 * @return  Whether the two object are equal.
	 */
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		
		if(!(o instanceof Vector3f)) {
			return false;
		}
		
		Vector3f other = (Vector3f) o;
		if (Float.compare(x, other.x) != 0) {
			return false;
		}
		if (Float.compare(y, other.y) != 0) {
			return false;
		}
		
		return Float.compare(z, other.z) == 0;
	}
	
	/**
	 * Return the string representation of the <code>Vector3f</code> with the format:
	 * <code>Vector3f[ x, y, z ]</code>.
	 * 
	 * @return The string representation of the vector.
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[ " + x + ", " + y + ", " + z + " ]";
	}
}
