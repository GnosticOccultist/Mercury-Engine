package fr.mercury.nucleus.application;

import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>Application</code> is an interface to easily implement a usable application,
 * by creating the essentials functions.
 * 
 * @author GnosticOccultist
 */
public interface Application {
	
	/**
	 * Initialize the <code>Application</code> by creating the needed content to run.
	 * <p>
	 * It is mostly used to initialize any OpenGL objects or scene-graph requirements.
	 */
	@OpenGLCall
	void internalInitialize();
	
	/**
	 * Update the <code>Application</code> state and render the scene to the back buffer.
	 * <p>
	 * All the updating and rendering logic happen here.
	 */
	@OpenGLCall
	void internalUpdate();
	
	/**
	 * Cleanup the application when the context has been ask for destruction.
	 * <p>
	 * It is mostly used to delete and free any OpenGL objects ensuring a clean shutdown
	 * of the <code>Application</code>.
	 */
	@OpenGLCall
	void cleanup();
	
	/**
	 * Notify the <code>Application</code> about a resizing <code>MercuryContext</code>.
	 * The new width and height are passed as arguments.
	 * 
	 * @param width  The new width.
	 * @param height The new height.
	 */
	void resize(int width, int height);
	
	/**
	 * Notify the <code>Application</code> about a maximized or focused 
	 * <code>MercuryContext</code>.
	 * <p>
	 * By default the function doesn't do anything and is purely optional,
	 * you can implement your own code by overriding the function.
	 */
	default void gainFocus() {}

	/**
	 * Notify the <code>Application</code> about a minimized or unfocused 
	 * <code>MercuryContext</code>.
	 * <p>
	 * By default the function doesn't do anything and is purely optional,
	 * you can implement your own code by overriding the function.
	 */
	default void looseFocus() {}
}
