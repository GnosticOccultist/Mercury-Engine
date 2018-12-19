package fr.mercury.nucleus.application;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.alchemy.utilities.logging.LoggerLevel;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.renderer.Renderer;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.Texture2D;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
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
public class MercuryApplication implements Application {

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
	protected NucleusMundi scene;
	
	
	private PhysicaMundi cube;
	
	private NucleusMundi nucleus;
	
	// TODO: Remove this, only used when concrete application are created.
	public static void main(String[] args) {
		MercuryApplication app = new MercuryApplication();
		app.start();
	}
	
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
	public void initialize() {
		
		// Initialize the camera.
		camera = new Camera(settings.getWidth(), settings.getHeight());
		camera.getLocation().set(0f, 0f, 8f);
		camera.setProjectionMatrix(45f, (float) camera.getWidth() / camera.getHeight(), 1f, 1000f);
		
		renderer = new Renderer(camera, assetManager);
		
		// Reset the timer before invoking anything else,
		// to ensure the first time per frame isn't too large...
		timer.reset();
		
		scene = new NucleusMundi("root-nucleus");
		
		Texture2D texture = assetManager.loadTexture2D("/model/octostone.png")
				.setFilter(MinFilter.TRILINEAR, MagFilter.BILINEAR)
				.setWrapMode(WrapMode.REPEAT, WrapMode.REPEAT);
		texture.upload();
		
		cube = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube.setName("cube-1");
		cube.getLocalTransform().setRotation(0.3f, 0, 0.3f).setScale(1f, 1f, 1f);
		
		PhysicaMundi cube1 = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube1.setName("cube-2");
		cube1.getLocalTransform().setTranslation(2.5f, 0, 0).setRotation(0.3f, 0, 0.3f).setScale(1f, 1f, 1f);
		
		camera.lookAt(cube.getLocalTransform().getTranslation(), Vector3f.UNIT_Y);
		
		cube.getMesh().texture = texture;
		
		Texture2D texture2 = new Texture2D().color(new Color(0, 0, 1), 2048, 2048)
				.setFilter(MinFilter.BILINEAR, MagFilter.BILINEAR)
				.setWrapMode(WrapMode.REPEAT, WrapMode.REPEAT);
		texture2.upload();
		
		cube1.getMesh().texture = texture2;
		
		FactoryLogger.getLogger("mercury.scenegraph").setActive(LoggerLevel.DEBUG, true);
		
		nucleus = new NucleusMundi("sub-nucleus");
		
		nucleus.attach(cube);
		nucleus.attach(cube1);
		scene.attach(nucleus);
	}
	
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
	public void update() {
		
		if(timer.isPaused()) {
			return;
		}
		
		timer.update();
		
		scene.rotate(.007f, 0.007f, 0);
		cube.rotate(0.0f, 0.03f, 0);
		
		scene.updateGeometricState();

		if(settings.getBoolean("ShowFPS")) {
			context.setTitle(settings.getTitle() + " - " + (int) (timer.getFrameRate()) + " FPS");
		}	
		
		renderer.renderScene(scene);
	}

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
		cube.getMesh().cleanup();
		
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
