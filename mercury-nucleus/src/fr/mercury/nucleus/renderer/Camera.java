package fr.mercury.nucleus.renderer;

import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Quaternion;
import fr.mercury.nucleus.math.objects.Vector3f;

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
    private Vector3f location;
    /**
     * The orientation of the camera.
     */
    private Quaternion rotation;
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
	 * The frustum planes distance from the camera.
	 */
	private float frustumLeft, frustumRight, frustumBottom, 
		frustumTop, frustumNear, frustumFar;
	
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
	
	public void updateViewMatrix() {
		Vector3f left = getLeft(MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class));
		Vector3f direction = getDirection(MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class));
		Vector3f up = getUp(MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class));
		
		viewMatrix.viewMatrix(location, direction, up, left);
		
		updateViewProjectionMatrix();
	}
	
	public void updateViewProjectionMatrix() {
		viewProjectionMatrix.set(projectionMatrix).mult(viewMatrix, viewProjectionMatrix);
	}
	
	public void setProjectionMatrix(float fovY, float aspect, float near, float far) {
		
		float h = MercuryMath.tan(fovY * (Math.PI / 180.0f) * .5f) * near;
	    float w = h * aspect;

	    frustumLeft = -w;
	    frustumRight = w;
	    frustumBottom = -h;
	    frustumTop = h;
	    frustumNear = near;
	    frustumFar = far;
	    
	    projectionMatrix.projection(frustumNear, frustumFar, frustumLeft, 
	    		frustumRight, frustumTop, frustumBottom);
	}
	
	/**
	 * Retrieves the direction vector the camera is facing.
	 * 
	 * @param store The store in which the direction is stored.
	 * @return		The direction the camera is facing.
	 */	
	public Vector3f getDirection(Vector3f store) {
		return rotation.getRotationColumn(2, store);
	}
	
	/**
	 * Retrieves the left-axis of the camera.
	 * 
	 * @param store The store in which the left-axis is stored.
	 * @return		The left-axis of the camera.
	 */	
	public Vector3f getLeft(Vector3f store) {
		return rotation.getRotationColumn(0, store);
	}
	
	/**
	 * Retrieves the up-axis of the camera.
	 * 
	 * @param store The store in which the up-axis is stored.
	 * @return		The up-axis of the camera.
	 */	
	public Vector3f getUp(Vector3f store) {
		return rotation.getRotationColumn(1, store);
	}
	
	/**
	 * Return the location of the camera.
	 * 
	 * @return The location of the camera.
	 */
	public Vector3f getLocation() {
		return location;
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
}
