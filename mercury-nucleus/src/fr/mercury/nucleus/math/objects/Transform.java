package fr.mercury.nucleus.math.objects;

import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.pool.Reusable;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.readable.ReadableMatrix3f;
import fr.mercury.nucleus.math.readable.ReadableQuaternion;
import fr.mercury.nucleus.math.readable.ReadableTransform;
import fr.mercury.nucleus.math.readable.ReadableVector3f;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;

/**
 * <code>Transform</code> is a mathematical object representing a translation, a rotation 
 * and a scale for a scene-graph object in a 3D space.
 * <p>
 * It is mostly used to compute the useful matrices for the shaders such as transform matrix.
 * 
 * @author GnosticOccultist
 */
public final class Transform implements ReadableTransform, Comparable<Transform>, Reusable {
	
	/**
	 * The <code>Transform</code> identity &rarr; Translation: [0,0,0] | Rotation: [0,0,0,1] | Scale: [1,1,1].
	 */
	public static final ReadableTransform IDENTITY_TRANSFORM = new Transform();
	
	/**
	 * The translation of the object.
	 */
	private final Vector3f translation;
	/**
	 * The rotation of the object.
	 */
	private final Matrix3f rotation;
	/**
	 * The scale of the object.
	 */
	private final Vector3f scale;
	/**
	 * The matrix containing the model transform.
	 */
	private final Matrix4f transformMatrix;
	/**
	 * Whether the transform is an identity one.
	 */
	private boolean identity;
	/**
	 * Whether the matrix used is only rotation or combines a scaling as well.
	 */
	private boolean rotationMatrix;
	/**
	 * Whether the transform is uniformly scaled.
	 */
	private boolean uniformScale;
	
	/**
	 * Instantiates a new <code>Transform</code> with identity values.
	 * <p>
	 * The translation: {0,0,0}.
	 * <br>
	 * The rotation: <br>{1,0,0}<br>{0,1,0}<br>{0,0,1}
	 * <br>
	 * The scale: {1,1,1}.
	 */
	public Transform() {
		this.translation = new Vector3f();
		this.rotation = new Matrix3f();
		this.scale = new Vector3f(1, 1, 1);
		this.transformMatrix = new Matrix4f();
		
		this.identity = true;
		this.rotationMatrix = true;
		this.uniformScale = true;
	}
	
	/**
	 * Instantiates a new <code>Transform</code> with the provided 
	 * translation vector and identity values for the rotation and the scale.
	 * <p>
	 * The rotation: {0,0,0,1}.
	 * The scale: {1,1,1}.
	 */
	public Transform(Vector3f translation) {
		this(translation, new Matrix3f(), new Vector3f(1, 1, 1));
	}
	
	/**
	 * Instantiates a new <code>Transform</code> with the provided 
	 * translation vector and rotation quaternion and identity values for the scale.
	 * <p>
	 * The scale: {1,1,1}.
	 */
	public Transform(Vector3f translation, Matrix3f rotation) {
		this(translation, rotation, new Vector3f(1, 1, 1));
	}
	
	/**
	 * Instantiates a new <code>Transform</code> with the provided 
	 * translation vector, rotation quaternion and scaling vector.
	 */
	public Transform(Vector3f translation, Matrix3f rotation, Vector3f scale) {
		this.translation = new Vector3f(translation);
		this.rotation = new Matrix3f(rotation);
		this.scale = new Vector3f(scale);
		this.transformMatrix = new Matrix4f();
		
		update(false);
	}
	
	/**
	 * Sets the components values of the provided transform to this <code>Transform</code> 
	 * components.
	 * 
	 * @param other The other transform to copy from (not null).
	 * @return		The transform with copied components.
	 */
	public Transform set(ReadableTransform other) {
		this.translation.set(other.getTranslation());
		this.rotation.set(other.getRotation());
		this.scale.set(other.getScale());
		this.identity = other.isIdentity();
		this.rotationMatrix = other.isRotationMatrix();
		this.uniformScale = other.isUniformScale();
		return this;
	}
	
	/**
	 * Return the translation vector of the <code>Transform</code>.
	 * 
	 * @return The translation vector.
	 */
	@Override
	public ReadableVector3f getTranslation() {
		return translation;
	}
	
	/**
	 * Set the translation vector of the <code>Transform</code> to the provided vector.
	 * 
	 * @param translation The translation vector (not null).
	 * @return			  The transform with the new translation vector.
	 */
	public Transform setTranslation(ReadableVector3f translation) {
		this.translation.set(translation);
		this.identity = identity && translation.equals(Vector3f.ZERO);
		return this;
	}
	
	/**
	 * Set the translation vector of the <code>Transform</code> to the provided components.
	 *
	 * @param x The X-component to copy from.
	 * @param y The Y-component to copy from.
	 * @param z The Z-component to copy from.
	 * 
	 * @return  The transform with the new translation vector.
	 */
	public Transform setTranslation(float x, float y, float z) {
		this.translation.set(x, y, z);
		this.identity = identity && x == 0.0F && y == 0.0F && z == 0.0F;
		return this;
	}
	
	/**
	 * Translate the translation vector of the <code>Transform</code> by the provided vector.
	 * 
	 * @param translation The translation vector to addition (not null).
	 *
	 * @return 			  The updated transform. 
	 */
	public Transform translate(ReadableVector3f translation) {
		this.translation.add(translation);
		this.identity = identity && translation.equals(Vector3f.ZERO);
		return this;
	}
	
	/**
	 * Translate the translation vector of the <code>Transform</code> by the provided components.
	 * 
	 * @param x The X-component to increase.
	 * @param y The Y-component to increase.
	 * @param z The Z-component to increase.
	 *
	 * @return The updated transform. 
	 */
	public Transform translate(float x, float y, float z) {
		this.translation.add(x, y, z);
		this.identity = identity && x == 0.0F && y == 0.0F && z == 0.0F;
		return this;
	}
	
	/**
	 * Return the rotation matrix of the <code>Transform</code>.
	 * 
	 * @return The rotation quaternion.
	 */
	@Override
	public ReadableMatrix3f getRotation() {
		return rotation;
	}
	
	/**
	 * Sets the matrix of the <code>Transform</code> to the provided one. If the given matrix isn't purely rotational 
	 * it will provide the scale of the transform {@link #setScale(float)} or its variants will throw an error.
	 * 
	 * @param rotation The rotation matrix if orthonormal, otherwise the rotation and the scale (not null).
	 * @return		   The updated transform for chaining purposes.
	 */
	public Transform setRotation(ReadableMatrix3f rotation) {
		this.rotation.set(rotation);
		
		update(false);
		return this;
	}
	
	/**
	 * Sets the matrix of the <code>Transform</code> to the provided quaternion value. The scale can therefore be defined
	 * using {@link #setScale(float)} or its variants.
	 * 
	 * @param rotation The rotation matrix if orthonormal, otherwise the rotation and the scale (not null).
	 * @return		   The updated transform for chaining purposes.
	 */
	public Transform setRotation(ReadableQuaternion rotation) {
		this.rotation.set(rotation);
		
		update(true);
		return this;
	}
	
	/**
	 * Sets the matrix components of the <code>Transform</code> to the provided euler angles in radians. 
	 * 
	 * @param x The X axis angle to apply in radians (aka yaw).
     * @param y The Y axis angle to apply in radians (aka roll).
     * @param z The Z axis angle to apply in radians (aka pitch).
	 * @return  The updated transform for chaining purposes.
	 */
	public Transform setRotation(float x, float y, float z) {
		this.rotation.fromAngles(x, y, z);
		
		update(false);
		return this;
	}
	
	/**
	 * Rotates the matrix of the <code>Transform</code> to the provided components for each axis.
	 * 
	 * @param x The X-axis angle of rotation in radians.
	 * @param y The Y-axis angle of rotation in radians.
	 * @param z The Z-axis angle of rotation in radians.
	 * @return	The updated transform for chaining purposes.
	 */
	public Transform rotate(float x, float y, float z) {
		this.rotation.rotateX(x).rotateY(y).rotateZ(z);
		
		update(false);
		return this;
	}
	
	/**
	 * Return the scaling vector of the <code>Transform</code>.
	 * 
	 * @return The scaling vector.
	 */
	@Override
	public ReadableVector3f getScale() {
		return scale;
	}
	
	/**
	 * Set the scale vector of the <code>Transform</code> to the provided vector.
	 * 
	 * @param scale The scale vector (not null, &gt;0).
	 * @return		The transform with the new scaling vector, for chaining purposes.
	 */
	public Transform setScale(ReadableVector3f scale) {
		if(!rotationMatrix) {
            throw new IllegalStateException("The scale as already been set by the 3x3 rotation matrix, "
            		+ "please use an orthonormal or a quaternion to set a pure rotation!");
        }

        if(scale.x() <= 0.0F && scale.y() <= 0.0F && scale.z() <= 0.0F) {
        	throw new IllegalArgumentException("The scale can't be negative or null !");
        }
        
		this.scale.set(scale);
		this.identity = identity && scale.x() == 1.0 && scale.y() == 1.0 && scale.z() == 1.0;
		this.uniformScale = scale.x() == scale.y() && scale.y() == scale.z();
		return this;
	}
	
	/**
	 * Set the scale vector of the <code>Transform</code> to the provided components.
	 *
	 * @param x The X-component to copy from (&gt;0).
	 * @param y The Y-component to copy from (&gt;0).
	 * @param z The Z-component to copy from (&gt;0).
	 * 
	 * @return  The transform with the new scaling vector, for chaining purposes.
	 */
	public Transform setScale(float x, float y, float z) {
		if(!rotationMatrix) {
            throw new IllegalStateException("The scale as already been set by the 3x3 rotation matrix, "
            		+ "please use an orthonormal or a quaternion to set a pure rotation!");
        }

        if(x <= 0.0F && y <= 0.0F && z <= 0.0F) {
        	throw new IllegalArgumentException("The scale can't be negative or null !");
        }
        
		this.scale.set(x, y, z);
		this.identity = false;
		this.uniformScale = x == y && y == z;
		return this;
	}
	
	/**
	 * Set the scale vector of the <code>Transform</code> to the provided components.
	 *
	 * @param scale The desired scale of the transform (&gt;0).
	 * 
	 * @return  	The transform with the new scaling vector, for chaining purposes.
	 */
	public Transform setScale(float scale) {
		if(!rotationMatrix) {
            throw new IllegalStateException("The scale as already been set by the 3x3 rotation matrix, "
            		+ "please use an orthonormal or a quaternion to set a pure rotation!");
        }

        if(scale <= 0.0F) {
        	throw new IllegalArgumentException("The scale can't be negative or null !");
        }
        
		this.scale.set(scale, scale, scale);
		this.identity = identity && scale == 1.0F;
		this.uniformScale = true;
		return this;
	}
	
	/**
	 * Scale the scaling vector of the <code>Transform</code> by the provided vector.
	 * 
	 * @param scale The scaling vector to addition (not null).
	 *
	 * @return 		The updated transform. 
	 */
	public Transform scale(Vector3f scale) {
		this.scale.add(scale);
		this.identity = identity && scale.x() == 1.0F && scale.y() == 1.0F 
				&& scale.z() == 1.0F;
		this.uniformScale = uniformScale && scale.x() == scale.y() 
				&& scale.y() == scale.z();
		return this;
	}
	
	/**
	 * Scale the scaling vector of the <code>Transform</code> by the provided components.
	 * 
	 * @param x The X-component to increase.
	 * @param y The Y-component to increase.
	 * @param z The Z-component to increase.
	 *
	 * @return  The updated transform. 
	 */
	public Transform scale(float x, float y, float z) {
		this.scale.add(x, y, z);
		this.identity = identity && x == 1.0F && y == 1.0F && z == 1.0F;
		this.uniformScale = uniformScale && x == y && y == z;
		return this;
	}
	
	/**
	 * Return the transformation matrix of the <code>Transform</code>, which
	 * is used inside a {@link ShaderProgram} to compute the correct position,
	 * rotation and scale.
	 * 
	 * @param The storing matrix, or null to use the internal store of the transform.
	 * 
	 * @return The transformation matrix.
	 */
	public Matrix4f asModelMatrix(Matrix4f store) {
		var result = (store == null) ? transformMatrix : store;
		if(isIdentity()) {
			result.set(Matrix4f.IDENTITY_MATRIX);
			return result;
		}
		
		result.m30 = 0.0F;
		result.m31 = 0.0F;
		result.m32 = 0.0F;
		
		if(rotationMatrix) {
			result.m00 = scale.x() * rotation.m00;
			result.m10 = scale.x() * rotation.m10;
			result.m20 = scale.x() * rotation.m20;
			result.m01 = scale.y() * rotation.m01;
			result.m11 = scale.y() * rotation.m11;
			result.m21 = scale.y() * rotation.m21;
			result.m02 = scale.z() * rotation.m02;
			result.m12 = scale.z() * rotation.m12;
			result.m22 = scale.z() * rotation.m22;
		} else {
			result.m00 = rotation.m00;
			result.m10 = rotation.m10;
			result.m20 = rotation.m20;
			result.m01 = rotation.m01;
			result.m11 = rotation.m11;
			result.m21 = rotation.m21;
			result.m02 = rotation.m02;
			result.m12 = rotation.m12;
			result.m22 = rotation.m22;
		}
		
		result.m03 = translation.x();
		result.m13 = translation.y();
		result.m23 = translation.z();
		result.m33 = 1.0F;
		
		return result;
	}
	
	/**
	 * Populates the given {@link FloatBuffer} with the data from the <code>Transform</code> in column 
	 * major order.
	 * <p>
	 * The method is using relative put method, meaning the float data is written at the current 
	 * buffer's position and the position is incremented by 16.
	 * <p>
	 * The populated buffer can be used safely to transfer data to shaders as mat4 uniforms.
	 * 
	 * @param store The buffer to populate with the data (not null). 
	 * @return 		The given store populated with the transform data.
	 * 
	 * @throws BufferOverflowException Thrown if there isn't enough space to write all 16 floats.
	 */
	@Override
	public FloatBuffer populate(FloatBuffer store) {
		Validator.nonNull(store, "The float buffer can't be null!");

		if (rotationMatrix) {
			store.put(scale.x() * rotation.m00);
			store.put(scale.x() * rotation.m10);
			store.put(scale.x() * rotation.m20);
			store.put(0.0F);
			store.put(scale.y() * rotation.m01);
			store.put(scale.y() * rotation.m11);
			store.put(scale.y() * rotation.m21);
			store.put(0.0F);
			store.put(scale.z() * rotation.m02);
			store.put(scale.z() * rotation.m12);
			store.put(scale.z() * rotation.m22);
			store.put(0.0F);
		} else {
			store.put(rotation.m00);
			store.put(rotation.m10);
			store.put(rotation.m20);
			store.put(0.0F);
			store.put(rotation.m01);
			store.put(rotation.m11);
			store.put(rotation.m21);
			store.put(0.0F);
			store.put(rotation.m02);
			store.put(rotation.m12);
			store.put(rotation.m22);
			store.put(0.0F);
		}

		store.put(translation.x());
		store.put(translation.y());
		store.put(translation.z());
		store.put(1.0F);

		return store;
	}
	
	public Transform worldTransform(ReadableTransform parent, Transform store) { 
		Validator.nonNull(parent, "The parent's transform cannot be null!");
		var result = (store == null) ? new Transform() : store;
		
		if(isIdentity()) {
    		return result.set(parent);
    	}
    	
    	if(parent.isIdentity()) {
    		return result.set(this);
    	}
    	
    	if(rotationMatrix && parent.isRotationMatrix() && uniformScale) {
    		result.rotationMatrix = true;
            var newRotation = result.rotation;
            newRotation.set(rotation).mul(parent.getRotation());
            
            var newTranslation = result.translation.set(parent.getTranslation());
            rotation.applyPost(newTranslation, newTranslation);
            // uniform scale, so just use X.
            newTranslation.mul(scale.x());
            newTranslation.add(translation);
            
            if (parent.isUniformScale()) {
                result.setScale(scale.x() * parent.getScale().x());
            } else {
                var scale = result.scale.set(parent.getScale());
                scale.mul(scale.x());
            }
            
            result.update(true);
            
            return result;
    	}
    	
    	// In all remaining cases, the matrix cannot be written as R*S*X+T.
    	var matrixA = isRotationMatrix()
    			? rotation.multiplyDiagonalPost(scale, MercuryMath.getMatrix3f())
    			: rotation;
    			
    	var	matrixB = parent.isRotationMatrix()
    			? parent.getRotation().multiplyDiagonalPost(parent.getScale(), MercuryMath.getMatrix3f())
    			: parent.getRotation();
    			
    	var	newMatrix = result.rotation;
    	newMatrix.set(matrixA).mul(matrixB);
    	
        var newTranslate = result.translation;
        matrixA.applyPost(parent.getTranslation(), newTranslate).add(getTranslation());
        
        // Prevent scale bleeding since we don't set it.
        result.scale.set(1.0F, 1.0F, 1.0F);
        
        result.update(false);
        
        return result;
	}
	
	/**
	 * Set the <code>Transform</code> to its identity values.
	 * <p>
	 * The translation: {0,0,0}.
	 * The rotation: {0,0,0,1}.
	 * The scale: {1,1,1}.
	 * 
	 * @return The identity transform.
	 */
	public void identity() {
		translation.set(0, 0, 0);
		rotation.identity();
		scale.set(1, 1, 1);
		this.identity = true;
		this.rotationMatrix = true;
		this.uniformScale = true;
	}
	
	
	/**
	 * Return whether the <code>Transform</code> is an identity transform.
	 * 
	 * @return Whether the transform is an identity one.
	 */
	@Override
	public boolean isIdentity() {
		return identity;
	}
	
	/**
	 * Return whether the matrix of the <code>Transform</code> is only representing
	 * a rotation or combines a scale as well.
	 * 
	 * @return Whether the transform's matrix represent only a rotation.
	 */
	@Override
	public boolean isRotationMatrix() {
		return rotationMatrix;
	}
	
	/**
	 * Update the <code>Transform</code> state defined by the boolean variables. The method should be called 
	 * every time the transform internal state has changed and can't be easily determined.
	 * 
	 * @param rotationMatrixGuaranteed Whether the transform matrix is guaranteed to be rotation-only, 
	 * meaning it's orthonormal. This is used to avoid unnecessary and tedius checking in depth checking.
	 * 
	 * @see #isIdentity()
	 * @see #isRotationMatrix()
	 * @see #isUniformScale()
	 */
	private void update(boolean rotationMatrixGuaranteed) {
		this.identity = translation.equals(Vector3f.ZERO) && 
				rotation.isIdentity() && scale.equals(Vector3f.ONE);
		if(identity) {
            rotationMatrix = true;
            uniformScale = true;
        } else {
        	rotationMatrix = rotationMatrixGuaranteed ? true : rotation.isOrthonormal();
        	uniformScale = rotationMatrix && scale.x() == scale.y() && scale.y() == scale.z();
        }
	}
	
	/**
   	 * Sets the <code>Transform</code> to the {@link #identity()},
   	 * before retrieving it from a pool.
   	 */
   	@Override
   	public void reuse() {
   		identity();
   	}
   	
   	/**
   	 * Sets the <code>Transform</code> to the {@link #identity()},
   	 * before storing it into a pool.
   	 */
   	@Override
   	public void free() {
   		identity();
   	}
	
	/**
	 * Compare this transform with the provided <code>Transform</code>. It will first
	 * compare the translation, then the rotation and finally the scale.
	 * 
	 * @param  The other transform to compare with (not null).
	 * @return 0 &rarr; the 2 transforms are equal, negative &rarr; this transform comes before 
	 * 		   the other, negative &rarr; this transform comes after the other.
	 */
	@Override
	public int compareTo(Transform other) {
		int result = translation.compareTo(other.translation);
		if(result == 0) {
			result = rotation.compareTo(other.rotation);
		}
        if(result == 0) {
        	result = scale.compareTo(other.scale);
        }
        
        return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Transform)) {
			return false;
		}

		if (this == o) {
			return true;
		}
		
		Transform other = (Transform) o;
		return translation.equals(other.translation)
			&& rotation.equals(other.rotation)
			&& scale.equals(other.scale);
	}
}	
