package fr.mercury.nucleus.application.module;

import fr.mercury.nucleus.application.Application;

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
	void initialize(Application application);
	
	/**
	 * Update the <code>ApplicationModule</code> during every loop-cycle if the module is both
	 * linked to an application and enabled.
	 * 
	 * @param tpf The time per frame in seconds.
	 */
	void update(float tpf);
	
	/**
	 * Cleanup the <code>ApplicationModule</code>.
	 */
	void cleanup();
	
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
