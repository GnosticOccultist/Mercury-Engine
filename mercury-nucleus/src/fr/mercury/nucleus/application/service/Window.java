package fr.mercury.nucleus.application.service;

import fr.mercury.nucleus.application.Application;

/**
 * <code>Window</code> is an {@link ApplicationService} which manages a window for an {@link Application}.
 * 
 * @author GnosticOccultist
 */
public interface Window extends ApplicationService {

    /**
     * Make the rendering context of the <code>Window</code> current.
     */
    void makeContextCurrent();

    /**
     * Set whether the <code>Window</code> should use vertical synchronization.
     * 
     * @param vSync Whether to use V-Sync for the window.
     */
    void useVSync(boolean vSync);

    /**
     * Return whether the <code>Window</code> should close itself.
     * 
     * @return Whether the window should close.
     */
    boolean shouldClose();

    /**
     * Finish the frame when rendering has been done.
     */
    void finishFrame();

    /**
     * Destroy the <code>Window</code> when the {@link Application} is closing or
     * restarting.
     */
    void destroy();

    /**
     * @deprecated Cleanup method isn't used by the window. See {@link #destroy()}.
     */
    @Override
    @Deprecated
    default void cleanup() {
    }

    /**
     * Show the <code>Window</code> on screen by making it visible.
     */
    void show();

    /**
     * Return the identifier of the <code>Window</code>.
     * 
     * @return The native identifier of the window.
     */
    long getID();
}
