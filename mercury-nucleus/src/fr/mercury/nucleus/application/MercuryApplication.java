package fr.mercury.nucleus.application;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.renderer.Renderer;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.SpeedableNanoTimer;

/**
 * <code>MercuryApplication</code> is a basic implementation of a usable application
 * using the <code>Mercury-Engine</code>.
 * <p>
 * It implements the initialization, updating and cleaning methods as well as 
 * the <code>MercurySettings</code> handling.
 * 
 * @author GnosticOccultist
 */
public abstract class MercuryApplication implements Application {

	/**
	 * The logger for the Mercury Application.
	 */
	protected static final Logger logger = FactoryLogger.getLogger("mercury.app");
	
	/**
	 * The current context of the application.
	 */
	protected MercuryContext context;
	/**
	 * The settings of the application.
	 */
	protected MercurySettings settings;
	/**
	 * The asset manager.
	 */
	protected AssetManager assetManager = new AssetManager();
	/**
	 * The timer of the application in nanoseconds.
	 */
	protected SpeedableNanoTimer timer = new SpeedableNanoTimer();
	/**
	 * The camera used for rendering.
	 */
	protected Camera camera;
	/**
	 * The renderer.
	 */
	protected Renderer renderer;
	/**
	 * The root node for the scene.
	 */
	protected NucleusMundi scene = new NucleusMundi("root-nucleus");
	
	/**
	 * Starts the <code>MercuryApplication</code> and creates the <code>MercuryContext</code>.
	 * While initializing the context, it will start the main application loop and show the configured window.
	 * <p>
	 * If no <code>MercurySettings</code> are set, it will use the default ones.
	 */
	public void start() {
		if(settings == null) {
			settings = new MercurySettings(true);
		}
		
		logger.info("Starting the application: " + getClass().getSimpleName());
		context = MercuryContext.newContext(this, settings);
		context.initialize();
	}
	
	/**
	 * <b>Don't call manually</b>
	 * 
	 * It is automatically called when the context is initialized.
	 */
	@Override
	@OpenGLCall
	public void internalInitialize() {
		
		// Initialize the camera.
		camera = new Camera(settings.getWidth(), settings.getHeight());
		camera.getLocation().set(0f, 0f, 8f);
		camera.setProjectionMatrix(45f, (float) camera.getWidth() / camera.getHeight(), 1f, 1000f);
		
		// Initialize renderer.
		renderer = new Renderer(camera, assetManager);
		
		// Reset the timer before invoking anything else,
		// to ensure the first time per frame isn't too large...
		timer.reset();
		
		// Initialize the implementation.
		initialize();
	}
	
	/**
	 * Initialize the implementation of <code>MercuryApplication</code>.
	 * This is automatically called internally in {@link #internalInitialize()}.
	 */
	@OpenGLCall
	protected abstract void initialize();
	
	@Override
	@OpenGLCall
	public void resize(int width, int height) {
		if(renderer != null) {
			renderer.resize(width, height);
		}
	}

	/**
	 * <b>Don't call manually</b>
	 * 
	 * It is automatically called during the context updating logic.
	 */
	@Override
	@OpenGLCall
	public void internalUpdate() {
		// Timer is paused, no need to continue...
		if(timer.isPaused()) {
			return;
		}
		
		timer.update();
		
		if(settings.getBoolean("ShowFPS")) {
			context.setTitle(settings.getTitle() + " - " + (int) (timer.getFrameRate()) + " FPS");
		}
		
		float tpf = timer.getTimePerFrame() * timer.getSpeed();
		
		// Update the implementation.
		update(tpf);
		
		// Update the geometric information of the scene and its hierarchy.
		scene.updateGeometricState();
		
		// Perform rendering of the scene.
		renderer.renderScene(scene);
	}
	
	/**
	 * Update the implementation of <code>MercuryApplication</code>.
	 * This is automatically called internally in {@link #internalUpdate()}.
	 * 
	 * @param tpf The time per frame.
	 */
	@OpenGLCall
	protected void update(float tpf) {}

	/**
	 * <b>Don't call manually</b>
	 * 
	 * It is automatically called when closing the application, 
	 * before the context destruction.
	 */
	@Override
	@OpenGLCall
	public void cleanup() {
		timer.reset();
		renderer.cleanup();
		
		logger.info("Closing the application: " + getClass().getSimpleName());
	}
	
	/**
     * Set the context settings to the application ones, and
     * restart the <code>MercuryContext</code> in order to apply any changes.
     */
	public void restart() {
		context.setSettings(settings);
		context.restart();
	}
	
	/**
	 * Set the <code>MercurySettings</code> for the <code>Application</code>.
	 * <p>
	 * You can change the display settings when the application is running but
	 * in order to apply the changes you will need to call {@link #restart()}.
	 * 
	 * @param settings The new settings to apply.
	 */
	public void setSettings(MercurySettings settings) {
		this.settings = settings;
	}
}
