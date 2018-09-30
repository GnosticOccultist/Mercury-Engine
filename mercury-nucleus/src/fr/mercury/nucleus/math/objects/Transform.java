package fr.mercury.nucleus.math.objects;

import fr.mercury.nucleus.math.MercuryMath;

/**
 * <code>Transform</code> is a mathematical object representing a translation, a rotation 
 * and a scale for a scene-graph object in a 3D space.
 * <p>
 * It is mostly used to compute the useful matrices for the shaders such as transform matrix.
 * 
 * @author GnosticOccultist
 */
public final class Transform {
	
	/**
	 * The translation of the object.
	 */
	private final Vector3f translation;
	/**
	 * The rotation of the object.
	 */
	private final Quaternion rotation;
	/**
	 * The scale of the object.
	 */
	private final Vector3f scale;
	/**
	 * The matrix containing the model transform.
	 */
	private final Matrix4f transformMatrix;
	
	/**
	 * Instantiates a new <code>Transform</code> with identity values.
	 * <p>
	 * The translation: {0,0,0}.
	 * The rotation: {0,0,0,1}.
	 * The scale: {1,1,1}.
	 */
	public Transform() {
		this(new Vector3f(), new Quaternion(), new Vector3f(1, 1, 1));
	}
	
	/**
	 * Instantiates a new <code>Transform</code> with the provided 
	 * translation vector and identity values for the rotation and the scale.
	 * <p>
	 * The rotation: {0,0,0,1}.
	 * The scale: {1,1,1}.
	 */
	public Transform(Vector3f translation) {
		this(translation, new Quaternion(), new Vector3f(1, 1, 1));
	}
	
	/**
	 * Instantiates a new <code>Transform</code> with the provided 
	 * translation vector and rotation quaternion and identity values for the scale.
	 * <p>
	 * The scale: {1,1,1}.
	 */
	public Transform(Vector3f translation, Quaternion rotation) {
		this(translation, rotation, new Vector3f(1, 1, 1));
	}
	
	/**
	 * Instantiates a new <code>Transform</code> with the provided 
	 * translation vector, rotation quaternion and scaling vector.
	 */
	public Transform(Vector3f translation, Quaternion rotation, Vector3f scale) {
		this.translation = new Vector3f(translation);
		this.rotation = new Quaternion(rotation);
		this.scale = new Vector3f(scale);
		this.transformMatrix = new Matrix4f();
	}
	
	/**
	 * Return the translation vector of the <code>Transform</code>.
	 * 
	 * @return The translation vector.
	 */
	public Vector3f getTranslation() {
		return translation;
	}
	
	/**
	 * Set the translation vector of the <code>Transform</code>
	 * to the provided vector.
	 * 
	 * @param translation The translation vector.
	 * @return			  The transform with the new translation vector.
	 */
	public Transform setTranslation(Vector3f translation) {
		this.translation.set(translation);
		return this;
	}
	
	/**
	 * Set the translation vector of the <code>Transform</code>
	 * to the provided components.
	 *
	 * @param x The X-component to copy from.
	 * @param y The Y-component to copy from.
	 * @param z The Z-component to copy from.
	 * 
	 * @return  The transform with the new translation vector.
	 */
	public Transform setTranslation(float x, float y, float z) {
		this.translation.set(x, y, z);
		return this;
	}
	
	/**
	 * Translate the translation vector of the <code>Transform</code> by
	 * the provided vector.
	 * 
	 * @param translation The translation vector to addition.
	 *
	 * @return 			  The updated transform. 
	 */
	public Transform translate(Vector3f translation) {
		this.translation.add(translation);
		return this;
	}
	
	/**
	 * Translate the translation vector of the <code>Transform</code> by
	 * the provided components.
	 * 
	 * @param x The X-component to increase.
	 * @param y The Y-component to increase.
	 * @param z The Z-component to increase.
	 *
	 * @return The updated transform. 
	 */
	public Transform translate(float x, float y, float z) {
		this.translation.add(x, y, z);
		return this;
	}
	
	/**
	 * Return the rotation quaternion of the <code>Transform</code>.
	 * 
	 * @return The rotation quaternion.
	 */
	public Quaternion getRotation() {
		return rotation;
	}
	
	/**
	 * Set the rotation quaternion of the <code>Transform</code>
	 * to the provided quaternion.
	 * 
	 * @param rotation The rotation quaternion.
	 * @return		   The transform with the new rotation quaternion.
	 */
	public Transform setRotation(Quaternion rotation) {
		this.rotation.set(rotation);
		return this;
	}
	
	/**
	 * Set the rotation quaternion of the <code>Transform</code>
	 * to the provided components. The w component is set to 1.
	 *
	 * @param x The X-component to copy from.
	 * @param y The Y-component to copy from.
	 * @param z The Z-component to copy from.
	 * 
	 * @return  The transform with the new rotation quaternion.
	 */
	public Transform setRotation(float x, float y, float z) {
		this.rotation.set(x, y, z, 1);
		return this;
	}
	
	/**
	 * Rotate the rotation quaternion of the <code>Transform</code> by
	 * the provided quaternion.
	 * 
	 * @param rotation The rotation quaternion to addition.
	 *
	 * @return 		   The updated transform. 
	 */
	public Transform rotate(Quaternion rotation) {
		this.rotation.add(rotation);
		return this;
	}
	
	/**
	 * Rotate the rotation quaternion of the <code>Transform</code> by
	 * the provided components. The w component is leaved untouched.
	 * 
	 * @param x The X-component to increase.
	 * @param y The Y-component to increase.
	 * @param z The Z-component to increase.
	 *
	 * @return  The updated transform. 
	 */
	public Transform rotate(float x, float y, float z) {
		this.rotation.add(x, y, z, 0);
		return this;
	}
	
	/**
	 * Return the scaling vector of the <code>Transform</code>.
	 * 
	 * @return The scaling vector.
	 */
	public Vector3f getScale() {
		return scale;
	}
	
	/**
	 * Set the scale vector of the <code>Transform</code>
	 * to the provided vector.
	 * 
	 * @param scale The scale vector.
	 * @return		The transform with the new scaling vector.
	 */
	public Transform setScale(Vector3f scale) {
		this.scale.set(scale);
		return this;
	}
	
	/**
	 * Set the scale vector of the <code>Transform</code>
	 * to the provided components.
	 *
	 * @param x The X-component to copy from.
	 * @param y The Y-component to copy from.
	 * @param z The Z-component to copy from.
	 * 
	 * @return  The transform with the new scaling vector.
	 */
	public Transform setScale(float x, float y, float z) {
		this.scale.set(x, y, z);
		return this;
	}
	
	/**
	 * Scale the scaling vector of the <code>Transform</code> by
	 * the provided vector.
	 * 
	 * @param scale The scaling vector to addition.
	 *
	 * @return 		The updated transform. 
	 */
	public Transform scale(Vector3f scale) {
		this.scale.add(scale);
		return this;
	}
	
	/**
	 * Scale the scaling vector of the <code>Transform</code> by
	 * the provided components.
	 * 
	 * @param x The X-component to increase.
	 * @param y The Y-component to increase.
	 * @param z The Z-component to increase.
	 *
	 * @return  The updated transform. 
	 */
	public Transform scale(float x, float y, float z) {
		this.scale.add(x, y, z);
		return this;
	}
	
	/**
	 * Return the transformation matrix of the <code>Transform</code>, which
	 * is used inside a <code>ShaderProgram</code> to compute the correct position,
	 * rotation and scale.
	 * 
	 * @return The transformation matrix.
	 */
	public Matrix4f transform() {
		transformMatrix.identity();
		
		transformMatrix.setRotation(rotation);
		transformMatrix.setTranslation(translation);
		transformMatrix.mult(scaleMatrix(MercuryMath.LOCAL_VARS
    			.acquireNext(Matrix4f.class)), transformMatrix);
    	
		return transformMatrix;
	}
	
	/**
	 * Apply the <code>Transform</code> scale to the provided <code>Matrix4f</code>,
	 * to be used as a scaling matrix.
	 * 
	 * @param store The matrix to scale.
	 * @return		The scaling matrix.
	 */
	public Matrix4f scaleMatrix(Matrix4f store) {
		store.scale(scale);
		return store;
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
		rotation.set(0, 0, 0, 1);
		scale.set(1, 1, 1);
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
