package fr.mercury.nucleus.application.service;

import fr.mercury.nucleus.application.Application;
import fr.mercury.nucleus.application.MercurySettings;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>ApplicationService</code> is an interface to implement a service to use with an {@link Application}.
 * 
 * @author GnosticOccultist
 */
public interface ApplicationService {

    /**
     * Initialize the <code>ApplicationService</code> using the specified {@link MercurySettings}. 
     * The {@link #setApplication(Application)} method needs to be invoked before initialization.
     * <p>
     * Invoked internally by the linked {@link Application}.
     * 
     * @param settings The settings used by the application.
     * 
     * @see #setApplication(Application)
     */
    void initialize(MercurySettings settings);

    /**
     * Update the <code>ApplicationService</code> using the specified {@link ReadableTimer}. 
     * The class calling this method should implements the {@link Application} interface.
     * <p>
     * Invoked internally by the linked {@link Application}.
     * 
     * @param timer The readable only timer (not null).
     */
    void update(ReadableTimer timer);

    /**
     * Cleanup the <code>ApplicationService</code> when no longer used or when the
     * {@link Application} is closing.
     */
    void cleanup();

    /**
     * Return whether the <code>ApplicationService</code> is already initialzed.
     * 
     * @return Whether the service is initialized.
     */
    boolean isInitialized();
    
    /**
     * Return the {@link Application} linked to the <code>ApplicationService</code>.
     * 
     * @return The application linked to the service, or null if unlinked.
     */
    Application getApplication();

    /**
     * Set the {@link Application} linked to the <code>ApplicationService</code>.
     * 
     * @param The application linked to the service, or null if unlinked.
     */
    void setApplication(Application application);
}
