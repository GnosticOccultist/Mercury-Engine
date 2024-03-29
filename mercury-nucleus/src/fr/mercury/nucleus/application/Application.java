package fr.mercury.nucleus.application;

import java.util.Optional;

import fr.alchemy.utilities.task.actions.BooleanAction;
import fr.alchemy.utilities.task.actions.VoidAction;
import fr.mercury.nucleus.application.service.ApplicationService;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>Application</code> is an interface to easily implement a usable
 * application, by creating the essentials functions.
 * 
 * @author GnosticOccultist
 */
public interface Application {

    /**
     * Initialize the <code>Application</code> by creating the needed content to
     * run.
     * <p>
     * It is mostly used to initialize any OpenGL objects or scene-graph
     * requirements.
     */
    @OpenGLCall
    void internalInitialize();

    /**
     * Update the <code>Application</code> state and render the scene to the back
     * buffer.
     * <p>
     * All the updating and rendering logic happen here.
     */
    @OpenGLCall
    void internalUpdate();

    /**
     * Performs some actions with the <code>Application</code> once the front and
     * back buffer have been swapped, meaning the rendered frame is visible on the
     * window.
     */
    @OpenGLCall
    void postFrame();

    /**
     * Cleanup the <code>Application</code> when the context has been ask for
     * destruction.
     * <p>
     * It is mostly used to delete and free any OpenGL objects ensuring a clean
     * shutdown of the application.
     */
    @OpenGLCall
    void cleanup();

    /**
     * Restart the <code>Application</code>, applying the new
     * {@link MercurySettings} to the {@link MercuryContext} and restarting it.
     */
    void restart();

    /**
     * Notify the <code>Application</code> about a resizing
     * <code>MercuryContext</code>. The new width and height of the framebuffer are
     * passed as arguments in pixel coordinates.
     * 
     * @param width  The new width in pixel coordinates (&gt;0).
     * @param height The new height in pixel coordinates (&gt;0).
     */
    void resize(int width, int height);
    
    /**
     * Return the {@link MercuryContext} bound to the <code>Application</code>.
     * 
     * @return The context, or null if no context is bound yet.
     */
    MercuryContext getContext();

    /**
     * Return the {@link MercurySettings} of the <code>Application</code>.
     * 
     * @return The settings used to create the context (not null).
     */
    MercurySettings getSettings();

    /**
     * Return an optional value of an {@link ApplicationService} matching the
     * provided type linked to the <code>Application</code>.
     * <p>
     * This function is supposed to be used to access the module, however it
     * shouldn't be used to detach it from the application, use
     * {@link #unlinkService(ApplicationService)} instead.
     * 
     * @param type The type of service to return.
     * @return An optional value containing either a service matching the given
     *         type, or nothing if none is linked to the application.
     * 
     * @see #getService(Class)
     */
    default <M extends ApplicationService> Optional<M> getOptionalService(Class<M> type) {
        return Optional.ofNullable(getService(type));
    }

    /**
     * Return an {@link ApplicationService} matching the provided type linked to the
     * <code>Application</code>.
     * 
     * @param type The type of service to return.
     * @return A service matching the given type, or null if none is linked to the
     *         application.
     * 
     * @see #getOptionalService(Class)
     */
    <M extends ApplicationService> M getService(Class<M> type);

    /**
     * Links the provided {@link ApplicationService} to the
     * <code>Application</code>.
     * 
     * @param service The service to be linked (not null).
     */
    void linkService(ApplicationService service);

    /**
     * Unlink the provided {@link ApplicationService} from the
     * <code>Application</code>.
     * 
     * @param service The service to be unlinked (not null).
     * 
     * @return Whether the service was unlinked.
     */
    boolean unlinkService(ApplicationService service);

    /**
     * Perform the given {@link VoidAction} with a linked {@link ApplicationService}
     * of the matching type if any.
     * 
     * @param type   The type of application service (not null).
     * @param action The action to perform (not null).
     * 
     * @param <M>    The type of application service.
     */
    default <M extends ApplicationService> void service(Class<M> type, VoidAction<M> action) {
        var service = getService(type);
        if (service != null) {
            action.accept(service);
        }
    }

    /**
     * Perform the given {@link BooleanAction} with a linked
     * {@link ApplicationService} of the matching type if any.
     * 
     * @param type   The type of application service (not null).
     * @param action The action to perform (not null).
     * 
     * @return Whether the action was performed and returned <code>true</code>.
     * 
     * @param <M> The type of application service.
     */
    default <M extends ApplicationService> boolean checkService(Class<M> type, BooleanAction<M> action) {
        var service = getService(type);
        return service != null && action.perform(service);
    }

    /**
     * Notify the <code>Application</code> about a maximized or focused
     * {@link MercuryContext}.
     * <p>
     * By default the function doesn't do anything and is purely optional, you can
     * implement your own code by overriding the function.
     */
    default void gainFocus() {
    }

    /**
     * Notify the <code>Application</code> about a minimized or unfocused
     * {@link MercuryContext}.
     * <p>
     * By default the function doesn't do anything and is purely optional, you can
     * implement your own code by overriding the function.
     */
    default void looseFocus() {
    }
}
