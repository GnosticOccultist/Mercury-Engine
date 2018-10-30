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
    private final Vector3f location;
    /**
     * The orientation of the camera.
     */
    private final Quaternion rotation;
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
		
		updateViewMatrix();
	}
	
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		
		setProjectionMatrix(70f, (float) width / height, 1f, 1000f);
		updateViewMatrix();
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

	    frustumLeft = -w;
	    frustumRight = w;
	    frustumBottom = -h;
	    frustumTop = h;
	    frustumNear = near;
	    frustumFar = far;
	    
	    projectionMatrix.projection(frustumNear, frustumFar, frustumLeft, 
	    		frustumRight, frustumTop, frustumBottom);
	}
	
	public void lookAt(Vector3f pos, Vector3f worldUpVector) {
		Vector3f newDirection = MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class);
		Vector3f newUp = MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class);
		Vector3f newLeft = MercuryMath.LOCAL_VARS.acquireNext(Vector3f.class);
		
		newDirection.set(pos).sub(location).normalize();
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
	
	/**
	 * Return the rotation of the camera.
	 * 
	 * @return The rotation of the camera.
	 */
	public Quaternion getRotation() {
		return rotation;
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
