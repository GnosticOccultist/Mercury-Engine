package fr.mercury.nucleus.renderer;

import java.util.EnumSet;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Quaternion;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.math.readable.ReadableQuaternion;
import fr.mercury.nucleus.math.readable.ReadableVector3f;
import fr.mercury.nucleus.renderer.AbstractRenderer.MatrixType;

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
    private final Vector3f location;
    /**
     * The orientation of the camera.
     */
    private final Quaternion rotation;
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
	 * The accumulated dirty fields by the camera. At instantiation it will contain {@link CameraDirtyFields#DEPTH_RANGE},
	 * {@link CameraDirtyFields#PROJECTION_MATRIX}, {@link CameraDirtyFields#VIEW_MATRIX}.
	 */
	protected final EnumSet<CameraDirtyFields> dirtyFields = EnumSet.of(CameraDirtyFields.DEPTH_RANGE, 
			CameraDirtyFields.PROJECTION_MATRIX, CameraDirtyFields.VIEW_MATRIX);
	
	/**
	 * Instantiates a new <code>Camera</code> object with the specified
	 * width and height.
	 * 
	 * @param width  The width.
	 * @param height The height.
	 */
	public Camera(int width, int height) {
		this.location = new Vector3f();
		this.rotation = new Quaternion();
		this.width = width;
		this.height = height;
	}
	
	public void resize(int width, int height) {
		// Prevent useless computations.
		if(this.width == width && this.height == height) {
			return;
		}
		
		this.width = width;
		this.height = height;
		
		dirtyFields.add(CameraDirtyFields.PROJECTION_MATRIX);
		dirtyFields.add(CameraDirtyFields.VIEW_MATRIX);
	}
	
	public void updateViewMatrix() {
		viewMatrix.viewMatrix(location, rotation);
		
		updateViewProjectionMatrix();
	}
	
	private void updateViewProjectionMatrix() {
		viewProjectionMatrix.set(projectionMatrix).mult(viewMatrix, viewProjectionMatrix);
	}
	
	public void setProjectionMatrix(float fovY, float aspect, float near, float far) {
		
		float h = MercuryMath.tan(fovY * (Math.PI / 180.0f) * .5f) * near;
	    float w = h * aspect;
	    
	    projectionMatrix.projection(near, far, -w, w, h, -h);
	}
	
	public void lookAt(float x, float y, float z, ReadableVector3f worldUpVector) {
		Vector3f newDirection = MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class, Vector3f::new);
		Vector3f newUp = MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class, Vector3f::new);
		Vector3f newLeft = MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class, Vector3f::new);
		
		newDirection.set(x, y, z).sub(location).normalize();
		newUp.set(worldUpVector).normalize();
		if(newUp.equals(Vector3f.ZERO)) {
			newUp.set(Vector3f.UNIT_Y);
		}
		
		newLeft.set(newUp).cross(newDirection).normalize();
		if (newLeft.equals(Vector3f.ZERO)) {
			if (newDirection.x != 0) {
				newLeft.set(newDirection.y, -newDirection.x, 0f);
			} else {
				newLeft.set(0f, newDirection.z, -newDirection.y);
			}
	    }
		
		newUp.set(newDirection).cross(newLeft).normalize();
		this.rotation.fromAxes(newLeft, newUp, newDirection);
		this.rotation.normalize();
	}
	
	public void setup(AbstractRenderer renderer) {
		if(dirtyFields.contains(CameraDirtyFields.DEPTH_RANGE)) {
			renderer.setDepthRange(nearDepthRange, farDepthRange);
		}
		
		if(dirtyFields.contains(CameraDirtyFields.VIEW_MATRIX)) {
			updateViewMatrix();
			renderer.setMatrix(MatrixType.VIEW, getViewMatrix());
		}
		
		if(dirtyFields.contains(CameraDirtyFields.PROJECTION_MATRIX)) {
			setProjectionMatrix(70f, (float) width / height, 1f, 1000f);
			renderer.setMatrix(MatrixType.PROJECTION, getProjectionMatrix());
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
	 * Sets the location of the <code>Camera</code> to the provided coordinates.
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
	
	public enum CameraDirtyFields {
		DEPTH_RANGE,
		
		VIEW_MATRIX,
		
		PROJECTION_MATRIX;
	}
}
