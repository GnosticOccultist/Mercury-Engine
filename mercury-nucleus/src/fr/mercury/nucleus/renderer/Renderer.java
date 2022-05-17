package fr.mercury.nucleus.renderer;

import fr.mercury.nucleus.application.Application;
import fr.mercury.nucleus.application.service.ApplicationService;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.renderer.queue.RenderBucket;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>Renderer</code> is an interface extending {@link ApplicationService} to allow an {@link Application}
 * to render a scenegraph.
 * 
 * @see #renderScene(NucleusMundi)
 * 
 * @author GnosticOccultist
 */
public interface Renderer extends ApplicationService {

    /**
     * Resize the {@link Camera} viewport dimensions to the provided width and height of the framebuffer, 
     * and update the <code>OpenGL</code> scissor test to discard any fragment outside the dimension 
     * of the rectangle.
     * 
     * @param width  The new width in pixel coordinates (&gt;0).
     * @param height The new height in pixel coordinates (&gt;0).
     */
    void resize(int width, int height);

    /**
     * Sets the clear values of the <code>Renderer</code> for the color-buffer.
     * 
     * @param color The color to be cleared from the buffer (not null).
     */
    @OpenGLCall
    void setClearColor(Color color);
    
    /**
     * Return the {@link Camera} used for rendering in the <code>Renderer</code>.
     * 
     * @param camera The camera to render with (not null).
     */
    Camera getCamera();

    /**
     * Sets the {@link Camera} used for rendering in the <code>Renderer</code>. If the renderer 
     * a {@link RenderBucket} pipeline, it should apply the new camera to the buckets.
     * 
     * @param camera The camera to render with (not null).
     */
    void setCamera(Camera camera);

    /**
     * Render the provided {@link NucleusMundi} and each of its children.
     * 
     * @param scene The scene root to render (not null).
     */
    void renderScene(NucleusMundi scene);
}
