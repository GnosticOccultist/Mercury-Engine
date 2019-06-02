package fr.mercury.nucleus.application;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.module.ApplicationModule;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.input.BaseInputProcessor;
import fr.mercury.nucleus.input.InputProcessor;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.renderer.Renderer;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.SpeedableNanoTimer;

/**
 * <code>MercuryApplication</code> is an abstract implementation of a usable {@link Application} using the <code>Mercury-Engine</code>.
 * It implements the initialization, updating and cleaning methods as well as managing the {@link MercurySettings} and a set of {@link ApplicationModule}
 * extending its basic capabilities depending on the user needs.
 * <p>
 * Such application is capable of rendering a 3D scene described by a hierarchy of {@link AnimaMundi} extending from the {@link #scene root-node}
 * using its own {@link Camera} and {@link Renderer}.
 * <p>
 * An {@link AssetManager} and an {@link InputProcessor} are contained within the application in order to handle asset loading and being 
 * notified about inputs related events.
 * 
 * @see #getScene()
 * @see #getModule(Class)
 * 
 * @author GnosticOccultist
 */
public abstract class MercuryApplication implements Application {

	/**
	 * The logger for the application.
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
	 * The set of modules linked to the application.
	 */
	protected final Set<ApplicationModule> modules = new HashSet<>();
	/**
	 * The asset manager.
	 */
	protected AssetManager assetManager = new AssetManager();
	/**
	 * The base input processor.
	 */
	protected BaseInputProcessor inputProcessor;
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
	 * Starts the <code>MercuryApplication</code> and creates the {@link MercuryContext}.
	 * While initializing the context, it will start the main application loop and show the configured window.
	 * <p>
	 * If no {@link MercurySettings} are set, it will use the default ones.
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
	 * <p>
	 * It is automatically called when the context is initialized.
	 */
	@Override
	@OpenGLCall
	public void internalInitialize() {
		
		// Initialize the camera.
		camera = new Camera(settings.getWidth(), settings.getHeight());
		camera.setLocation(0f, 0f, 8f);
		camera.setProjectionMatrix(45f, (float) camera.getWidth() / camera.getHeight(), 1f, 1000f);
		
		// Initialize renderer.
		renderer = new Renderer(camera);
		
		// Initialize input processor with context inputs.
		inputProcessor = new BaseInputProcessor(context.getMouseInput(), context.getKeyInput());
		
		// Reset the timer before invoking anything else,
		// to ensure the first time per frame isn't too large...
		timer.reset();
		
		// Initialize application's modules.
		modules.forEach(module -> module.initialize(this));
		
		// Initialize the implementation.
		initialize();
	}
	
	/**
	 * <b>Don't call manually</b>
	 * <p>
	 * It is automatically called internally in {@link #internalInitialize()}.
	 * <p>
	 * Initialize the implementation of <code>MercuryApplication</code>.
	 * 
	 * @see #internalInitialize()
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
	 * <p>
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
		
		inputProcessor.update();
		
		float tpf = timer.getTimePerFrame() * timer.getSpeed();
		
		// Initialize modules which haven't already.
		modules.stream().filter(module -> !module.isInitialized()).forEach(module -> module.initialize(this));
		// Update application's modules.
		modules.stream().filter(ApplicationModule::isEnabled).forEach(module -> module.update(tpf));
		
		// Update the implementation.
		update(tpf);
		
		// Update the geometric information of the scene and its hierarchy.
		scene.updateGeometricState();
		
		// Perform rendering of the scene.
		renderer.renderScene(scene);
	}
	
	/**
	 * <b>Don't call manually</b>
	 * <p>
	 * It is automatically called internally in {@link #internalUpdate()}.
	 * <p>
	 * Update the implementation of <code>MercuryApplication</code>.
	 * 
	 * @param tpf The time per frame.
	 * 
	 * @see #internalUpdate()
	 */
	@OpenGLCall
	protected void update(float tpf) {}

	/**
	 * <b>Don't call manually</b>
	 * <p>
	 * It is automatically called when closing the application, 
	 * before the context destruction.
	 */
	@Override
	@OpenGLCall
	public void cleanup() {
		
		modules.forEach(ApplicationModule::cleanup);
		
		inputProcessor.destroy();
		inputProcessor = null;
		
		timer.reset();
		renderer.cleanup(scene);
		
		logger.info("Closing the application: " + getClass().getSimpleName());
	}
	
	/**
     * Sets the context settings to the application ones, and
     * restart the <code>MercuryContext</code> in order to apply any changes.
     */
	public void restart() {
		context.setSettings(settings);
		context.restart();
	}
	
	/**
	 * Return an optional value of an {@link ApplicationModule} matching the provided type
	 * linked to the <code>MercuryApplication</code>.
	 * <p>
	 * This function is supposed to be used to access the module, however it shouldn't be used 
	 * to detach it from the application, use {@link #unlinkModule(ApplicationModule)} instead.
	 * 
	 * @param type The type of module to return.
	 * @return     An optional value containing either a module matching the given type, or 
	 * 			   nothing if none is linked to the application.
	 * 
	 * @see #getModule(Class)
	 */
	public <M extends ApplicationModule> Optional<M> getOptionalModule(Class<M> type) {
		return Optional.ofNullable(getModule(type));
	}
	
	/**
	 * Return an {@link ApplicationModule} matching the provided type linked to the 
	 * <code>MercuryApplication</code>.
	 * <p>
	 * This function is supposed to be used to access the module, however it shouldn't be used 
	 * to detach it from the application, use {@link #unlinkModule(ApplicationModule)} instead.
	 * 
	 * @param type The type of module to return (not null).
	 * @return	   A module matching the given type, or null if none is linked to 
	 * 			   the application.
	 * 
	 * @see #unlinkModule(ApplicationModule)
	 * @see #getOptionalModule(Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <M extends ApplicationModule> M getModule(Class<M> type) {
		Validator.nonNull(type, "The module's type can't be null!");
		for(ApplicationModule module : modules) {
			if(module.getClass().isAssignableFrom(type)) {
				return (M) module;
			}
		}
		return null;
	}
	
	/**
	 * Links the provided {@link ApplicationModule} to the <code>MercuryApplication</code>.
	 * Note that the module will be initialized during the next update cycle if it hasn't been yet.
	 * 
	 * @param module The module to be linked (not null).
	 */
	@Override
	public void linkModule(ApplicationModule module) {
		Validator.nonNull(module, "The module can't be null!");
		this.modules.add(module);
	}
	
	/**
	 * Unlinks the provided {@link ApplicationModule} from the <code>MercuryApplication</code>
	 * and terminate the module by calling the {@link ApplicationModule#cleanup()} method.
	 * 
	 * @param module The module which is to be cleaned up and removed (not null).
	 */
	public void unlinkModule(ApplicationModule module) {
		Validator.nonNull(module, "The module can't be null!");
		if(modules.remove(module)) {
			module.cleanup();
		}
	}
	
	/**
	 * Return the {@link NucleusMundi} representing the root-node of the scene meaning 
	 * all scenegraph elements are expanding down from this one.
	 * <p>
	 * In order to attach an {@link AnimaMundi} to this one, call the method {@link NucleusMundi#attach(AnimaMundi)}.
	 * 
	 * @return The root-node of the scene.
	 */
	public NucleusMundi getScene() {
		return scene;
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
