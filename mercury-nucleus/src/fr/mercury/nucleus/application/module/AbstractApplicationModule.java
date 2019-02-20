package fr.mercury.nucleus.application.module;

import fr.mercury.nucleus.application.Application;

/**
 * <code>AbstractApplicationModule</code> is an abstract implementation of {@link ApplicationModule} and should be
 * extended by all modules implementations because it provides working initialization and enabling state.
 * 
 * @author GnosticOccultist
 */
public abstract class AbstractApplicationModule implements ApplicationModule {
	
	/**
	 * Whether the module is enabled.
	 */
	private boolean enabled;
	/**
	 * Whether the module has been initialized.
	 */
	private boolean initialized;
	
	/**
	 * Instantiates a new <code>AbstractApplicationModule</code> which enabled at creation.
	 */
	public AbstractApplicationModule() {
		this(true);
	}
	
	/**
	 * Instantiates a new <code>AbstractApplicationModule</code>.
	 * 
	 * @param enabled Whether to enable the module at creation.
	 */
	public AbstractApplicationModule(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public void initialize(Application application) {
		this.initialized = true;
	}
	
	@Override
	public void cleanup() {
		this.initialized = false;
	}
	
	@Override
	public boolean isInitialized() {
		return initialized;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public void enable() {
		this.enabled = true;
	}
	
	@Override
	public void disable() {
		this.enabled = false;
	}
}
