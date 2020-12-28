package fr.mercury.nucleus.application;

import java.lang.StackWalker.Option;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.application.service.ApplicationService;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>AbstractApplicationService</code> is an abstract implementation of {@link ApplicationService}. It should
 * be used as a base for every service implementations.
 * 
 * @author GnosticOccultist
 */
public abstract class AbstractApplicationService implements ApplicationService {

	/**
	 * The application which uses the service.
	 */
	protected Application application;
	/**
	 * Whether the service is initialized.
	 */
	private boolean initialized;

	/**
	 * Initialize the <code>AbstractApplicationService</code> using the specified {@link MercurySettings}.
	 * The {@link #setApplication(Application)} method needs to be invoked before initialization.
	 * <p>
	 * Invoked internally by the linked {@link Application}.
	 * 
	 * @param settings The settings used by the application.
	 * 
	 * @see #setApplication(Application)
	 */
	public void initialize(MercurySettings settings) {
		assert application != null;
		assert !initialized;

		this.initialized = true;
	}

	/**
	 * Update the <code>AbstractApplicationService</code> using the specified {@link ReadableTimer}. The
	 * class calling this method should implements the {@link Application} interface.
	 * <p>
	 * Invoked internally by the linked {@link Application}.
	 * 
	 * @param timer The readable only timer (not null).
	 */
	public void update(ReadableTimer timer) {
		assert application != null;
		assert initialized;

		var caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (!checkCaller(caller)) {
			throw new IllegalStateException("Update method must be called by an Application instance only!");
		}
	}

	/**
	 * Set the {@link Application} linked to the <code>AbstractApplicationService</code>.
	 * 
	 * @param The application linked to the service (not null).
	 */
	public void setApplication(Application application) {
		Validator.nonNull(application, "The application can't be null!");
		this.application = application;
	}

	/**
	 * Return whether the <code>AbstractApplicationService</code> is already initialzed.
	 * 
	 * @return Whether the service is initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Cleanup the <code>AbstractApplicationService</code> when no longer used or when
	 * the {@link Application} is closing.
	 */
	public void cleanup() {
		assert initialized;

		this.initialized = false;
	}

	/**
	 * Return whether the provided caller, which has called a method of the <code>AbstractApplicationService</code>,
	 * is valid, meaning it implements {@link Application} or is the service itself.
	 * 
	 * @param caller The class calling a method (not null).
	 * @return		 Whether the caller is valid.
	 */
	private boolean checkCaller(Class<?> caller) {
		var result = false;

		// Check that the caller isn't the service itself (super invokation).
		if (caller == getClass()) {
			return true;
		}

		// Check that the caller implements the Application interface.
		var interfaces = caller.getInterfaces();
		for (var inter : interfaces) {
			if (inter == Application.class) {
				result = true;
			}
		}

		return result;
	}
}
