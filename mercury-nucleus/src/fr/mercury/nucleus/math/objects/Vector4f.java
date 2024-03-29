package fr.mercury.nucleus.math.objects;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.pool.Reusable;
import fr.mercury.nucleus.math.readable.ReadableVector4f;

/**
 * <code>Vector4f</code> is a mathematical utility class representing a vector with 4 components 
 * on the 4 axis (X;Y;Z;W) of a 4D space. It contains some utility methods for fast calculation 
 * or physical determination.
 * <p>
 * Example of representation: <i>
 * <li>A color </i></li>
 * <p>
 * 
 * @author GnosticOccultist
 */
public final class Vector4f implements ReadableVector4f, Comparable<Vector4f>, Reusable {

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
     * Instantiates a new <code>Vector4f</code> with 0 value for each component
     * {0,0,0,0}.
     */
    public Vector4f() {
        set(0, 0, 0, 0);
    }

    /**
     * Instantiates a new <code>Vector4f</code> with the provided components.
     * 
     * @param x The X-component of the vector.
     * @param y The Y-component of the vector.
     * @param z The Z-component of the vector.
     * @param w The W-component of the vector.
     */
    public Vector4f(float x, float y, float z, float w) {
        set(x, y, z, w);
    }

    /**
     * Instantiates a new <code>Vector4f</code> with the provided vector's
     * components.
     * 
     * @param other The other vector to get the components (not null).
     */
    public Vector4f(Vector4f other) {
        set(other);
    }

    /**
     * Set the provided components values to this <code>Vector4f</code> components.
     * 
     * @param x The X-component to copy from.
     * @param y The Y-component to copy from.
     * @param z The Z-component to copy from.
     * @param w The W-component of the vector.
     */
    public Vector4f set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Set the components values of the provided vector to this <code>Vector4f</code> 
     * components.
     * 
     * @param other The other vector to copy from (not null).
     */
    public Vector4f set(Vector4f other) {
        Validator.nonNull(other, "The vector cannot be null!");

        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
        return this;
    }

    /**
     * Add the provided components to this <code>Vector4f</code> components.
     * 
     * @param x The X-component to addition.
     * @param y The Y-component to addition.
     * @param z The Z-component to addition.
     * @param w The W-component to addition.
     * @return  The vector with its new components values.
     */
    public Vector4f add(float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    /**
     * Add the provided vector to this <code>Vector4f</code> components.
     * 
     * @param other The other vector to addition (not null).
     * @return      The vector with its new components values.
     */
    public Vector4f add(Vector4f other) {
        Validator.nonNull(other, "The vector cannot be null!");

        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        this.w += other.w;
        return this;
    }

    /**
     * Subtracts the provided components to this <code>Vector4f</code> components.
     * 
     * @param x The X-component to subtract.
     * @param y The Y-component to subtract.
     * @param z The Z-component to subtract.
     * @param w The W-component to subtract.
     * @return  The vector with its new components values.
     */
    public Vector4f sub(float x, float y, float z, float w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    /**
     * Subtracts the provided vector to this <code>Vector4f</code> components.
     * 
     * @param other The other vector to subtract (not null).
     * @return      The vector with its new components values.
     */
    public Vector4f sub(Vector4f other) {
        Validator.nonNull(other, "The vector cannot be null!");

        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
        this.w -= other.w;
        return this;
    }

    /**
     * Multiplies the provided components to this <code>Vector4f</code> components.
     * 
     * @param x The X-component to multiply.
     * @param y The Y-component to multiply.
     * @param z The Z-component to multiply.
     * @param w The W-component to multiply.
     * @return  The vector with its new components values.
     */
    public Vector4f mul(float x, float y, float z, float w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    /**
     * Multiplies the provided vector to this <code>Vector4f</code> components.
     * 
     * @param other The other vector to multiply (not null).
     * @return      The vector with its new components values.
     */
    public Vector4f mul(Vector4f other) {
        Validator.nonNull(other, "The vector cannot be null!");

        this.x *= other.x;
        this.y *= other.y;
        this.z *= other.z;
        this.w *= other.w;
        return this;
    }

    /**
     * Divides the provided components to this <code>Vector4f</code> components.
     * 
     * @param x The X-component to divide.
     * @param y The Y-component to divide.
     * @param z The Z-component to divide.
     * @param w The W-component to divide.
     * @return  The vector with its new components values.
     */
    public Vector4f div(float x, float y, float z, float w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    /**
     * Divides the provided vector to this <code>Vector4f</code> components.
     * 
     * @param other The other vector to divide (not null).
     * @return      The vector with its new components values.
     */
    public Vector4f div(Vector4f other) {
        Validator.nonNull(other, "The vector cannot be null!");

        this.x /= other.x;
        this.y /= other.y;
        this.z /= other.z;
        this.w /= other.w;
        return this;
    }

    /**
     * Normalizes the <code>Vector4f</code>. It will will divide the vector's
     * components by its {@link #length()}, returning a unit vector.
     * 
     * @return The normalized vector (unit vector).
     */
    public Vector4f normalize() {
        float length = x * x + y * y + z * z + w * w;
        if (length != 1f && length != 0f) {
            length = (float) (1.0f / Math.sqrt(length));
            x *= length;
            y *= length;
            z *= length;
            w *= length;
        }
        return this;
    }

    /**
     * Return the X-component of the <code>Vector4f</code>, as a single-precision
     * float.
     * 
     * @return The X-coordinate value of the vector.
     */
    @Override
    public float x() {
        return x;
    }

    /**
     * Sets the X-component of the <code>Vector4f</code>.
     * 
     * @param x The X-coordinate value of the vector.
     * @return  The vector for chaining purposes.
     */
    public Vector4f setX(float x) {
        this.x = x;
        return this;
    }

    /**
     * Return the Y-component of the <code>Vector4f</code>, as a single-precision
     * float.
     * 
     * @return The Y-coordinate value of the vector.
     */
    @Override
    public float y() {
        return y;
    }

    /**
     * Sets the Y-component of the <code>Vector4f</code>.
     * 
     * @param y The Y-coordinate value of the vector.
     * @return  The vector for chaining purposes.
     */
    public Vector4f setY(float y) {
        this.y = y;
        return this;
    }

    /**
     * Return the Z-component of the <code>Vector4f</code>, as a single-precision
     * float.
     * 
     * @return The Z-coordinate value of the vector.
     */
    @Override
    public float z() {
        return z;
    }

    /**
     * Sets the Z-component of the <code>Vector4f</code>.
     * 
     * @param z The Z-coordinate value of the vector.
     * @return  The vector for chaining purposes.
     */
    public Vector4f setZ(float z) {
        this.z = z;
        return this;
    }

    /**
     * Return the W-component of the <code>Vector4f</code>, as a single-precision
     * float.
     * 
     * @return The W-coordinate value of the vector.
     */
    @Override
    public float w() {
        return z;
    }

    /**
     * Sets the W-component of the <code>Vector4f</code>.
     * 
     * @param w The W-coordinate value of the vector.
     * @return  The vector for chaining purposes.
     */
    public Vector4f setW(float w) {
        this.z = w;
        return this;
    }

    /**
     * Set each component value of the <code>Vector4f</code> to 0.
     * 
     * @return The zero vector.
     */
    public Vector4f zero() {
        x = y = z = w = 0;
        return this;
    }

    /**
     * Sets all the components of the <code>Vector4f</code> to 0, before retrieving
     * it from a pool.
     */
    @Override
    public void reuse() {
        zero();
    }

    /**
     * Sets all the components of the <code>Vector4f</code> to 0, before storing it
     * into a pool.
     */
    @Override
    public void free() {
        zero();
    }

    /**
     * Clone the <code>Vector4f</code> to a new vector instance with the same
     * components as the caller.
     * 
     * @return A cloned instance of the vector.
     */
    @Override
    public Vector4f clone() {
        return new Vector4f(this);
    }

    /**
     * Compare this vector with the provided <code>Vector4f</code>. It will first
     * compare the X-component, then the Y-component and so on.
     * 
     * @param other The other vector to compare with (not null).
     * @return      0 &rarr; the 2 vectors are equal, negative &rarr; this vector comes
     *              before the other, negative &rarr; this vector comes after the other.
     */
    @Override
    public int compareTo(Vector4f other) {
        int result = Float.compare(x, other.x);
        if (result == 0) {
            result = Float.compare(y, other.y);
        }
        if (result == 0) {
            result = Float.compare(z, other.z);
        }
        if (result == 0) {
            result = Float.compare(w, other.w);
        }

        return result;
    }

    /**
     * Return a unique code for the <code>Vector4f</code> based on each component
     * value. If two vectors are logically equivalent, they will return the same
     * hash code value.
     * 
     * @return The hash code value of the vector.
     */
    @Override
    public int hashCode() {
        return Float.floatToIntBits(x) * Float.floatToIntBits(y) * Float.floatToIntBits(z) * Float.floatToIntBits(w);
    }

    /**
     * Return whether the provided object is equal to the <code>Vector4f</code>. It
     * checks that all 4 component values are the same.
     * 
     * @param o The object to check equality with the vector.
     * @return  Whether the two object are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ReadableVector4f)) {
            return false;
        }

        var other = (ReadableVector4f) o;
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

    /**
     * Return the string representation of the <code>Vector4f</code> with the
     * format: <code>Vector4f[ x, y, z, w ]</code>.
     * 
     * @return The string representation of the vector.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[ " + x + ", " + y + ", " + z + ", " + w + " ]";
    }
}