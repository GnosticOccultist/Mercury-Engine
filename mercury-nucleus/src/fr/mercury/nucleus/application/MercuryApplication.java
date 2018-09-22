package fr.mercury.nucleus.application;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.renderer.Renderer;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.Usage;
import fr.mercury.nucleus.renderer.opengl.mesh.VertexArray;
import fr.mercury.nucleus.renderer.opengl.mesh.VertexBuffer;
import fr.mercury.nucleus.renderer.opengl.mesh.VertexBufferType;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
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
	
	public static void main(String[] args) {
		MercuryApplication app = new MercuryApplication();
		app.start();
	}
	
	public void start() {
		if(settings == null) {
			settings = new MercurySettings(true);
		}
		
		System.out.println("Starting the application: " + getClass().getSimpleName());
		context = MercuryContext.newContext(this, settings);
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
		camera.setProjectionMatrix(45f, (float) camera.getWidth() / camera.getHeight(), 1f, 1000f);
		
		renderer = new Renderer(camera);
		
		// Reset the timer before invoking anything else,
		// to ensure the first time per frame isn't too large...
		timer.reset();
		
		VertexArray vertexArray = new VertexArray();
		vertexArray.upload();
		
		// TEST:
		ShaderProgram program = new ShaderProgram()
				.attachSource(assetManager.loadShaderSource("/shaders/default.vert"))
				.attachSource(assetManager.loadShaderSource("/shaders/default.frag"));
		
		program.upload();
		
		int loc = GL20.glGetUniformLocation(program.getID(), "projectionMatrix");
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer fb = stack.mallocFloat(16);
			camera.projectionMatrix.fillFloatBuffer(fb, true);
			fb.clear();
			GL20.glUniformMatrix4fv(loc, false, fb);
		}
		
		VertexBuffer vertexBuffer = new VertexBuffer(VertexBufferType.POSITION, Usage.STATIC_DRAW);
		vertexBuffer.storeData(new float[]{
		        -0.5f,  0.5f, 0.0f,
		        -0.5f, -0.5f, 0.0f,
		         0.5f,  0.5f, 0.0f,
		         0.5f,  0.5f, 0.0f,
		        -0.5f, -0.5f, 0.0f,
		         0.5f, -0.5f, 0.0f,
		});

		vertexBuffer.upload();
		
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
	
		GL20.glEnableVertexAttribArray(0);
	}

	/**
	 * <b>Don't call manually</b>
	 * 
	 * It is automatically called during the context updating logic.
	 */
	@Override
	@OpenGLCall
	public void update() {
		timer.update();
		
		renderer.update();
		renderer.drawTriangles(6);
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
