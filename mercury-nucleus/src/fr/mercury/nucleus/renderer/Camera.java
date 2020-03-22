package fr.mercury.nucleus.renderer;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Vector3f;
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
	 * The logger for the camera.
	 */
	protected static final Logger logger = FactoryLogger.getLogger("mercury.renderer.camera");
	
	/**
	 * The width of the camera.
	 */
	private int width;
	/**
	 * The height of the camera.
	 */
	private int height;
	/**
	 * The field of view in the Y-axis in degrees, only used in 
	 * {@link GraphicalProjectionMode#PERSPECTIVE}.
	 */
	private float fov = 45f;
	/**
	 * The zooming factor of the camera, only used in 
	 * {@link GraphicalProjectionMode#ORTHOGRAPHIC}.
	 */
	private float zoom = 1f;
	/**
	 * The distance from the camera to the 6 planes defining its frustum.
	 */
	private float frustumLeft, frustumRight,  frustumBottom, frustumTop, frustumNear, frustumFar;
	/**
     * The camera's location.
     */
    private final Vector3f location = new Vector3f();
    /**
     * The direction of the left-side of the camera.
     */
    private final Vector3f left = new Vector3f(-1.0F, 0.0F, 0.0F);
    /**
     * The direction of the up-side of the camera.
     */
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    /**
     * The direction of the camera.
     */
    private final Vector3f direction = new Vector3f(0.0F, 0.0F, -1.0F);
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
		
		// Make sure all the rendering data such as matrices is computed before the first rendering pass
		// and applied to the renderer.
		this.dirtyFields.addAll(Arrays.asList(CameraDirtyFields.values()));
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
	
	/**
	 * Sets the frustum planes values of the <code>Camera</code> using the given perspective values.
	 * The method will mark the camera as dirty, and the new projection matrix will be computed and applied on the next call
	 * of {@link #prepare(AbstractRenderer)}.
	 * 
	 * @param aspect The aspect ratio of the view, usually the display width divided by the height (&ge;0, &le;1).
	 * @param near	 The near plane frustum distance from the camera (&ge;0).
	 * @param far	 The far plane frustum distance from the camera (&ge;0).
	 * @return 		 The camera with the updated frustum for chaining purposes.
	 */
	public Camera setFrustumPerspective(float aspect, float near, float far) {
		return setFrustumPerspective(fov, aspect, near, far);
	}
	
	/**
	 * Sets the frustum planes values of the <code>Camera</code> using the given perspective values.
	 * The method will mark the camera as dirty, and the new projection matrix will be computed and applied on the next call
	 * of {@link #prepare(AbstractRenderer)}.
	 * 
	 * @param fovY	 The field of view of the camera.
	 * @param aspect The aspect ratio of the view, usually the display width divided by the height (&ge;0, &le;1).
	 * @param near	 The near plane frustum distance from the camera (&ge;0).
	 * @param far	 The far plane frustum distance from the camera (&ge;0).
	 * @return 		 The camera with the updated frustum for chaining purposes.
	 */
	public Camera setFrustumPerspective(float fovY, float aspect, float near, float far) {
		Validator.nonNegative(near, "The near plane value can't be negative!");
		Validator.nonNegative(far, "The far plane value can't be negative!");
		if(Float.isNaN(aspect) || Float.isInfinite(aspect)) {
			logger.warning("Invalid aspect ration for Camera " + this + " provided with setFrustumPerspective()!");
			return this;
		}
		
		this.fov = fovY;
		var h = MercuryMath.tan((float) (fov * (Math.PI / 180.0f) * .5f)) * near;
		var w = h * aspect;
		frustumLeft = -w;
		frustumRight = w;
		frustumBottom = -h;
		frustumTop = h;
		frustumNear = near;
		frustumFar = far;
        
		this.dirtyFields.add(CameraDirtyFields.PROJECTION_MATRIX);
        
		return this;
	}
	
	/**
	 * Update the view {@link Matrix4f} of the <code>Camera</code>.
	 */
	public void updateViewMatrix() {
		viewMatrix.view(location, left, up, direction);
		
		dirtyFields.add(CameraDirtyFields.VIEW_PROJECTION_MATRIX);
	}
	
	/**
	 * Update the projection {@link Matrix4f} of the <code>Camera</code> according to its {@link GraphicalProjectionMode}.
	 */
	protected void updateProjectionMatrix() {
		switch (projectionMode) {
			case ORTHOGRAPHIC:
				projectionMatrix.orthographic(frustumNear, frustumFar, frustumLeft, frustumRight, frustumTop, frustumBottom);
				break;
			case PERSPECTIVE:
				projectionMatrix.perspective(frustumNear, frustumFar, frustumLeft, frustumRight, frustumTop, frustumBottom);
				break;
			default:
				throw new IllegalStateException("Unknown projection mode for camera " + projectionMode);
		}
		
		dirtyFields.add(CameraDirtyFields.VIEW_PROJECTION_MATRIX);
	}
	
	/**
	 * Orientates the <code>Camera</code> towards the provided world position vector using the difference
	 * between it and the camera location as the new facing direction and the world up vector to compute the
	 * left and up-axis of the camera. 
	 * 
	 * @param x				The X world coordinate to look at.
	 * @param y				The Y world coordinate to look at.
	 * @param z				The Z world coordinate to look at.
	 * @param worldUpVector A normalized vector describing the up direction of the world (default&rarr;[0, 1, 0]).
	 */
	public void lookAt(ReadableVector3f position, ReadableVector3f worldUpVector) {
		lookAt(position.x(), position.y(), position.z(), worldUpVector);
	}
	
	/**
	 * Orientates the <code>Camera</code> towards the provided world position using the difference
	 * between it and the camera location as the new facing direction and the world up vector to compute the
	 * left and up-axis of the camera. 
	 * 
	 * @param x				The X world coordinate to look at.
	 * @param y				The Y world coordinate to look at.
	 * @param z				The Z world coordinate to look at.
	 * @param worldUpVector A normalized vector describing the up direction of the world (default&rarr;[0, 1, 0]).
	 */
	public void lookAt(float x, float y, float z, ReadableVector3f worldUpVector) {
		var newDirection = MercuryMath.getVector3f();
		newDirection.set(x, y, z).sub(location).normalize();
		
		// Check to see if we haven't really updated camera -- no need to call sets.
		if(newDirection.equals(direction)) {
			return;
		}
		direction.set(newDirection);
		
		up.set(worldUpVector).normalize();
		// Here, we default to the Y-axis as the up vector of the camera orientation.
		if(up.equals(Vector3f.ZERO)) {
			up.set(Vector3f.UNIT_Y);
		}
		
		left.set(up).cross(direction).normalize();
		if(left.equals(Vector3f.ZERO)) {
			if(direction.x() != 0.0F) {
				left.set(direction.y(), -direction.x(), 0.0F);
			} else {
				left.set(0.0F, direction.z(), -direction.y());
			}
	    }
		
		up.set(direction).cross(left).normalize();
		
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
			updateProjectionMatrix();
			renderer.setMatrix(MatrixType.PROJECTION, getProjectionMatrix());
		}
		
		if(dirtyFields.contains(CameraDirtyFields.VIEW_MATRIX)) {
			updateViewMatrix();
			renderer.setMatrix(MatrixType.VIEW, getViewMatrix());
		}
		
		if(dirtyFields.contains(CameraDirtyFields.VIEW_PROJECTION_MATRIX)) {
			// Recompute the view-projection matrix after.
			viewProjectionMatrix.set(viewMatrix).mult(projectionMatrix, viewProjectionMatrix);
			renderer.setMatrix(MatrixType.VIEW_PROJECTION, getViewProjectionMatrix());
		}
	}
	
	/**
	 * Return the width of the <code>Camera</code>, usually the
	 * width of the display.
	 * 
	 * @return The width of the camera.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Return the height of the <code>Camera</code>, usually the
	 * height of the display.
	 * 
	 * @return The height of the camera.
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Return the field of view in the Y-axis of the <code>Camera</code> exprimed 
	 * in degrees.
	 * The value is only used when the camera is in {@link GraphicalProjectionMode#PERSPECTIVE}.
	 * 
	 * @return The field of view of the camera in degrees.
	 */
	public float getFOV() {
		return fov;
	}
	
	/**
	 * Sets the field of view in the Y-axis of the <code>Camera</code> in degrees.
	 * The method will set automatically the camera into {@link GraphicalProjectionMode#PERSPECTIVE} 
	 * and compute the new projection matrix.
	 * 
	 * @param fov The field of view of the camera in degrees.
	 */
	public void setFOV(float fov) {
		this.fov = fov;
		
		setProjectionMode(GraphicalProjectionMode.PERSPECTIVE);
		dirtyFields.add(CameraDirtyFields.PROJECTION_MATRIX);
	}
	
	/**
	 * Return the zooming factor of the <code>Camera</code>.
	 * The value is only used when the camera is in {@link GraphicalProjectionMode#ORTHOGRAPHIC}.
	 * 
	 * @return The zooming factor of the camera.
	 */
	public float getZoom() {
		return zoom;
	}
	
	/**
	 * Sets the zooming factor of the <code>Camera</code>.
	 * The method will set automatically the camera into {@link GraphicalProjectionMode#ORTHOGRAPHIC} 
	 * and compute the new projection matrix.
	 * 
	 * @param zoom The zooming factor of the camera.
	 */
	public void setZoom(float zoom) {
		this.zoom = zoom;
		
		setProjectionMode(GraphicalProjectionMode.ORTHOGRAPHIC);
		dirtyFields.add(CameraDirtyFields.PROJECTION_MATRIX);
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
	 * Return the readable-only left-axis vector of this <code>Camera</code>.
	 * 
	 * @param store The store for the result.
	 * @return		The left-axis vector of the camera (readable-only).
	 */
	public ReadableVector3f getLeft(Vector3f store) {
		return left;
	}
	
	/**
	 * Sets the left-axis vector of the <code>Camera</code> to the provided one.
	 * The method will mark the camera as dirty, and the new view matrix will be computed and applied on the next call
	 * of {@link #prepare(AbstractRenderer)}.
	 * 
	 * @param left The desired left-axis vector (not null).
	 * @return	   The updated camera for chaining purposes.
	 */
	public Camera setLeft(ReadableVector3f left) {
		this.left.set(left);
		dirtyFields.add(CameraDirtyFields.VIEW_MATRIX);
		
		return this;
	}
	
	/**
	 * Return the readable-only up-axis vector of this <code>Camera</code>.
	 * 
	 * @param store The store for the result.
	 * @return		The up-axis vector of the camera (readable-only).
	 */
	public ReadableVector3f getUp(Vector3f store) {
		return up;
	}
	
	/**
	 * Sets the up-axis vector of the <code>Camera</code> to the provided one.
	 * The method will mark the camera as dirty, and the new view matrix will be computed and applied on the next call
	 * of {@link #prepare(AbstractRenderer)}.
	 * 
	 * @param up The desired up-axis vector (not null).
	 * @return	 The updated camera for chaining purposes.
	 */
	public Camera setUp(ReadableVector3f up) {
		this.up.set(up);
		dirtyFields.add(CameraDirtyFields.VIEW_MATRIX);
		
		return this;
	}
	
	/**
	 * Return the readable-only direction vector of this <code>Camera</code>.
	 * 
	 * @param store The store for the result.
	 * @return		The direction vector of the camera (readable-only).
	 */
	public ReadableVector3f getDirection(Vector3f store) {
		return direction;
	}
	
	/**
	 * Sets the direction vector that the <code>Camera</code> is facing to the provided one.
	 * The method will mark the camera as dirty, and the new view matrix will be computed and applied on the next call
	 * of {@link #prepare(AbstractRenderer)}.
	 * 
	 * @param direction The desired direction vector (not null).
	 * @return	   		The updated camera for chaining purposes.
	 */
	public Camera setDirection(ReadableVector3f direction) {
		this.direction.set(direction);
		dirtyFields.add(CameraDirtyFields.VIEW_MATRIX);
		
		return this;
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
	 * Adds the specified {@link RenderLayer} to be managed by the <code>Camera</code>.
	 * 
	 * @param layer The layer to manage with the camera (not null).
	 */
	public void addLayer(RenderLayer layer) {
		Validator.nonNull(layer, "The render layer can't be null!");
		this.layers.add(layer);
	}
	
	/**
	 * Removes the specified {@link RenderLayer} to no longer be managed by the <code>Camera</code>.
	 * 
	 * @param layer The layer to no longer manage with the camera (not null).
	 */
	public void removeLayer(RenderLayer layer) {
		Validator.nonNull(layer, "The render layer can't be null!");
		this.layers.remove(layer);
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
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[width= " + width + ", height= " + height + ", location= " + location + 
				", layers=" + Arrays.toString(layers.toArray(new RenderLayer[layers.size()])) + "]";
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
