package fr.mercury.nucleus.renderer;

import org.lwjgl.opengl.GL11;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.renderer.logic.DefaultRenderLogic;
import fr.mercury.nucleus.renderer.logic.RenderLogic;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.scenegraph.visitor.VisitType;
import fr.mercury.nucleus.utils.OpenGLCall;

public class Renderer extends AbstractRenderer {
	
	private final Camera camera;
	
	private final RenderLogic defaultLogic;
	
	public Renderer(Camera camera, AssetManager assetManager) {
		Validator.nonNull(camera);
		
		this.camera = camera;
		this.defaultLogic = new DefaultRenderLogic();
		
		// TEST:
		program = new ShaderProgram()
				.attachSource(assetManager.loadShaderSource("/shaders/default.vert"))
				.attachSource(assetManager.loadShaderSource("/shaders/default.frag"))
				.addUniform("color", UniformType.VECTOR4F, new Color(0.1f, 0.3f, 0.1f, 1f))
				.addUniform("texture_sampler", UniformType.TEXTURE2D, 0);
				
		program.upload();	
	}
	
	/**
	 * Clears the color and depth buffer. The function should be called before every rendering process
	 * to clean these buffers before writing.
	 */
	@OpenGLCall
	public void clearBuffers() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
	}
	
	@OpenGLCall
	public void renderScene(NucleusMundi scene) {
		// Clears the buffer before writing to it.
		clearBuffers();
	
		camera.updateViewMatrix();
		
		setMatrix(MatrixType.VIEW, camera.getViewMatrix());
		setMatrix(MatrixType.PROJECTION, camera.getProjectionMatrix());
		
		scene.visit(anima -> {
			if(anima instanceof PhysicaMundi) {
				render((PhysicaMundi) anima);
			}
		}, VisitType.POST_ORDER);
	}
	
	@OpenGLCall
	private void render(PhysicaMundi physica) {
		
		setMatrix(MatrixType.MODEL, physica.getWorldTransform().transform());
		
		computeMatrix(MatrixType.VIEW_PROJECTION_MODEL);
		
		setupUniforms();
		
		defaultLogic.begin(physica);
	
		defaultLogic.render(physica);
		
		defaultLogic.end(physica);
	}

	/**
	 * Resize the camera and the viewport dimensions to the provided ones.
	 * 
	 * @param width	 The new width of the window.
	 * @param height The new height of the window.
	 */
	@OpenGLCall
	public void resize(int width, int height) {
		if(camera != null) {
			camera.resize(width, height);
			GL11.glViewport(0, 0, camera.getWidth(), camera.getHeight());
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			GL11.glScissor(0, 0, width, height);
		}
	}

	@OpenGLCall
	public void setDepthRange(double depthRangeNear, double depthRangeFar) {
		GL11.glDepthRange(depthRangeNear, depthRangeFar);
	}
	
	/**
	 * Cleanup the renderer and its components.
	 */
	@OpenGLCall
	public void cleanup() {
		program.cleanup();
	}
}
