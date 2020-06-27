package fr.mercury.nucleus.application;

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
	 * Cleanup the application when the context has been ask for destruction.
	 * <p>
	 * It is mostly used to delete and free any OpenGL objects ensuring a clean
	 * shutdown of the <code>Application</code>.
	 */
	@OpenGLCall
	void cleanup();

	/**
	 * Notify the <code>Application</code> about a resizing
	 * <code>MercuryContext</code>. The new width and height of the framebuffer are
	 * passed as arguments in pixel coordinates.
	 * 
	 * @param width  The new width in pixel coordinates (&gt;0).
	 * @param height The new height in pixel coordinates (&gt;0).
	 */
	void resize(int width, int height);

	MercurySettings getSettings();

	/**
	 * Return an {@link ApplicationModule} matching the provided type linked to the
	 * <code>Application</code>.
	 * 
	 * @param type The type of module to return.
	 * @return A module matching the given type, or null if none is linked to the
	 *         application.
	 */
	<M extends ApplicationService> M getService(Class<M> type);

	/**
	 * Links the provided {@link ApplicationModule} to the <code>Application</code>.
	 * 
	 * @param module The module to be linked.
	 */
	void linkService(ApplicationService module);

	default <M extends ApplicationService> void service(Class<M> type, VoidAction<M> action) {
		var service = getService(type);
		if (service != null) {
			action.accept(service);
		}
	}

	default <M extends ApplicationService> boolean checkService(Class<M> type, BooleanAction<M> action) {
		var service = getService(type);
		return service != null && action.perform(service);
	}

	/**
	 * Notify the <code>Application</code> about a maximized or focused
	 * <code>MercuryContext</code>.
	 * <p>
	 * By default the function doesn't do anything and is purely optional, you can
	 * implement your own code by overriding the function.
	 */
	default void gainFocus() {}

	/**
	 * Notify the <code>Application</code> about a minimized or unfocused
	 * <code>MercuryContext</code>.
	 * <p>
	 * By default the function doesn't do anything and is purely optional, you can
	 * implement your own code by overriding the function.
	 */
	default void looseFocus() {}
}
