package fr.mercury.nucleus.renderer;

import org.lwjgl.opengl.GL11;

import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.renderer.logic.DefaultRenderLogic;
import fr.mercury.nucleus.renderer.logic.RenderLogic;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.renderer.queue.BucketType;
import fr.mercury.nucleus.renderer.queue.RenderBucket;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.scenegraph.visitor.VisitType;
import fr.mercury.nucleus.scenegraph.visitor.Visitor;
import fr.mercury.nucleus.utils.OpenGLCall;

public class Renderer extends AbstractRenderer {
	
	/**
	 * The visitor designed to render {@link PhysicaMundi} which doesn't use any {@link RenderBucket}.
	 */
	private final Visitor GL_CLEANUP = new Visitor() {
		
		@Override
		public void visit(AnimaMundi anima) {
			if(anima instanceof PhysicaMundi) {
				var physica = (PhysicaMundi) anima;
				physica.getMesh().cleanup();
			}
		}
	};
	
	/**
	 * The visitor designed to render {@link PhysicaMundi} which doesn't use any {@link RenderBucket}.
	 */
	private final Visitor RENDER_NONE_BUCKET = new Visitor() {
		
		@Override
		public void visit(AnimaMundi anima) {
			if(anima instanceof PhysicaMundi && anima.getBucket().equals(BucketType.NONE)) {
				render((PhysicaMundi) anima);
			}
		}
	};
	
	/**
	 * The visitor designed to fill the buckets with appropriate {@link PhysicaMundi}.
	 */
	private final Visitor BUCKETS_FILLER = new Visitor() {
		
		@Override
		public void visit(AnimaMundi anima) {
			if(anima instanceof PhysicaMundi) {
				submitToBucket(anima);
			}
		}
	};
	
	private final RenderLogic defaultLogic;
	
	public Renderer(Camera camera, AssetManager assetManager) {
		super(camera);
		
		this.defaultLogic = new DefaultRenderLogic();
		
		// TEST:
		
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
		
		// Visit the scene and render objects which doesn't use the bucket logic.
		scene.visit(RENDER_NONE_BUCKET, VisitType.POST_ORDER);
		
		// Visit the scene and fill the buckets with renderables.
		scene.visit(BUCKETS_FILLER, VisitType.POST_ORDER);
		
		// Render buckets...
		renderBucket(BucketType.OPAQUE);
		
		// Flushes all the buckets, even if some rendering wasn't performed.
		flushBuckets();
	}
	
	@Override
	@OpenGLCall
	public void render(PhysicaMundi physica) {
		
		setMatrix(MatrixType.MODEL, physica.getWorldTransform());
		
		computeMatrix(MatrixType.VIEW_PROJECTION_MODEL);
		
		// this is temporary hopefully...
		var material = physica.getMaterial();
		if(material != null && program == null) {
			program = new ShaderProgram();
			material.getSources().forEach(program::attachSource);
			program.addUniform("texture_sampler", UniformType.TEXTURE2D, 0);
			program.upload();
		}
		
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
	public void cleanup(AnimaMundi anima) {
		program.cleanup();
		anima.visit(GL_CLEANUP, VisitType.POST_ORDER);
	}
}
