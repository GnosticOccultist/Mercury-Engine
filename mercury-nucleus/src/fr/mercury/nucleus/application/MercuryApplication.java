package fr.mercury.nucleus.application;

import org.lwjgl.opengl.GL11;

import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.renderer.Renderer;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.Usage;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.scene.PhysicaMundi;
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
		camera.getLocation().set(0f, 0f, 10f);
		camera.setProjectionMatrix(45f, (float) camera.getWidth() / camera.getHeight(), 1f, 1000f);
		
		renderer = new Renderer(camera);
		
		// Reset the timer before invoking anything else,
		// to ensure the first time per frame isn't too large...
		timer.reset();
		
		cube = new PhysicaMundi();
		cube.getTransform().setRotation(0.3f, 0, 0.3f).setScale(3f, 3f, 3f);
		camera.lookAt(cube.getTransform().getTranslation(), Vector3f.UNIT_Y);
		
		projectionModelMatrix = MercuryMath.LOCAL_VARS.acquireNext(Matrix4f.class);
		projectionModelMatrix.set(camera.getViewProjectionMatrix());
		projectionModelMatrix.mult(cube.getTransform().transform(), projectionModelMatrix);
		
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		
		// TEST:
		program = new ShaderProgram()
				.attachSource(assetManager.loadShaderSource("/shaders/default.vert"))
				.attachSource(assetManager.loadShaderSource("/shaders/default.frag"))
				.addUniform("projectionMatrix", UniformType.MATRIX4F, projectionModelMatrix)
				.addUniform("color", UniformType.VECTOR4F, new Color(0, 0.8f, 0, 1f));
		
		program.upload();
		
		cube.getMesh().setupBuffer(VertexBufferType.POSITION, Usage.STATIC_DRAW, new float[] {
	            // V0
	            -0.5f, 0.5f, 0.5f,
	            // V1
	            -0.5f, -0.5f, 0.5f,
	            // V2
	            0.5f, -0.5f, 0.5f,
	            // V3
	            0.5f, 0.5f, 0.5f,
	            // V4
	            -0.5f, 0.5f, -0.5f,
	            // V5
	            0.5f, 0.5f, -0.5f,
	            // V6
	            -0.5f, -0.5f, -0.5f,
	            // V7
	            0.5f, -0.5f, -0.5f,
	            // For text coords in top face
	            // V8: V4 repeated
	            -0.5f, 0.5f, -0.5f,
	            // V9: V5 repeated
	            0.5f, 0.5f, -0.5f,
	            // V10: V0 repeated
	            -0.5f, 0.5f, 0.5f,
	            // V11: V3 repeated
	            0.5f, 0.5f, 0.5f,
	            // For text coords in right face
	            // V12: V3 repeated
	            0.5f, 0.5f, 0.5f,
	            // V13: V2 repeated
	            0.5f, -0.5f, 0.5f,
	            // For text coords in left face
	            // V14: V0 repeated
	            -0.5f, 0.5f, 0.5f,
	            // V15: V1 repeated
	            -0.5f, -0.5f, 0.5f,
	            // For text coords in bottom face
	            // V16: V6 repeated
	            -0.5f, -0.5f, -0.5f,
	            // V17: V7 repeated
	            0.5f, -0.5f, -0.5f,
	            // V18: V1 repeated
	            -0.5f, -0.5f, 0.5f,
	            // V19: V2 repeated
	            0.5f, -0.5f, 0.5f,
		});
		cube.getMesh().setupBuffer(VertexBufferType.INDEX, Usage.STATIC_DRAW, new int[] {

			// Front face
			0, 1, 3, 3, 1, 2,
			// Top Face
			8, 10, 11, 9, 8, 11,
			// Right face
			12, 13, 7, 5, 12, 7,
			// Left face
			14, 15, 6, 4, 14, 6,
			// Bottom face
			16, 18, 19, 17, 16, 19,
			// Back face
			4, 6, 7, 5, 4, 7,
	    });
		
		cube.getMesh().upload();
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
		
		// TODO: Manage this automatically, somehow...
		projectionModelMatrix.identity();
		projectionModelMatrix.set(camera.getViewProjectionMatrix());
		projectionModelMatrix.mult(cube.getTransform().transform(), projectionModelMatrix);
		program.getUniform("projectionMatrix").setValue(UniformType.MATRIX4F, projectionModelMatrix);
		program.getUniform("projectionMatrix").upload(program);
		program.getUniform("color").upload(program);
		
		renderer.update(cube.getMesh());
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
