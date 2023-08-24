package fr.mercury.nucleus.input.control;

import fr.mercury.nucleus.math.objects.Matrix3f;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.math.readable.ReadableVector3f;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.utils.ReadableTimer;

public class FirstPersonCamControl implements CameraControl {
	
    /**
     * The currently controlled camera.
     */
    private volatile Camera camera;
    /**
     * The rotation speed of the camera.
     */
	private float rotateSpeed = 0.05f;
	/**
	 * The move speed of the camera.
	 */
	private float moveSpeed = 10f;
	/**
	 * The displacement for each axis.
	 */
	private double forward, side, elevate;
	
	private final Matrix3f rotationTemp = new Matrix3f();
	
	private final Vector3f store = new Vector3f();
	
	/**
	 * Please use {@link CameraControl#newFirstPersonControl(Camera)}.
	 * 
	 * @param camera The camera to control.
	 */
	FirstPersonCamControl(Camera camera) {
		this.camera = camera;
	}
	
	@Override
	public void rotate(double dx, double dy) {
		if(dx != 0) {
			rotationTemp.fromAngleAxis((float) (rotateSpeed * dx), Vector3f.UNIT_Y);
			rotationTemp.applyPost(camera.getLeft(), store);
			camera.setLeft(store);
			rotationTemp.applyPost(camera.getDirection(), store);
		    camera.setDirection(store);
		    rotationTemp.applyPost(camera.getUp(), store);
		    camera.setUp(store);
		}
		
		if(dy != 0) {
			rotationTemp.fromAngleAxis((float) (rotateSpeed * dy), camera.getLeft());
			rotationTemp.applyPost(camera.getDirection(), store);
			camera.setDirection(store);
		    rotationTemp.applyPost(camera.getUp(), store);
		    camera.setUp(store);
		}
	}
	
	@Override
	public void update(ReadableTimer timer) {
	    if (forward != 0 || side != 0 || elevate != 0) {
	        ReadableVector3f loc = camera.getLocation();
	        ReadableVector3f dir = camera.getDirection();
	        ReadableVector3f up = camera.getUp();
	        ReadableVector3f left = camera.getLeft();
	        store.zero();
	        if (forward == 1) {
	            store.add(dir);
	        } else if (forward == -1) {
	            store.sub(dir);
	        }
	        if (elevate == 1) {
	            store.add(up);
	        } else if (elevate == -1) {
                store.sub(up);
            }
	        if (side == 1) {
                store.add(left);
            } else if (side == -1) {
                store.sub(left);
            }
	        
	        store.normalize().mul(moveSpeed * timer.getTimePerFrame()).add(loc);
	        camera.setLocation(store);
	        
	        forward = 0;
	        elevate = 0;
	        side = 0;
	    }
	}

	@Override
    public void move(double dx, double dy, double dz) {
        this.forward = dx;
        this.elevate = dy;
        this.side = dz;
    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    public void setCamera(Camera camera) {
        this.camera = camera;
    }
}
