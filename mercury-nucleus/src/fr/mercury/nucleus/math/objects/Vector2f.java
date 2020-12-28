package fr.mercury.nucleus.math.objects;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.pool.Reusable;
import fr.mercury.nucleus.math.readable.ReadableVector2f;

/**
 * <code>Vector2f</code> is a mathematical utility class representing a vector with
 * 2 components on the 2 axis (X;Y) of a 2D space. It contains some utility methods
 * for fast calculation or physical determination.
 * <p>
 * Example of representation:
 * <i><li>A texture coordinate
 * <li>A plane
 * </i></li>
 * <p>
 * 
 * @author GnosticOccultist
 */
public final class Vector2f implements  ReadableVector2f, Comparable<Vector2f>, Reusable {
	
	/**
	 * The X-component of the vector.
	 */
	public float x;
	/**
	 * The Y-component of the vector.
	 */
	public float y;
	
	/**
	 * Instantiates a new <code>Vector2f</code> with 0 value for 
	 * each component {0,0}.
	 */
	public Vector2f() {
		set(0, 0);
	}
	
	/**
	 * Instantiates a new <code>Vector2f</code> with the provided
	 * components.
	 * 
	 * @param x The X-component of the vector.
	 * @param y The Y-component of the vector.
	 */
	public Vector2f(float x, float y) {
		set(x, y);
	}
	
	/**
	 * Instantiates a new <code>Vector2f</code> with the provided
	 * vector's components.
	 * 
	 * @param other The other vector to get the components.
	 */
	public Vector2f(Vector2f other) {
		set(other);
	}
	
	/**
	 * Set the provided components values to this <code>Vector2f</code> 
	 * components.
	 * 
	 * @param x The X-component to copy from.
	 * @param y The Y-component to copy from.
	 */
	public Vector2f set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	/**
	 * Set the components values of the provided vector to this 
	 * <code>Vector2f</code> components.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param other The other vector to copy from.
	 */
	public Vector2f set(Vector2f other) {
		Validator.nonNull(other, "The vector cannot be null!");
		
		this.x = other.x;
		this.y = other.y;
		return this;
	}
	
	/**
	 * Add the provided components to this <code>Vector2f</code> components.
	 * 
	 * @param x The X-component to addition.
	 * @param y The Y-component to addition.
	 * 
	 * @return	The vector with its new components values.
	 */
	public Vector2f add(float x, float y) {
		this.x += x;
        this.y += y;
        return this;
	}
	
	/**
	 * Add the provided vector to this <code>Vector2f</code> components.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param other The other vector to addition.
	 * 
	 * @return		The vector with its new components values.
	 */
	public Vector2f add(Vector2f other) {
        Validator.nonNull(other, "The vector cannot be null!");
		
		this.x += other.x;
        this.y += other.y;
        return this;
	}
	
	/**
	 * Subtracts the provided components to this <code>Vector2f</code> components.
	 * 
	 * @param x The X-component to subtract.
	 * @param y The Y-component to subtract.
	 * 
	 * @return	The vector with its new components values.
	 */
	public Vector2f sub(float x, float y) {
		this.x -= x;
        this.y -= y;
        return this;
	}
	
	/**
	 * Subtracts the provided vector to this <code>Vector2f</code> components.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param other The other vector to subtract.
	 * 
	 * @return		The vector with its new components values.
	 */
	public Vector2f sub(Vector2f other) {
        Validator.nonNull(other, "The vector cannot be null!");
		
		this.x -= other.x;
        this.y -= other.y;
        return this;
	}
	
	/**
	 * Multiplies the provided components to this <code>Vector2f</code> components.
	 * 
	 * @param x The X-component to multiply.
	 * @param y The Y-component to multiply.
	 * 
	 * @return 	The vector with its new components values. 
	 */
    public Vector2f mul(float x, float y) {
    	this.x *= x;
        this.y *= y;
        return this;
    }
    
    /**
	 * Multiplies the provided vector to this <code>Vector2f</code> components.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param other The other vector to multiply.
	 * 
	 * @return 		The vector with its new components values. 
	 */
    public Vector2f mul(Vector2f other) {
    	Validator.nonNull(other, "The vector cannot be null!");
    	
    	this.x *= other.x;
        this.y *= other.y;
        return this;
    }
    
    /**
	 * Divides the provided components to this <code>Vector2f</code> components.
	 * 
	 * @param x The X-component to divide.
	 * @param y The Y-component to divide.
	 * 
	 * @return 	The vector with its new components values. 
	 */
    public Vector2f div(float x, float y) {
    	this.x /= x;
        this.y /= y;
        return this;
    }
    
    /**
	 * Divides the provided vector to this <code>Vector2f</code> components.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param other The other vector to divide.
	 * 
	 * @return 		The vector with its new components values. 
	 */
    public Vector2f div(Vector2f other) {
    	Validator.nonNull(other, "The vector cannot be null!");
    	
    	this.x /= other.x;
        this.y /= other.y;
        return this;
    }
	
	/**
	 * Normalizes the <code>Vector2f</code>. It will will divide
     * the vector's components by its {@link #length()}, returning 
     * a unit vector.
	 * 
	 * @return The normalized vector (unit vector).
	 */
	public Vector2f normalize() {
        float length = x * x + y * y;
        if (length != 1f && length != 0f) {
            length = (float) (1.0f / Math.sqrt(length));
            x *= length;
            y *= length;
        }
        return this;
	}
	
	/**
	 * Return the X-component of the <code>Vector2f</code>, as a single-precision float.
	 * 
	 * @return The X-coordinate value of the vector.
	 */
	@Override
	public float x() {
		return x;
	}
	
	/**
	 * Return the Y-component of the <code>Vector2f</code>, as a single-precision float.
	 * 
	 * @return The Y-coordinate value of the vector.
	 */
	@Override
	public float y() {
		return y;
	}
	
	/**
	 * Set each component value of the <code>Vector2f</code> to 0.
	 * 
	 * @return The zero vector.
	 */
	public Vector2f zero() {
		x = y = 0;
		return this;
	}
	
	/**
	 * Sets all the components of the <code>Vector2f</code> to 0,
	 * before retrieving it from a pool.
	 */
	@Override
	public void reuse() {
		zero();
	}
	
	/**
	 * Sets all the components of the <code>Vector2f</code> to 0,
	 * before storing it into a pool.
	 */
	@Override
	public void free() {
		zero();
	}
	
	/**
	 * Clone the <code>Vector2f</code> to a new vector instance with the same
	 * components as the caller.
	 * 
	 * @return A cloned instance of the vector.
	 */
	@Override
	public Vector2f clone() {
		return new Vector2f(this);
	}
	
	/**
	 * Compare this vector with the provided <code>Vector2f</code>. It will first
	 * compare the X-component, then the Y-component.
	 * 
	 * @param  The other vector to compare with (not null).
	 * @return 0 &rarr; the 2 vectors are equal, negative &rarr; this vector comes before 
	 * 		   the other, negative &rarr; this vector comes after the other.
	 */
	@Override
	public int compareTo(Vector2f other) {
		int result = Float.compare(x, other.x);
		if(result == 0) {
			result = Float.compare(y, other.y);
		}
		
        return result;
	}
	
	/**
	 * Return a unique code for the <code>Vector2f</code> based on each component value.
	 * If two vectors are logically equivalent, they will return the same hash code value.
	 * 
	 * @return The hash code value of the vector.
	 */
	@Override
	public int hashCode() {
		return Float.floatToIntBits(x) * Float.floatToIntBits(y);
	}
	
	/**
	 * Return whether the provided object is equal to the <code>Vector2f</code>.
	 * It checks that all 2 component values are the same.
	 * 
	 * @param o The object to check equality with the vector.
	 * @return  Whether the two object are equal.
	 */
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		
		if(!(o instanceof ReadableVector2f)) {
			return false;
		}
		
		var other = (ReadableVector2f) o;
		if (Float.compare(x, other.x()) != 0) {
			return false;
		}
		
		return Float.compare(y, other.y()) == 0;
	}
	
	/**
	 * Return the string representation of the <code>Vector2f</code> with the format:
	 * <code>Vector2f[ x, y ]</code>.
	 * 
	 * @return The string representation of the vector.
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[ " + x + ", " + y + " ]";
	}
}
