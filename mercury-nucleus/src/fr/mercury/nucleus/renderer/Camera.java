package fr.mercury.nucleus.renderer;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Quaternion;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.math.readable.ReadableQuaternion;
import fr.mercury.nucleus.math.readable.ReadableVector3f;
import fr.mercury.nucleus.renderer.AbstractRenderer.MatrixType;
import fr.mercury.nucleus.renderer.queue.RenderLayer;
import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>Camera</code> represents a mathematical object designed to render
 * objects correctly in 3D space using matrices. 
 * <p>
 * It can also provides culling of objects outside of the frustum of 
 * the camera, to save on performance.
 * 
 * @author GnosticOccultist
 */
public final class Camera {
	
	/**
	 * The width of the camera.
	 */
	private int width;
	/**
	 * The height of the camera.
	 */
	private int height;
	 /**
     * The camera's location.
     */
    private final Vector3f location = new Vector3f();
    /**
     * The orientation of the camera.
     */
    private final Quaternion rotation = new Quaternion();
    /**
     * The near depth range for the viewport.
     */
    private float nearDepthRange = 0f;
    /**
     * The far depth range for the viewport.
     */
    private float farDepthRange = 1f;
	/**
	 * The view matrix.
	 */
	private final Matrix4f viewMatrix = new Matrix4f();
	/**
	 * The projection matrix.
	 */
	private final Matrix4f projectionMatrix = new Matrix4f();
	/**
	 * The view-projection matrix.
	 */
	private final Matrix4f viewProjectionMatrix = new Matrix4f();
	/**
	 * The graphical projection mode used by the camera.
	 */
	private GraphicalProjectionMode projectionMode = GraphicalProjectionMode.PERSPECTIVE;
	/**
	 * The accumulated dirty fields by the camera. At instantiation it will contain {@link CameraDirtyFields#DEPTH_RANGE},
	 * {@link CameraDirtyFields#PROJECTION_MATRIX}, {@link CameraDirtyFields#VIEW_MATRIX}.
	 */
	private final EnumSet<CameraDirtyFields> dirtyFields = EnumSet.of(CameraDirtyFields.DEPTH_RANGE, 
			CameraDirtyFields.PROJECTION_MATRIX, CameraDirtyFields.VIEW_MATRIX);
	/**
	 * The set of layer specifying which anima-mundis are to be queued and rendered.
	 */
	private final Set<RenderLayer> layers = new HashSet<RenderLayer>();
	
	/**
	 * Instantiates a new perspective <code>Camera</code> object with the specified width and height.
	 * 
	 * @param width  The width of the camera viewport.
	 * @param height The height of the camera viewport.
	 */
	public Camera(int width, int height) {
		this(GraphicalProjectionMode.PERSPECTIVE, width, height);
	}
	
	/**
	 * Instantiates a new <code>Camera</code> object with the specified width and height as well as
	 * the {@link GraphicalProjectionMode}.
	 * 
	 * @param projectionMode The graphical projection mode the camera uses to render the scene 
	 * 						 on the screen (not null).
	 * @param width  		 The width of the camera viewport.
	 * @param height 		 The height of the camera viewport.
	 */
	public Camera(GraphicalProjectionMode projectionMode, int width, int height) {
		Validator.nonNull(projectionMode, "The projection mode can't be null");
		this.projectionMode = projectionMode;
		this.width = width;
		this.height = height;
		
		this.layers.add(RenderLayer.DEFAULT);
	}
	
	/**
	 * Resize the <code>Camera</code> with the provided new width and height values if they
	 * have been modified.
	 * It will recompute the projection matrix using the new width and height during the next call
	 * of {@link #prepare(AbstractRenderer)} which should occur before every rendering cycle.
	 * 
	 * @param width  The new width of the camera.
	 * @param height The new height of the camera.
	 * @return 		 Whether the camera has been actually resized.
	 */
	public boolean resize(int width, int height) {
		// Prevent useless computations.
		if(this.width == width && this.height == height) {
			return false;
		}
		
		this.width = width;
		this.height = height;
		
		dirtyFields.add(CameraDirtyFields.PROJECTION_MATRIX);
		
		return true;
	}
	
	public void updateViewMatrix() {
		viewMatrix.view(location, rotation);
		
		dirtyFields.add(CameraDirtyFields.VIEW_PROJECTION_MATRIX);
	}
	
	public void setProjectionMatrix(float fovY, float aspect, float near, float far) {
		
		float h = MercuryMath.tan(fovY * (Math.PI / 180.0f) * .5f) * near;
	    float w = h * aspect;
	    
	    projectionMatrix.projection(projectionMode, near, far, -w, w, h, -h);
	    dirtyFields.add(CameraDirtyFields.VIEW_PROJECTION_MATRIX);
	}
	
	public void lookAt(ReadableVector3f position, ReadableVector3f worldUpVector) {
		lookAt(position.x(), position.y(), position.z(), worldUpVector);
	}
	
	public void lookAt(float x, float y, float z, ReadableVector3f worldUpVector) {
		
		Vector3f newDirection = MercuryMath.getVector3f();
		newDirection.set(x, y, z).sub(location).normalize();
		
		// Check to see if we haven't really updated camera -- no need to call sets.
		if(newDirection.equals(getDirection(null))) {
			return;
		}
		
		Vector3f newUp = MercuryMath.getVector3f();
		Vector3f newLeft = MercuryMath.getVector3f();
		
		newUp.set(worldUpVector).normalize();
		if(newUp.equals(Vector3f.ZERO)) {
			newUp.set(Vector3f.UNIT_Y);
		}
		
		newLeft.set(newUp).cross(newDirection).normalize();
		if(newLeft.equals(Vector3f.ZERO)) {
			if(newDirection.x != 0) {
				newLeft.set(newDirection.y, -newDirection.x, 0f);
			} else {
				newLeft.set(0f, newDirection.z, -newDirection.y);
			}
	    }
		
		newUp.set(newDirection).cross(newLeft).normalize();
		this.rotation.fromAxes(newLeft, newUp, newDirection);
		this.rotation.normalize();
		
		// We need to recompute the view matrix since we modified the 
		// rotation of the camera.
		dirtyFields.add(CameraDirtyFields.VIEW_MATRIX);
	}
	
	/**
	 * Prepares the <code>Camera</code> before performing the rendering using the provided
	 * {@link AbstractRenderer}.
	 * This method will update fields or recompute useful matrices.
	 * 
	 * @param renderer The renderer which will be used for rendering (not null).
	 */
	public void prepare(AbstractRenderer renderer) {
		Validator.nonNull(renderer, "The renderer can't be null!");
		
		if(dirtyFields.contains(CameraDirtyFields.DEPTH_RANGE)) {
			renderer.setDepthRange(nearDepthRange, farDepthRange);
		}
		
		if(dirtyFields.contains(CameraDirtyFields.PROJECTION_MATRIX)) {
			setProjectionMatrix(70f, (float) width / height, 1f, 1000f);
			renderer.setMatrix(MatrixType.PROJECTION, getProjectionMatrix());
		}
		
		if(dirtyFields.contains(CameraDirtyFields.VIEW_MATRIX)) {
			updateViewMatrix();
			renderer.setMatrix(MatrixType.VIEW, getViewMatrix());
		}
		
		if(dirtyFields.contains(CameraDirtyFields.VIEW_PROJECTION_MATRIX)) {
			// Recompute the view-projection matrix after.
			viewProjectionMatrix.set(projectionMatrix).mult(viewMatrix, viewProjectionMatrix);
			renderer.setMatrix(MatrixType.VIEW_PROJECTION, getViewProjectionMatrix());
		}
	}
	
	/**
	 * Return the readable-only location of the camera.
	 * 
	 * @return The location of the camera.
	 */
	public ReadableVector3f getLocation() {
		return location;
	}
	
	/**
	 * Sets the location of the <code>Camera</code> to the provided coordinates.
	 * 
	 * @param x The X-axis coordinate for the location.
	 * @param y The Y-axis coordinate for the location.
	 * @param z The Z-axis coordinate for the location.
	 */
	public void setLocation(float x, float y, float z) {
		location.set(x, y, z);
		dirtyFields.add(CameraDirtyFields.VIEW_MATRIX);
	}
	
	/**
	 * Sets the location of the <code>Camera</code> to the provided vector.
	 * 
	 * @param location The location vector of the camera (not null).
	 */
	public void setLocation(ReadableVector3f location) {
		Validator.nonNull(location, "The location vector can't be null!");
		setLocation(location.x(), location.y(), location.z());
	}
	
	/**
	 * Return the readable-only rotation of the camera.
	 * 
	 * @return The rotation of the camera.
	 */
	public ReadableQuaternion getRotation() {
		return rotation;
	}
	
	/**
	 * Sets the rotation of the <code>Camera</code> to the provided quaternion.
	 * 
	 * @param rotation The rotation quaternion of the camera (not null).
	 */
	public void setRotation(ReadableQuaternion rotation) {
		Validator.nonNull(rotation, "The rotation quaternion can't be null!");
		this.rotation.set(rotation);
	}
	
	/**
	 * Return the readable-only left-axis vector of this <code>Camera</code>.
	 * 
	 * @param store The store for the result.
	 * @return		The left-axis vector of the camera (readable-only).
	 */
	public ReadableVector3f getLeft(Vector3f store) {
		return rotation.getRotationColumn(0, store);
	}
	
	/**
	 * Return the readable-only up-axis vector of this <code>Camera</code>.
	 * 
	 * @param store The store for the result.
	 * @return		The up-axis vector of the camera (readable-only).
	 */
	public ReadableVector3f getUp(Vector3f store) {
		return rotation.getRotationColumn(1, store);
	}
	
	/**
	 * Return the readable-only direction vector of this <code>Camera</code>.
	 * 
	 * @param store The store for the result.
	 * @return		The direction vector of the camera (readable-only).
	 */
	public ReadableVector3f getDirection(Vector3f store) {
		return rotation.getRotationColumn(2, store);
	}
	
	/**
	 * Return the near depth range of the <code>Camera</code> viewport.
	 * 
	 * @return The near depth range.
	 */
	public float getNearDepthRange() {
		return nearDepthRange;
	}
	
	/**
	 * Sets the near depth range of the <code>Camera</code> viewport.
	 * 
	 * @param nearDepthRange The near depth range (&ge; 0 &le;1, default &rarr; 0).
	 */
	public void setNearDepthRange(float nearDepthRange) {
		Validator.inRange(nearDepthRange, 0, 1);
		if(this.nearDepthRange == nearDepthRange) {
			return;
		}
		
		this.nearDepthRange = nearDepthRange;
		dirtyFields.add(CameraDirtyFields.DEPTH_RANGE);
	}
	
	/**
	 * Return the far depth range of the <code>Camera</code> viewport.
	 * 
	 * @return The far depth range.
	 */
	public float getFarDepthRange() {
		return farDepthRange;
	}
	
	/**
	 * Sets the far depth range of the <code>Camera</code> viewport.
	 * 
	 * @param farDepthRange The far depth range (&ge; 0 &le;1, default &rarr; 1).
	 */
	public void setFarDepthRange(float farDepthRange) {
		Validator.inRange(farDepthRange, 0, 1);
		if(this.farDepthRange == farDepthRange) {
			return;
		}
		
		this.farDepthRange = farDepthRange;
		dirtyFields.add(CameraDirtyFields.DEPTH_RANGE);
	}
	
	/**
	 * Return the view-projection matrix of the camera.
	 * 
	 * @return The view-projection matrix.
	 */
	public Matrix4f getViewProjectionMatrix() {
		return viewProjectionMatrix;
	}
	
	/**
	 * Return the projection matrix of the camera.
	 * 
	 * @return The projection matrix.
	 */
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
	
	/**
	 * Return the view matrix of the camera.
	 * 
	 * @return The view matrix.
	 */
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	
	/**
	 * Return the width of the camera, usually the
	 * width of the display.
	 * 
	 * @return The width of the camera.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Return the height of the camera, usually the
	 * height of the display.
	 * 
	 * @return The height of the camera.
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Return the {@link GraphicalProjectionMode} of the <code>Camera</code>. 
	 * 
	 * @return The projection mode used by the camera.
	 */
	public GraphicalProjectionMode getProjectionMode() {
		return projectionMode;
	}
	
	/**
	 * Sets the {@link GraphicalProjectionMode} of the <code>Camera</code>.
	 * 
	 * @param mode The projection mode used by the camera (not null, default &rarr; perspective).
	 */
	public void setProjectionMode(GraphicalProjectionMode mode) {
		Validator.nonNull(mode, "The projection mode of the camera can't be null!");
		this.projectionMode = mode;
		dirtyFields.add(CameraDirtyFields.PROJECTION_MATRIX);
		dirtyFields.add(CameraDirtyFields.VIEW_MATRIX);
	}
	
	/**
	 * Checks whether the specified {@link RenderLayer} is used by the <code>Camera</code>, meaning
	 * it will queue and render all {@link AnimaMundi} present on the layer.
	 * 
	 * @param layer The layer to check with the camera.	
	 * @return		Whether the layer is queued and rendered by the camera.
	 */
	public boolean checkLayer(RenderLayer layer) {
		return layers.contains(layer);
	}
	
	/**
	 * <code>GraphicalProjectionMode</code> enumerates all possible projection modes available for a <code>Camera</code>.
	 * 
	 * @author GnosticOccultist
	 */
	public enum GraphicalProjectionMode {
		/**
		 * A linear projection mode used to render three-dimensional geometries on a picture plane.
		 * <p>
		 * Distant object appears smaller than nearer ones and parallel line seems to converge into a single point 
		 * named 'vanishing point'.
		 */
		PERSPECTIVE,
		/**
		 * A projection mode used to render two-dimensional representation of 3D geometries.
		 * <p>
		 * It is using an orthogonal line for each point of the geometry which is directly projected onto the projection plane.
		 */
		ORTHOGRAPHIC;
	}
	
	/**
	 * <code>CameraDirtyFields</code> is an enumeration to represent the fields which can
	 * be dirty during the life-cycle of the {@link Camera}.
	 * 
	 * @author GnosticOccultist
	 */
	enum CameraDirtyFields {
		/**
		 * The depth range fields are dirty.
		 */
		DEPTH_RANGE,
		/**
		 * The view matrix is dirty and needs to be recomputed and probably resent
		 * to the renderer.
		 */
		VIEW_MATRIX,
		/**
		 * The projection matrix is dirty and needs to be recomputed and probably 
		 * resent to the renderer.
		 */
		PROJECTION_MATRIX,
		/**
		 * The view-projection matrix is dirty and needs to be recomputed and probably 
		 * resent to the renderer.
		 */
		VIEW_PROJECTION_MATRIX;
	}
}
