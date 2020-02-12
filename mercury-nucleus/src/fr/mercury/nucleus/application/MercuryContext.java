package fr.mercury.nucleus.application;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_REFRESH_RATE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.input.GLFWKeyInput;
import fr.mercury.nucleus.input.GLFWMouseInput;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.NanoTimer;
import fr.mercury.nucleus.utils.Timer;

/**
 * <code>GLFWContext</code> is a wrapper class to handle the creation of 
 * GLFW context and LWJGL initialization.
 * <p>
 * It also contains the <code>Application</code> main-loop with update and render methods.
 * 
 * @author GnosticOccultist
 */
public class MercuryContext implements Runnable {
	
	/**
	 * The application logger.
	 */
	private static final Logger logger = FactoryLogger.getLogger("mercury.app");
	/**
	 * A reference of the rendering thread.
	 */
	protected static final String GL_THREAD_NAME = "OpenGL Render Thread";
	
	/**
	 * The application which manages the context.
	 */
	private Application application;
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
	 * Whether the context window is focused or maximized/minimized.
	 */
	private boolean focused;
	/**
	 * The window handle value.
	 */
	private long window = NULL;
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
	 * Instantiates and return the <code>MercuryContext</code> bound to the provided application
	 * and using the provided <code>MercurySettings</code>.
	 * 
	 * @param application The application to bound to.
	 * @param settings	  The settings.
	 * @return			  The new context.
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
	 * Please use {@link #newContext(Application, MercurySettings)} to create 
	 * the <code>MercuryContext</code>.
	 */
	private MercuryContext() {}
	
	public void initialize() {
		if(initialized.get()) {
			logger.warning("The context is already initialized!");
			return;
		}
		
		run();
	}
	
	/**
	 * Restart the context to apply new settings. The context should first
	 * be initialized.
	 */
	public void restart() {
		if(initialized.get()) {
			needRestart.set(true);
		} else {
			logger.warning("The context isn't initialized, cannot restart!");
		}
	}
	
	@Override
	public void run() {
		
		// Set the correct name for the thread.
		Thread.currentThread().setName(GL_THREAD_NAME);
		
		if(application == null) {
			throw new IllegalArgumentException("The bounded application cannot be null !");
		}
		
		if(!initializeInMercury()) {
			logger.error("The context initialization failed. Stopping...");
			return;
		}
		
		while (true) {
			
			runLoop();
			
			if(glfwWindowShouldClose(window)) {
				break;
			}
		}
		
		cleanup();
	}
	
	/**
	 * Execute a single iteration over the rendering and updating logic inside
	 * the OpenGL Thread.
	 */
	private void runLoop() {
    	// If a restart is required, recreate the context.
    	if(needRestart.getAndSet(false)) {
    		try {
    			logger.info("Restarting the application: " + application.getClass().getSimpleName());
    			destroyContext();
    			createContext(settings);
    		} catch (Exception ex) {
    			logger.error("Failed to set display settings!", ex);
    		}
    	}
		
		if(!initialized.get()) {
			throw new IllegalStateException();
		}
		
		application.internalUpdate();
		
		GL11C.glFlush();
		
		glfwSwapBuffers(window);
		
		if(frameRateLimit != settings.getFrameRate()) {
			setFrameRateLimit(settings.getFrameRate());
		}
		
		if(frameRateLimit > 0) {
    		var sleep = frameSleepTime - (timer.getTimePerFrame() / 1000.0);
    		var sleepMillis = (long) sleep;
    		var additionalNanos = (int) ((sleep - sleepMillis) * 1000000.0);
    		
    		if(sleepMillis >= 0 && additionalNanos >= 0) {
    			try {
    				Thread.sleep(sleepMillis, additionalNanos);
    			} catch (InterruptedException ignored) {
    				// Just ignore...
    			}
    		}
		}
		
		glfwPollEvents();
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
			destroyContext();
			ex.printStackTrace();
			return false;
		}
		
		application.internalInitialize();
		return true;
	}
	
	private void createContext(MercurySettings settings) {
		
		// Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();
		
		if(!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW context!");
		}
		
		// Optional, the current window hints are already the default
		glfwDefaultWindowHints();
		
		// The window will stay hidden after creation.
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE); 		    
		// Whether the window is going to be resizable or not based on the configs.
		glfwWindowHint(GLFW_RESIZABLE, settings.isResizable() ? GL_TRUE : GL_FALSE);
		// Set the refresh rate of the window (frequency).
		glfwWindowHint(GLFW_REFRESH_RATE, 60);
		
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		
		// This allow to use OpenGL 3.x and 4.x contexts on OSX.
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		
		long monitor = NULL;
		
		if(settings.isFullscreen()) {
			monitor = glfwGetPrimaryMonitor();
		}
		
		// Getting the resolution of the primary monitor.
		final GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		
		// Make sure the width and the height is superior than 0.
		if(settings.getWidth() <= 0 || settings.getHeight() <= 0) {
			settings.setResolution(videoMode.width(), videoMode.height());
		}
		
		// Create the window.
		window = glfwCreateWindow(settings.getWidth(), 
				settings.getHeight(), settings.getTitle(), monitor, NULL);
		
		if(window == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}
		
		// Setup window size callback to update framebuffers resolutions, view or projection matrix.
		glfwSetWindowSizeCallback(window, (window, width, height) -> {
			settings.setResolution(width, height);
			application.resize(width, height);
		});
		
		// Setup window focus callback to stop updating or rendering when minimized.
		glfwSetWindowFocusCallback(window, (window, focus) -> {
			if(focused != focus) {
				if(!focused) {
					application.gainFocus();
					timer.reset();
				} else {
					application.looseFocus();
				}
				focused = !focused;
			}
		});
		
		// Center the window
		if(!settings.isFullscreen()) {
			glfwSetWindowPos(window,  
					(videoMode.width() - settings.getWidth()) / 2, 
					(videoMode.height() - settings.getHeight()) / 2);
		}
		
		// Make the OpenGL context current.
        glfwMakeContextCurrent(window);
        
        GL.createCapabilities();
        
        if(settings.getInteger("Samples") != 0) {
        	GL11.glEnable(GL13.GL_MULTISAMPLE);
        }
        
        // Enabling V-Sync.
        glfwSwapInterval(settings.isVSync() ? 1 : 0);
        
        // Enable depth testing.
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);
        GL11C.glDepthFunc(GL11C.GL_LEQUAL);
        
        // Finally show the window when finished.
        showWindow();
	}
	
	private void cleanup() {
		application.cleanup();
		destroyContext();
		
		// Reset the state of variables.
		timer = null;
		initialized.set(false);
	}
	
	/**
	 * Destroy the GLFW context.
	 */
	private void destroyContext() {
		try {
			if(window != NULL) {
				glfwDestroyWindow(window);
				window = NULL;
				glfwTerminate();
			}
		} catch (Exception ex) {
			logger.error("Failed to destroy context !", ex);
		}
	}
	
	/**
	 * Show the window.
	 */
	private void showWindow() {
		glfwShowWindow(window);
	}
	
	/**
	 * Return the <code>MercuryContext</code> window handle.
	 * 
	 * @return The window handle of the current context.
	 */
	public long getWindow() {
		return window;
	}
	
	/**
	 * Check that the currently used thread is the one used by the <code>OpenGL</code> context for rendering.
	 * 
	 * @throws GLException Thrown if the current thread isn't the rendering one.
	 */
	public static void checkGLThread() {
		if(!GL_THREAD_NAME.equals(Thread.currentThread().getName())) {
			throw new GLException("The method should only be called from '" + GL_THREAD_NAME + "'!");
		}
	}
	
	/**
	 * Set the settings used by the context to the provided ones. It copies the settings
	 * without altering the provided instance of <code>MercurySettings</code>.
	 * <p>
	 * Note that the settings won't be applied until you {@link #restart()}
	 * the <code>MercuryContext</code>.
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
	 * Set the title of the window's context.
	 * Note that it is only visible when windowed.
	 * 
	 * @param title The title of the window.
	 */
	public void setTitle(String title) {
		if(initialized.get() && window != NULL) {
			glfwSetWindowTitle(window, title);
		}
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
}
