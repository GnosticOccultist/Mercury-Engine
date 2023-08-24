package fr.mercury.nucleus.input.control;

import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>CameraControl</code> is an interface to implement a controlled {@link Camera}, for
 * example with inputs.
 * 
 * @author GnosticOccultist
 */
public interface CameraControl {
    
    /**
     * Creates a new {@link FirstPersonCamControl} for the provided {@link Camera}.
     * 
     * @param camera The camera to control.
     * @return       A new first person camera controller.
     */
    static FirstPersonCamControl newFirstPersonControl(Camera camera) {
        var control = new FirstPersonCamControl(camera);
        return control;
    }

    /**
     * Update the <code>CameraControl</code> by applying a new rotation and/or location
     * to the controlled {@link Camera}.
     * 
     * @param timer The The readable only timer (not null).
     */
    @OpenGLCall
    void update(ReadableTimer timer);
    
    /**
     * Apply movement to the controlled {@link Camera}.
     * 
     * @param dx The delta movement in the X-axis.
     * @param dy The delta movement in the Y-axis.
     * @param dz The delta movement in the Z-axis.
     */
    void move(double dx, double dy, double dz);
    
    /**
     * Apply rotation to the controlled {@link Camera}.
     * 
     * @param dx The rotation amount in the X-axis.
     * @param dy The rotation amount in the Y-axis.
     */
    void rotate(double dx, double dy);
    
    /**
     * Return the {@link Camera} controlled by the <code>CameraControl</code>.
     * 
     * @return The controlled camera (not null).
     */
    Camera getCamera();
    
    /**
     * Set the {@link Camera} controlled by the <code>CameraControl</code>.
     * 
     * @param camera The camera to control (not null).
     */
    void setCamera(Camera camera);
}
