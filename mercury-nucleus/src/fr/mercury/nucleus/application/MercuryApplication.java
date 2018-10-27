package fr.mercury.nucleus.application;

import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.renderer.Renderer;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.scene.PhysicaMundi;
import fr.mercury.nucleus.texture.Texture2D;
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
	
	private ShaderProgram program;
	private Matrix4f projectionModelMatrix;
	private PhysicaMundi cube;
	
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
		
		System.out.println("Starting the application: " + getClass().getSimpleName());
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
		
		renderer = new Renderer(camera);
		
		// Reset the timer before invoking anything else,
		// to ensure the first time per frame isn't too large...
		timer.reset();
		
		Texture2D texture = assetManager.loadTexture2D("/model/octostone.png");
		//texture.fromColor(new Color(0, 0, 1), 256, 256);
		texture.upload();
		
		cube = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube.getTransform().setRotation(0.3f, 0, 0.3f).setScale(1f, 1f, 1f);
		camera.lookAt(cube.getTransform().getTranslation(), Vector3f.UNIT_Y);
		
		projectionModelMatrix = MercuryMath.LOCAL_VARS.acquireNext(Matrix4f.class);
		projectionModelMatrix.identity();
		projectionModelMatrix.set(camera.getViewProjectionMatrix().mult(cube.getTransform().transform(), projectionModelMatrix));
		
		cube.getMesh().texture = texture;
		
		// TEST:
		program = new ShaderProgram()
				.attachSource(assetManager.loadShaderSource("/shaders/default.vert"))
				.attachSource(assetManager.loadShaderSource("/shaders/default.frag"))
				.addUniform("projectionMatrix", UniformType.MATRIX4F, projectionModelMatrix)
				.addUniform("color", UniformType.VECTOR4F, new Color(0.1f, 0.3f, 0.1f, 1f))
				.addUniform("texture_sampler", UniformType.TEXTURE2D, 0);
		
		program.upload();
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

		if(settings.getBoolean("ShowFPS")) {
			context.setTitle(settings.getTitle() + " - " + (int) (timer.getFrameRate()) + " FPS");
		}	
	
		projectionModelMatrix.identity();
		projectionModelMatrix.set(camera.getViewProjectionMatrix().mult(cube.getTransform().transform(), projectionModelMatrix));
		program.getUniform("projectionMatrix").setValue(UniformType.MATRIX4F, projectionModelMatrix);
		program.getUniform("projectionMatrix").upload(program);
		
		renderer.render(cube.getMesh());
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
		program.cleanup();
		cube.getMesh().cleanup();
		
		System.out.println("Closing the application: " + getClass().getSimpleName());
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
