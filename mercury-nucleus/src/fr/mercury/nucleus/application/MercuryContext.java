package fr.mercury.nucleus.application;

import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GLCapabilities;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.service.GLFWWindow;
import fr.mercury.nucleus.application.service.Window;
import fr.mercury.nucleus.input.GLFWKeyInput;
import fr.mercury.nucleus.input.GLFWMouseInput;
import fr.mercury.nucleus.renderer.device.PhysicalDevice;
import fr.mercury.nucleus.renderer.device.Vendor;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.NanoTimer;
import fr.mercury.nucleus.utils.Timer;

/**
 * <code>GLFWContext</code> is a wrapper class to handle the creation of GLFW
 * context and LWJGL initialization.
 * <p>
 * It also contains the <code>Application</code> main-loop with update and
 * render methods.
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
	protected static final String GL_THREAD_NAME = "OpenGL Render Thread";
	/**
	 * A reference of the rendering thread or main thread.
	 */
	protected static Thread GL_THREAD_REFERENCE;

	/**
	 * The application which manages the context.
	 */
	private Application application;
	/**
	 * The window used by the context, or null for headless context.
	 */
	private Window window;
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
	 * The timer using to calculate the sleeping time.
	 */
	private Timer timer;
	/**
	 * The frame-rate limit.
	 */
	private int frameRateLimit = -1;
	/**
	 * The sleeping time of the frame.
	 */
	private double frameSleepTime;
	/**
	 * The mouse input handler.
	 */
	private GLFWMouseInput mouseInput;
	/**
	 * The mouse input handler.
	 */
	private GLFWKeyInput keyInput;
	/**
	 * The physical device used for rendering.
	 */
	private PhysicalDevice physicalDevice;

	/**
	 * Instantiates and return the <code>MercuryContext</code> bound to the provided
	 * application and using the provided <code>MercurySettings</code>.
	 * 
	 * @param application The application to bound to.
	 * @param settings    The settings.
	 * @return The new context.
	 */
	public static MercuryContext newContext(Application application, MercurySettings settings) {

		MercuryContext context = new MercuryContext();
		context.setSettings(settings);
		context.setApplication(application);

		return context;
	}

	/**
	 * Internal use only.
	 * <p>
	 * Please use {@link #newContext(Application, MercurySettings)} to create the
	 * <code>MercuryContext</code>.
	 */
	private MercuryContext() {
	}

	public void initialize() {
		if (initialized.get()) {
			logger.warning("The context is already initialized!");
			return;
		}

		run();
	}

	/**
	 * Restart the context to apply new settings. The context should first be
	 * initialized.
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
		Thread.currentThread().setName(GL_THREAD_NAME);
		GL_THREAD_REFERENCE = Thread.currentThread();

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
		GL11C.glFinish();
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
		GL11C.glFlush();

		application.service(GLFWWindow.class, GLFWWindow::finishFrame);

		application.postFrame();

		if (frameRateLimit != settings.getFrameRate()) {
			setFrameRateLimit(settings.getFrameRate());
		}

		if (frameRateLimit > 0) {
			var sleep = frameSleepTime - (timer.getTimePerFrame() / 1000.0);
			var sleepMillis = (long) sleep;
			var additionalNanos = (int) ((sleep - sleepMillis) * 1000000.0);

			if (sleepMillis >= 0 && additionalNanos >= 0) {
				try {
					Thread.sleep(sleepMillis, additionalNanos);
				} catch (InterruptedException ignored) {
					// Just ignore...
				}
			}
		}
	}

	/**
	 * Initialize the LWJGL display in OpenGL Thread.
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean initializeInMercury() {
		try {

			timer = new NanoTimer();

			createContext(settings);

			mouseInput = new GLFWMouseInput(this);
			mouseInput.initialize();

			keyInput = new GLFWKeyInput(this);
			keyInput.initialize();

			initialized.set(true);

		} catch (Exception ex) {

			// Creation failed destroying the context
			// and stopping there.
			application.service(GLFWWindow.class, GLFWWindow::destroy);
			ex.printStackTrace();
			return false;
		}

		application.internalInitialize();
		return true;
	}

	private void createContext(MercurySettings settings) {

		window = new GLFWWindow();
		application.linkService((AbstractApplicationService) window);
		window.initialize(settings);

		// Make the OpenGL context current.
		window.makeContextCurrent();
		GLCapabilities capabilities = GL.createCapabilities();

		// Once the OpenGL context is set, change vSync if needed.
		window.useVSync(settings.isVSync());

		/*
		 * Once we have set the current OpenGL context we can access informations about
		 * our device.
		 */
		physicalDevice = createPhysicalDevice(capabilities);

		logger.info("Using physical device: \n" + physicalDevice);
		physicalDevice.check(settings);

		if (settings.getInteger("Samples") != 0) {
			GL11.glEnable(GL13.GL_MULTISAMPLE);
		}

		// Enable depth testing.
		GL11C.glEnable(GL11C.GL_DEPTH_TEST);
		GL11C.glDepthFunc(GL11C.GL_LEQUAL);

		// Finally show the window when finished.
		window.show();
	}

	/**
	 * Creates a new {@link PhysicalDevice} for the current
	 * <code>MercuryContext</code>.
	 * 
	 * @param capabilites The OpenGL context capabilities (not null).
	 * @return A new physical instance containing device and renderer infos (not
	 *         null).
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

	private void cleanup() {
		application.cleanup();
		application.service(GLFWWindow.class, GLFWWindow::destroy);

		// Reset the state of variables.
		timer = null;
		initialized.set(false);
	}

	/**
	 * Check that the currently used {@link Thread} is the one used by the
	 * <code>OpenGL</code> context for rendering.
	 * 
	 * @throws GLException Thrown if the current thread isn't the rendering one.
	 */
	public static void checkGLThread() {
		if (GL_THREAD_REFERENCE != Thread.currentThread()) {
			throw new GLException("The method should only be called from '" + GL_THREAD_REFERENCE + "'!");
		}
	}

	/**
	 * Return whether the currently used {@link Thread} is the one used by the
	 * <code>OpenGL</code> context for rendering.
	 * 
	 * @return Whether the current thread is the rendering one.
	 */
	public static boolean isGLThread() {
		return GL_THREAD_REFERENCE == Thread.currentThread();
	}

	/**
	 * Set the settings used by the context to the provided ones. It copies the
	 * settings without altering the provided instance of
	 * <code>MercurySettings</code>.
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
		this.frameSleepTime = 1000.0 / this.frameRateLimit;
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
	 * Return the width of the <code>MercuryContext</code>'s window.
	 * 
	 * @return The width of the context window.
	 */
	public int getWidth() {
		return settings.getWidth();
	}

	/**
	 * Return the height of the <code>MercuryContext</code>'s window.
	 * 
	 * @return The height of the context window.
	 */
	public int getHeight() {
		return settings.getHeight();
	}

	/**
	 * Return the {@link GLFWMouseInput} for the <code>MercuryContext</code>.
	 * 
	 * @return The GLFW mouse input.
	 */
	public GLFWMouseInput getMouseInput() {
		return mouseInput;
	}

	/**
	 * Return the {@link GLFWKeyInput} for the <code>MercuryContext</code>.
	 * 
	 * @return The GLFW key input.
	 */
	public GLFWKeyInput getKeyInput() {
		return keyInput;
	}

	public long getWindowID() {
		return window.getID();
	}
}
