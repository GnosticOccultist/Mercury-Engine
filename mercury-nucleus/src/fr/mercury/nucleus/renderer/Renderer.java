package fr.mercury.nucleus.renderer;

import org.lwjgl.opengl.GL11C;

import fr.mercury.nucleus.renderer.logic.DefaultRenderLogic;
import fr.mercury.nucleus.renderer.logic.RenderLogic;
import fr.mercury.nucleus.renderer.logic.state.RenderState;
import fr.mercury.nucleus.renderer.queue.BucketType;
import fr.mercury.nucleus.renderer.queue.RenderBucket;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.scenegraph.visitor.VisitType;
import fr.mercury.nucleus.scenegraph.visitor.Visitor;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.OpenGLCall;

public class Renderer extends AbstractRenderer {
	
	/**
	 * The visitor designed to cleanup a hierarchy of {@link AnimaMundi}.
	 */
	private final Visitor GL_CLEANUP = new Visitor() {
		
		@Override
		public void visit(AnimaMundi anima) {
			if(anima instanceof PhysicaMundi) {
				var physica = (PhysicaMundi) anima;
				physica.getMesh().cleanup();
				physica.getMaterial().cleanup();
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
	
	/**
	 * The render logic used by the renderer.
	 */
	private final RenderLogic defaultLogic;
	
	public Renderer(Camera camera) {
		super(camera);
		
		this.defaultLogic = new DefaultRenderLogic();
	}
	
	/**
	 * Clears the color and depth buffer. The function should be called before every rendering process
	 * to clean these buffers before wQWWwriting.
	 */
	@OpenGLCall
	public void clearBuffers() {
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT | GL11C.GL_STENCIL_BUFFER_BIT);
	}
	
	@OpenGLCall
	public void renderScene(NucleusMundi scene) {
		// Check a camera is registered.
		if(camera == null) {
			throw new MercuryException("Unable to render scene without a camera!");
		}
		
		// Clears the buffer before writing to it.
		clearBuffers();

		// Prepares the camera before rendering the scene.
		camera.prepare(this);
		
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
		
		var shader = physica.getMaterial().getFirstShader();
		
		setupMatrixUniforms(shader, physica);
		
		setupPrefabUniforms(shader, physica);
		
		physica.getMaterial().setupUniforms(shader);
		
		// Upload latest changes to the OpenGL state.
		shader.upload();
		
		for(var type : RenderState.Type.values()) {
			var state = physica.getLocalRenderState(type);
			if(state != null) {
				applyRenderState(state);
			}
		}
		
		defaultLogic.begin(physica);
	
		defaultLogic.render(physica);
		
		defaultLogic.end(physica);
	}

	/**
	 * Resize the {@link Camera} viewport dimensions to the provided width and height, and update 
	 * the <code>OpenGL</code> scissor test to discard any fragment outside the dimension of the rectangle.
	 * 
	 * @param width	 The new width of the window.
	 * @param height The new height of the window.
	 */
	@OpenGLCall
	public void resize(int width, int height) {
		if(camera != null && camera.resize(width, height)) {
			GL11C.glViewport(0, 0, camera.getWidth(), camera.getHeight());
			GL11C.glEnable(GL11C.GL_SCISSOR_TEST);
			GL11C.glScissor(0, 0, width, height);
		}
	}
	
	/**
	 * Cleanup the renderer and its components.
	 */
	@OpenGLCall
	public void cleanup(AnimaMundi anima) {
		anima.visit(GL_CLEANUP, VisitType.POST_ORDER);
	}
}
