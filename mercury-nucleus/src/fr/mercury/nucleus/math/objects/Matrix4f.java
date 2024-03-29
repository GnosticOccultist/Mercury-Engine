package fr.mercury.nucleus.math.objects;

import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.pool.Reusable;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.readable.ReadableMatrix4f;
import fr.mercury.nucleus.renderer.AbstractRenderer;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.renderer.Camera.GraphicalProjectionMode;

/**
 * <code>Matrix4f</code> defines a 4x4 matrix, which is mainly used to store translation or rotational informations. The matrix can therefore 
 * be create from multiple ways and transformed into multiple mathematical object or {@link Buffer}.
 * The storage pattern is the following:
 * <pre> 
 * | Lx | Mx | Nx | 0 |
 * | Ly | My | Ny | 0 |
 * | Lz | Mz | Nz | 0 |
 * | Tx | Ty | Tz | 1 |
 * </pre>
 * <p>
 * For example, the translation component is stored in the 30, 31 and 32 slots, whereas rotation along the X, Y and Z axis is stored respectly
 * in 00, 10, 20; 01, 11, 21 and 02, 12, 22. The 33th component is called the homogeneous coordinate.
 * 
 * @author GnosticOccultist
 */
public final class Matrix4f implements ReadableMatrix4f, Reusable {

    /**
     * The <code>Matrix4f</code> identity &rarr; {@link #identity()}.
     */
    public static final ReadableMatrix4f IDENTITY_MATRIX = new Matrix4f();

    public float m00, m01, m02, m03;
    public float m10, m11, m12, m13;
    public float m20, m21, m22, m23;
    public float m30, m31, m32, m33;

    /**
     * Instantiates a new <code>Matrix4f</code> with the identity values
     * ({@link #identity()}).
     */
    public Matrix4f() {
        identity();
    }

    /**
     * Instantiates a new <code>Matrix4f</code> with the provided matrixes
     * components.
     * 
     * @param other The other matrix to get the components.
     */
    public Matrix4f(ReadableMatrix4f other) {
        set(other);
    }

    /**
     * Set the components values of the provided matrix to this <code>Matrix4f</code> components.
     * 
     * @param other The other matrix to copy from (not null).
     * @return      The matrix with its new components values, used for chaining methods.
     */
    public Matrix4f set(ReadableMatrix4f other) {
        Validator.nonNull(other, "The matrix cannot be null!");

        m00 = other.m00();
        m01 = other.m01();
        m02 = other.m02();
        m03 = other.m03();
        m10 = other.m10();
        m11 = other.m11();
        m12 = other.m12();
        m13 = other.m13();
        m20 = other.m20();
        m21 = other.m21();
        m22 = other.m22();
        m23 = other.m23();
        m30 = other.m30();
        m31 = other.m31();
        m32 = other.m32();
        m33 = other.m33();

        return this;
    }

    /**
     * Set the provided components values to this <code>Matrix4f</code> components.
     * 
     * @param m00 The desired row 0 - column 0 component.
     * @param m01 The desired row 0 - column 1 component.
     * @param m02 The desired row 0 - column 2 component.
     * @param m03 The desired row 0 - column 3 component.
     * @param m10 The desired row 1 - column 0 component.
     * @param m11 The desired row 1 - column 1 component.
     * @param m12 The desired row 1 - column 2 component.
     * @param m13 The desired row 1 - column 3 component.
     * @param m20 The desired row 2 - column 0 component.
     * @param m21 The desired row 2 - column 1 component.
     * @param m22 The desired row 2 - column 2 component.
     * @param m23 The desired row 2 - column 3 component.
     * @param m30 The desired row 3 - column 0 component.
     * @param m31 The desired row 3 - column 1 component.
     * @param m32 The desired row 3 - column 2 component.
     * @param m33 The desired row 3 - column 3 component.
     * @return    The matrix with its new components values, used for chaining methods.
     */
    public Matrix4f set(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23, float m30, float m31, float m32, float m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;

        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;

        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;

        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;

        return this;
    }

    /**
     * Set the <code>Matrix4f</code> to its identity values. It is all zeros except
     * the diagonal values which are set to 1.
     * 
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
     * Set the components of the <code>Matrix4f</code> to represent a view matrix based on the
     * provided vectors.
     * <p>
     * This method is used by the {@link Camera} to update its view matrix and send it as uniform
     * through an {@link AbstractRenderer} implementation.
     * 
     * @param location  The location of the view (not null).
     * @param left      The direction of the left-side view vector (not null).
     * @param up        The direction of the up-side view vector (not null).
     * @param direction The view direction vector (not null).
     * @return          The modified matrix for chaining purposes.
     */
    public Matrix4f view(Vector3f location, Vector3f left, Vector3f up, Vector3f direction) {
        identity();
        m00 = -left.x();
        m10 = -left.y();
        m20 = -left.z();

        m01 = up.x();
        m11 = up.y();
        m21 = up.z();

        m02 = -direction.x();
        m12 = -direction.y();
        m22 = -direction.z();

        m30 = left.dot(location);
        m31 = -up.dot(location);
        m32 = direction.dot(location);

        return this;
    }

    /**
     * Build a projection <code>Matrix4f</code> matching the given {@link GraphicalProjectionMode} 
     * and using each provided frustum plane distance from the {@link Camera}.
     * <p>
     * It is used in the shader to adjust an object depending on the window size.
     * 
     * @param mode   The projection mode to use for the matrix (not null).
     * @param near   The near frustum plane distance from the camera.
     * @param far    The far frustum plane distance from the camera.
     * @param left   The left frustum plane distance from the camera.
     * @param right  The right frustum plane distance from the camera.
     * @param top    The top frustum plane distance from the camera.
     * @param bottom The bottom frustum plane distance from the camera.
     * @return       The modified projection matrix (not null).
     */
    public Matrix4f projection(Camera.GraphicalProjectionMode mode, float near, float far, float left, float right,
            float top, float bottom) {
        Validator.nonNull(mode, "The projection mode can't be null!");

        if (mode == GraphicalProjectionMode.PERSPECTIVE) {
            return perspective(near, far, left, right, top, bottom);
        } else if (mode == GraphicalProjectionMode.ORTHOGRAPHIC) {
            return orthographic(near, far, left, right, top, bottom);
        } else {
            throw new IllegalArgumentException("Invalid projection mode: " + mode);
        }
    }

    /**
     * Build the perspective projection <code>Matrix4f</code> with each frustum plane distance 
     * from the {@link Camera}.
     * <p>
     * It is used in the shader to adjust an object depending on the window size.
     * 
     * @param near   The near frustum plane distance from the camera.
     * @param far    The far frustum plane distance from the camera.
     * @param left   The left frustum plane distance from the camera.
     * @param right  The right frustum plane distance from the camera.
     * @param top    The top frustum plane distance from the camera.
     * @param bottom The bottom frustum plane distance from the camera.
     * @return       The modified matrix with perspective projection (not null).
     */
    public Matrix4f perspective(float near, float far, float left, float right, float top, float bottom) {
        identity();

        m00 = (2.0f * near) / (right - left);
        m11 = (2.0f * near) / (top - bottom);
        m20 = (right + left) / (right - left);
        m21 = (top + bottom) / (top - bottom);
        m22 = -(far + near) / (far - near);
        m23 = -1.0F;
        m32 = -(2.0F * far * near) / (far - near);
        m33 = 0.0F;
        
        return this;
    }

    /**
     * Build the orthographic projection <code>Matrix4f</code> with each frustum plane distance 
     * from the {@link Camera}.
     * <p>
     * It is used in the shader to adjust an object depending on the window size.
     * 
     * @param near   The near frustum plane distance from the camera.
     * @param far    The far frustum plane distance from the camera.
     * @param left   The left frustum plane distance from the camera.
     * @param right  The right frustum plane distance from the camera.
     * @param top    The top frustum plane distance from the camera.
     * @param bottom The bottom frustum plane distance from the camera.
     * @return       The modified matrix with orthographic projection (not null).
     */
    public Matrix4f orthographic(float near, float far, float left, float right, float top, float bottom) {
        identity();

        m00 = 2.0f / (right - left);
        m11 = 2.0f / (top - bottom);
        m22 = -2.0f / (far - near);
        m30 = -((right + left) / (right - left));
        m31 = -((top + bottom) / (top - bottom));
        m32 = -((far + near) / (far - near));
        
        return this;
    }

    /**
     * Populates the given {@link FloatBuffer} with the data from the <code>Matrix4f</code> in row-major order.
     * <p>
     * The method is using relative put method, meaning the float data is written at the current buffer's position 
     * and the position is incremented by 16.
     * <p>
     * The populated buffer can be used safely to transfer data to shaders as mat4 uniforms.
     * 
     * @param store The buffer to populate with the data (not null).
     * @return      The given store populated with the matrix data.
     * 
     * @throws BufferOverflowException Thrown if there isn't enough space to write all 16 floats.
     * 
     * @see #populate(FloatBuffer, boolean)
     */
    @Override
    public FloatBuffer populate(FloatBuffer store) {
        Validator.nonNull(store, "The float buffer can't be null!");
        return populate(store, false);
    }

    /**
     * Populates the given {@link FloatBuffer} with the data from the <code>Matrix4f</code>.
     * <p>
     * The method is using relative put method, meaning the float data is written at the current buffer's position 
     * and the position is incremented by 16.
     * <p>
     * The populated buffer can be used safely to transfer data to shaders as mat4 uniforms using a row-major order.
     * 
     * @param store       The buffer to populate with the data (not null).
     * @param columnMajor Whether to write the data in column or row-major order.
     * @return            The given store populated with the matrix data.
     * 
     * @throws BufferOverflowException Thrown if there isn't enough space to write all 16 floats.
     * 
     * @see #populate(FloatBuffer)
     */
    public FloatBuffer populate(FloatBuffer store, boolean columnMajor) {
        Validator.nonNull(store, "The float buffer can't be null!");

        if (columnMajor) {
            store.put(m00);
            store.put(m10);
            store.put(m20);
            store.put(m30);
            store.put(m01);
            store.put(m11);
            store.put(m21);
            store.put(m31);
            store.put(m02);
            store.put(m12);
            store.put(m22);
            store.put(m32);
            store.put(m03);
            store.put(m13);
            store.put(m23);
            store.put(m33);
        } else {
            store.put(m00);
            store.put(m01);
            store.put(m02);
            store.put(m03);
            store.put(m10);
            store.put(m11);
            store.put(m12);
            store.put(m13);
            store.put(m20);
            store.put(m21);
            store.put(m22);
            store.put(m23);
            store.put(m30);
            store.put(m31);
            store.put(m32);
            store.put(m33);
        }

        return store;
    }

    /**
     * Fill the float array with the <code>Matrix4f</code> values. It can be ordered in column 
     * major order or row major ordered.
     * 
     * @param array       The float array to fill with the matrix (length left
     *                    &ge;16).
     * @param columnMajor Whether to fill in column major order or in row major
     *                    order.
     */
    public void fillFloatArray(float[] array, boolean columnMajor) {
        if (columnMajor) {
            array[0] = m00;
            array[1] = m10;
            array[2] = m20;
            array[3] = m30;
            array[4] = m01;
            array[5] = m11;
            array[6] = m21;
            array[7] = m31;
            array[8] = m02;
            array[9] = m12;
            array[10] = m22;
            array[11] = m32;
            array[12] = m03;
            array[13] = m13;
            array[14] = m23;
            array[15] = m33;
        } else {
            array[0] = m00;
            array[1] = m01;
            array[2] = m02;
            array[3] = m03;
            array[4] = m10;
            array[5] = m11;
            array[6] = m12;
            array[7] = m13;
            array[8] = m20;
            array[9] = m21;
            array[10] = m22;
            array[11] = m23;
            array[12] = m30;
            array[13] = m31;
            array[14] = m32;
            array[15] = m33;
        }
    }

    /**
     * Reads value from the provided {@link FloatBuffer} and store them into the <code>Matrix4f</code> 
     * as row major order.
     * 
     * @param fb The float buffer to read data from (size &ge;16).
     * @return   The filled matrix with the data of the float buffer.
     */
    public Matrix4f readFloatBuffer(FloatBuffer fb) {
        return readFloatBuffer(fb, false);
    }

    /**
     * Reads value from the provided {@link FloatBuffer} and store them into the <code>Matrix4f</code>.
     * 
     * @param fb          The float buffer to read data from (size &ge;16).
     * @param columnMajor Whether to fill the matrix with column major data or row
     *                    major data.
     * @return            The filled matrix with the data of the float buffer.
     */
    public Matrix4f readFloatBuffer(FloatBuffer fb, boolean columnMajor) {
        Validator.nonNull(fb);

        if (columnMajor) {
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
     * Multiplies the <code>Matrix4f</code> with the provided one. The result is stored inside the second 
     * provided matrix or a new one if null.
     * 
     * @param other The other matrix to do the multiplication.
     * @param store The matrix to store the result.
     * @return      The resulting matrix.
     */
    public Matrix4f mult(ReadableMatrix4f other, Matrix4f store) {
        if (store == null) {
            store = new Matrix4f();
        }

        float temp00, temp01, temp02, temp03;
        float temp10, temp11, temp12, temp13;
        float temp20, temp21, temp22, temp23;
        float temp30, temp31, temp32, temp33;

        temp00 = m00 * other.m00() + m01 * other.m10() + m02 * other.m20() + m03 * other.m30();
        temp01 = m00 * other.m01() + m01 * other.m11() + m02 * other.m21() + m03 * other.m31();
        temp02 = m00 * other.m02() + m01 * other.m12() + m02 * other.m22() + m03 * other.m32();
        temp03 = m00 * other.m03() + m01 * other.m13() + m02 * other.m23() + m03 * other.m33();

        temp10 = m10 * other.m00() + m11 * other.m10() + m12 * other.m20() + m13 * other.m30();
        temp11 = m10 * other.m01() + m11 * other.m11() + m12 * other.m21() + m13 * other.m31();
        temp12 = m10 * other.m02() + m11 * other.m12() + m12 * other.m22() + m13 * other.m32();
        temp13 = m10 * other.m03() + m11 * other.m13() + m12 * other.m23() + m13 * other.m33();

        temp20 = m20 * other.m00() + m21 * other.m10() + m22 * other.m20() + m23 * other.m30();
        temp21 = m20 * other.m01() + m21 * other.m11() + m22 * other.m21() + m23 * other.m31();
        temp22 = m20 * other.m02() + m21 * other.m12() + m22 * other.m22() + m23 * other.m32();
        temp23 = m20 * other.m03() + m21 * other.m13() + m22 * other.m23() + m23 * other.m33();

        temp30 = m30 * other.m00() + m31 * other.m10() + m32 * other.m20() + m33 * other.m30();
        temp31 = m30 * other.m01() + m31 * other.m11() + m32 * other.m21() + m33 * other.m31();
        temp32 = m30 * other.m02() + m31 * other.m12() + m32 * other.m22() + m33 * other.m32();
        temp33 = m30 * other.m03() + m31 * other.m13() + m32 * other.m23() + m33 * other.m33();

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
     * Set the scaling components of the <code>Matrix4f</code> to the ones specified
     * in the provided <code>Vector3f</code>.
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
     * Apply a scale to the <code>Matrix4f</code> using the provided {@link Vector3f}.
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
     * Return a scaling {@link Vector3f} from the <code>Matrix4f</code> components.
     * 
     * @param store The vector to store the scale into.
     * @return      The scaling vector.
     */
    public Vector3f getScale(Vector3f store) {
        float scaleX = (float) Math.sqrt(m00 * m00 + m10 * m10 + m20 * m20);
        float scaleY = (float) Math.sqrt(m01 * m01 + m11 * m11 + m21 * m21);
        float scaleZ = (float) Math.sqrt(m02 * m02 + m12 * m12 + m22 * m22);
        store.set(scaleX, scaleY, scaleZ);
        return store;
    }

    /**
     * Converts the <code>Matrix4f</code> to a {@link Matrix3f} using the provided one.
     * 
     * @param store The store for the result or null for a new instance.
     * @return      A 3x3 matrix representation of the matrix.
     */
    public Matrix3f toMatrix3f(Matrix3f store) {
        var result = store == null ? new Matrix3f() : store;

        result.m00 = m00;
        result.m01 = m01;
        result.m02 = m02;
        result.m10 = m10;
        result.m11 = m11;
        result.m12 = m12;
        result.m20 = m20;
        result.m21 = m21;
        result.m22 = m22;

        return result;
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
    public float m03() {
        return m03;
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
    public float m13() {
        return m13;
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
    public float m23() {
        return m23;
    }

    @Override
    public float m30() {
        return m30;
    }

    @Override
    public float m31() {
        return m31;
    }

    @Override
    public float m32() {
        return m32;
    }

    @Override
    public float m33() {
        return m33;
    }

    /**
     * Sets all the components of the <code>Matrix4f</code> to the
     * {@link #identity()}, before retrieving it from a pool.
     */
    @Override
    public void reuse() {
        identity();
    }

    /**
     * Sets all the components of the <code>Matrix4f</code> to the
     * {@link #identity()}, before storing it into a pool.
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

        if (!(o instanceof ReadableMatrix4f)) {
            return false;
        }

        var comp = (ReadableMatrix4f) o;
        if (Float.compare(m00, comp.m00()) != 0) {
            return false;
        }
        if (Float.compare(m01, comp.m01()) != 0) {
            return false;
        }
        if (Float.compare(m02, comp.m02()) != 0) {
            return false;
        }
        if (Float.compare(m03, comp.m03()) != 0) {
            return false;
        }

        if (Float.compare(m10, comp.m10()) != 0) {
            return false;
        }
        if (Float.compare(m11, comp.m11()) != 0) {
            return false;
        }
        if (Float.compare(m12, comp.m12()) != 0) {
            return false;
        }
        if (Float.compare(m13, comp.m13()) != 0) {
            return false;
        }

        if (Float.compare(m20, comp.m20()) != 0) {
            return false;
        }
        if (Float.compare(m21, comp.m21()) != 0) {
            return false;
        }
        if (Float.compare(m22, comp.m22()) != 0) {
            return false;
        }
        if (Float.compare(m23, comp.m23()) != 0) {
            return false;
        }

        if (Float.compare(m30, comp.m30()) != 0) {
            return false;
        }
        if (Float.compare(m31, comp.m31()) != 0) {
            return false;
        }
        if (Float.compare(m32, comp.m32()) != 0) {
            return false;
        }
        if (Float.compare(m33, comp.m33()) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        var result = new StringBuffer(getClass().getSimpleName() + "\n[\n");
        result.append(' ');
        result.append(m00);
        result.append(' ');
        result.append(m01);
        result.append(' ');
        result.append(m02);
        result.append(' ');
        result.append(m03);
        result.append(" \n");

        result.append(' ');
        result.append(m10);
        result.append(' ');
        result.append(m11);
        result.append(' ');
        result.append(m12);
        result.append(' ');
        result.append(m13);
        result.append(" \n");

        result.append(' ');
        result.append(m20);
        result.append(' ');
        result.append(m21);
        result.append(' ');
        result.append(m22);
        result.append(' ');
        result.append(m23);
        result.append(" \n");

        result.append(' ');
        result.append(m30);
        result.append(' ');
        result.append(m31);
        result.append(' ');
        result.append(m32);
        result.append(' ');
        result.append(m33);
        result.append(" \n");

        result.append(']');
        return result.toString();
    }
}
