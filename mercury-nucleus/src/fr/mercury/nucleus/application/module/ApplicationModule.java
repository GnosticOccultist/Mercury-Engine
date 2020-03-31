package fr.mercury.nucleus.application.module;

import fr.mercury.nucleus.application.Application;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>ApplicationModule</code> represents an addon which rely only on the existence of an {@link Application}. It contains
 * a set of methods which follows the application life.
 * 
 * @author GnosticOccultist
 */
public interface ApplicationModule {
	
	/**
	 * Initialize the <code>ApplicationModule</code>.
	 * 
	 * @param application The application which owns the module.
	 */
	@OpenGLCall
	void initialize(Application application);
	
	/**
	 * Update the <code>ApplicationModule</code> during every loop-cycle if the module is both
	 * linked to an application and enabled.
	 * 
	 * @param timer The timer used by the application (not null).
	 */
	@OpenGLCall
	void update(ReadableTimer timer);
	
	/**
	 * Cleanup the <code>ApplicationModule</code> and de-initialized it.
	 */
	@OpenGLCall
	void cleanup();
	
	/**
	 * Return whether the <code>ApplicationModule</code> has been initialized with its 
	 * current <code>Application</code>.
	 * 
	 * @return Whether the module has been initialized.
	 */
	boolean isInitialized();
	
	/**
	 * Return whether the <code>ApplicationModule</code> is enabled.
	 * 
	 * @return Whether the module is enabled.
	 */
	boolean isEnabled();
	
	/**
	 * Enable the <code>ApplicationModule</code>.
	 */
	void enable();
	
	/**
	 * Disable the <code>ApplicationModule</code>.
	 */
	void disable();
}
