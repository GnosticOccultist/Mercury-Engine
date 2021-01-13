package fr.mercury.nucleus.application;

import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLCapabilities;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.service.GLFWWindow;
import fr.mercury.nucleus.input.BaseInputProcessor;
import fr.mercury.nucleus.input.GLFWKeyInput;
import fr.mercury.nucleus.input.GLFWMouseInput;
import fr.mercury.nucleus.renderer.device.PhysicalDevice;
import fr.mercury.nucleus.renderer.device.Vendor;
import fr.mercury.nucleus.utils.MercuryException;

/**
 * <code>MercuryContext</code> represent the core layer of an {@link Application}. It contains the main-loop within 
 * which the update and render logic take place.
 * <p>
 * The usage of a context is defined by its {@link Type}, for example some context type can't display a window, 
 * handle inputs or play sounds, like the {@link Type#HEADLESS}.
 * Such type should be provided in the {@link MercurySettings}, using the {@link MercurySettings#setContextType(Type)} 
 * method and before starting the application.
 * 
 * @author GnosticOccultist
 */
public class MercuryContext implements Runnable {

    /**
     * The application logger.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.app");
    /**
     * The name of the rendering thread or main thread.
     */
    protected static final String MAIN_THREAD_NAME = "Main/Render Thread";
    /**
     * A reference of the rendering thread or main thread.
     */
    protected static Thread MAIN_THREAD_REFERENCE;

    /**
     * The application which manages the context.
     */
    private Application application;
    /**
     * The type of context.
     */
    private Type type;
    /**
     * The general settings.
     */
    private MercurySettings settings = new MercurySettings(true);
    /**
     * Whether the context is initialized.
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    /**
     * The boolean to notify about a needed restart.
     */
    private final AtomicBoolean needRestart = new AtomicBoolean(false);
    /**
     * The frame-rate limit.
     */
    private int frameRateLimit = -1;
    /**
     * The physical device used for rendering, or null for headless context.
     */
    private PhysicalDevice physicalDevice;

    /**
     * Instantiates and return a new <code>MercuryContext</code> bound to the provided {@link Application}. 
     * The context is created accordingly to the given {@link MercurySettings}.
     * 
     * @param application The application to bound the context to (not null).
     * @param settings    The settings to use for creation (not null).
     * @return            A new context instance (not null).
     */
    public static MercuryContext newContext(Application application, MercurySettings settings) {
        Validator.nonNull(application, "The application can't be null!");
        Validator.nonNull(settings, "The settings can't be null!");

        var context = new MercuryContext();
        context.setSettings(settings);
        context.setApplication(application);

        var type = settings.getContextType();
        context.type = type;
        switch (type) {
        case HEADLESS:
            // No need for window.
            break;
        case WINDOW:
            var window = new GLFWWindow();
            application.linkService(window);
            break;
        default:
            break;
        }

        return context;
    }

    /**
     * Internal use only.
     * <p>
     * Please use {@link #newContext(Application, MercurySettings)} to create the
     * <code>MercuryContext</code>.
     */
    private MercuryContext() {}

    /**
     * Initialize the <code>MercuryContext</code> if it hasn't been already. The
     * method will start the main-loop of the application.
     * 
     * @see #restart()
     */
    public void initialize() {
        if (initialized.get()) {
            logger.warning("The context is already initialized!");
            return;
        }

        run();
    }

    /**
     * Restart the <code>MercuryContext</code> to apply new {@link MercurySettings}.
     * The context should first be initialized.
     * 
     * @see #initialize()
     * @see #setSettings(MercurySettings)
     */
    public void restart() {
        if (initialized.get()) {
            needRestart.set(true);
        } else {
            logger.warning("The context isn't initialized, cannot restart!");
        }
    }

    @Override
    public void run() {

        // Set the correct name for the thread and keep a reference for checking
        // purposes.
        Thread.currentThread().setName(MAIN_THREAD_NAME);
        MAIN_THREAD_REFERENCE = Thread.currentThread();

        if (application == null) {
            throw new IllegalArgumentException("The bounded application cannot be null !");
        }

        if (!initializeInMercury()) {
            logger.error("The context initialization failed. Stopping...");
            return;
        }

        while (true) {

            runLoop();

            if (application.checkService(GLFWWindow.class, w -> w.shouldClose())) {
                break;
            }
        }

        cleanup();
        /*
         * Wait until all GL commands are executed.
         */
        ifRenderable(GL11C::glFinish);
    }

    /**
     * Execute a single iteration over the rendering and updating logic inside the
     * OpenGL Thread.
     */
    private void runLoop() {
        // If a restart is required, recreate the context.
        if (needRestart.getAndSet(false)) {
            try {
                logger.info("Restarting the application: " + application.getClass().getSimpleName());
                application.service(GLFWWindow.class, GLFWWindow::destroy);
                createContext(settings);
            } catch (Exception ex) {
                logger.error("Failed to set display settings!", ex);
            }
        }

        if (!initialized.get()) {
            throw new IllegalStateException();
        }

        application.internalUpdate();

        // Try flushing all previous GL commands before swapping buffers.
        ifRenderable(GL11C::glFlush);

        application.service(GLFWWindow.class, GLFWWindow::finishFrame);

        application.postFrame();

        if (frameRateLimit != settings.getFrameRate()) {
            setFrameRateLimit(settings.getFrameRate());
        }

        Sync.sync(frameRateLimit);
    }

    /**
     * Initialize the <code>MercuryContext</code> inside the main {@link Thread} to
     * handle graphics related actions.
     * 
     * @return Whether the context has been successfully initialized.
     * 
     * @throws Exception Thrown if the initialization failed.
     */
    private boolean initializeInMercury() {
        try {

            createContext(settings);

            initialized.set(true);

        } catch (Exception ex) {
            /*
             * Creation failed destroying the window and input processor and stopping there.
             */
            application.service(GLFWWindow.class, GLFWWindow::destroy);
            application.service(BaseInputProcessor.class, BaseInputProcessor::cleanup);
            ex.printStackTrace();
            return false;
        }

        application.internalInitialize();
        return true;
    }

    private void createContext(MercurySettings settings) {
        // The context doesn't need a window, nor a renderer, nor input.
        var window = application.getService(GLFWWindow.class);
        if (window == null) {
            return;
        }

        window.initialize(settings);

        // Make the OpenGL context current.
        window.makeContextCurrent();

        // Once the OpenGL context is set, change vSync if needed.
        window.useVSync(settings.isVSync());

        GLCapabilities capabilities = GL.createCapabilities();

        /*
         * Once we have set the current OpenGL context we can access informations about
         * our device.
         */
        physicalDevice = createPhysicalDevice(capabilities);

        logger.info("Using physical device: \n" + physicalDevice);
        physicalDevice.check(settings);

        if (settings.getInteger("Samples") > 1) {
            // TODO: Don't know if it is useful.
            GL11C.glEnable(GL43C.GL_MULTISAMPLE);
        }

        // Finally show the window when finished.
        window.show();

        var glfwWindow = (GLFWWindow) window;

        var mouseInput = new GLFWMouseInput(glfwWindow);
        mouseInput.initialize();

        var keyInput = new GLFWKeyInput(glfwWindow);
        keyInput.initialize();

        // Initialize input processor with window input handlers.
        var inputProcessor = new BaseInputProcessor(mouseInput, keyInput);
        application.linkService(inputProcessor);
    }

    /**
     * Creates a new {@link PhysicalDevice} for the current <code>MercuryContext</code>.
     * 
     * @param capabilites The OpenGL context capabilities (not null).
     * @return            A new physical instance containing device and renderer infos (not null).
     */
    private PhysicalDevice createPhysicalDevice(GLCapabilities capabilites) {
        Validator.nonNull(capabilites, "The capabilities of the OpenGL context can't be null!");

        Vendor vendor = Vendor.fromGLVendor(GL11C.glGetString(GL11C.GL_VENDOR));
        String device = GL11C.glGetString(GL11C.GL_RENDERER);
        String version = GL11C.glGetString(GL11C.GL_VERSION);

        int count = GL11C.glGetInteger(GL30C.GL_NUM_EXTENSIONS);
        String[] extensions = new String[count];
        for (int i = 0; i < count; i++) {
            extensions[i] = GL30C.glGetStringi(GL11C.GL_EXTENSIONS, i);
        }

        return new PhysicalDevice(vendor, device, version, extensions, capabilites);
    }

    /**
     * Cleanup the <code>MercuryContext</code> and all of its sub-services.
     * This method is called automatically when exiting the main-loop.
     */
    private void cleanup() {
        application.cleanup();
        application.service(GLFWWindow.class, GLFWWindow::destroy);

        // Reset the state of variables.
        initialized.set(false);
    }

    /**
     * Check that the currently used {@link Thread} is the main one, usually the one
     * where rendering needs to occur.
     * 
     * @throws MercuryException Thrown if the current thread isn't the main one.
     */
    public static void checkMainThread() {
        if (MAIN_THREAD_REFERENCE != Thread.currentThread()) {
            throw new MercuryException("The method should only be called from '" + MAIN_THREAD_REFERENCE + "'!");
        }
    }

    /**
     * Return whether the currently used {@link Thread} is the main one, usually the
     * one where rendering needs to occur.
     * 
     * @return Whether the current thread is the main one.
     */
    public static boolean isMainThread() {
        return MAIN_THREAD_REFERENCE == Thread.currentThread();
    }

    /**
     * Set the settings used by the context to the provided ones. It copies the
     * settings without altering the provided instance of {@link MercurySettings}.
     * <p>
     * Note that the settings won't be applied until you {@link #restart()} the
     * <code>MercuryContext</code>.
     * 
     * @param settings The settings to use.
     */
    public void setSettings(MercurySettings settings) {
        this.settings.copyFrom(settings);
    }

    /**
     * Sets the frame-rate limit and determine the frame sleep time
     * 
     * @param frameRateLimit The frame-rate limit.
     */
    private void setFrameRateLimit(int frameRateLimit) {
        this.frameRateLimit = frameRateLimit;
    }

    /**
     * Bind the context to the specified application.
     * <p>
     * It should be called before creating the context using {@link #initialize()}.
     * 
     * @param application
     */
    public void setApplication(Application application) {
        this.application = application;
    }

    /**
     * Return the {@link Type} of the <code>MercuryContext</code>.
     * 
     * @return The type of context (not null).
     */
    public Type getType() {
        return type;
    }

    /**
     * Execute the provided {@link Runnable} if the <code>MercuryContext</code> is
     * renderable.
     * 
     * @param action The action to execute (not null).
     * 
     * @see Type#isRenderable()
     */
    public void ifRenderable(Runnable action) {
        Validator.nonNull(action, "The action can't be null!");

        var renderable = type.isRenderable();
        if (renderable) {
            action.run();
        }
    }

    /**
     * <code>Type</code> enumerates the different context which can be created along
     * the {@link Application}.
     * 
     * @author GnosticOccultist
     */
    public enum Type {

        /**
         * The window context supports displaying a window in fullscreen or windowed
         * mode, and render graphics onto it. The window is created using native
         * operating system.
         * <p>
         * This type also supports an event-based input system (mouse and keyboard), as
         * well as sound playing and streaming.
         */
        WINDOW,
        /**
         * The headless context doesn't define any renderable surface and doesn't
         * provide any window, input or sound playing or streaming.
         * <p>
         * Generally used to create a server.
         */
        HEADLESS {

            /**
             * Return false, headless context shouldn't be renderable.
             * 
             * @return Always false.
             */
            @Override
            protected boolean isRenderable() {
                return false;
            }
        };

        /**
         * Return whether the <code>Type</code> of context support rendering on a
         * surface.
         * 
         * @return Whether the context supports rendering.
         */
        protected boolean isRenderable() {
            return true;
        }
    }
}
