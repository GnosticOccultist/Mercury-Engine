package fr.mercury.nucleus.renderer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.readable.ReadableTransform;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.renderer.logic.state.RenderState;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Face;
import fr.mercury.nucleus.renderer.logic.state.PolygonModeState.PolygonMode;
import fr.mercury.nucleus.renderer.logic.state.PolygonModeState;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.UniformStructure;
import fr.mercury.nucleus.renderer.queue.BucketType;
import fr.mercury.nucleus.renderer.queue.RenderBucket;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.OpenGLCall;

public abstract class AbstractRenderer {
	
	/**
	 * The logger for the Mercury Renderer.
	 */
	protected static final Logger logger = FactoryLogger.getLogger("mercury.renderer");
	
	/**
	 * The table containing the various render buckets organized by their types.
	 */
	protected final Map<BucketType, RenderBucket> buckets = new HashMap<BucketType, RenderBucket>();
	/**
	 * The table containing the various matrices used for rendering in the shader 
	 * as a float buffer.
	 */
	protected final EnumMap<MatrixType, Matrix4f> matrixMap = new EnumMap<>(MatrixType.class);
	/**
	 * The clear values for the color buffer used by the renderer.
	 */
	protected final Color clearColor = new Color(0, 0, 0, 0);
	/**
	 * The camera used by the renderer.
	 */
	protected Camera camera;
	
	/**
	 * Instantiates a new <code>AbstractRenderer</code> with the provided {@link Camera}.
	 * 
	 * @param camera The camera to use for rendering (not null).
	 */
	protected AbstractRenderer(Camera camera) {
		Validator.nonNull(camera, "The camera can't be null!");
		
		this.camera = camera;
	}
	
	/**
	 * Register a new {@link RenderBucket} with the specified {@link BucketType} for
	 * the <code>AbstractRenderer</code>.
	 * 
	 * @param type The bucket type to register (not null).
	 */
	public void registerBucket(BucketType type) {
		Validator.nonNull(type, "The bucket type to register can't be null!");
		if(type.equals(BucketType.LEGACY) || type.equals(BucketType.NONE)) {
			throw new MercuryException("The bucket '" + type + "' cannot be registered!");
		}
		
		buckets.put(type, new RenderBucket(camera));
	}
	
	/**
	 * Submit the specified {@link AnimaMundi} to a {@link RenderBucket} matching 
	 * the {@link BucketType}. 
	 * <p>
	 * If the type of bucket isn't registered for this <code>AbstractRenderer</code>,
	 * it will not add the anima to be rendered, call {@link #registerBucket(BucketType)}
	 * to register the needed bucket's type.
	 * 
	 * @param anima The anima-mundi to add to a bucket.
	 * @return		Whether the anima-mundi has been added to a bucket.
	 */
	protected boolean submitToBucket(AnimaMundi anima) {
		if(anima.getBucket().equals(BucketType.NONE) || !camera.checkLayer(anima.getRenderLayer())) {
			return false;
		}
		
		var type = anima.getBucket();
		
		var bucket = buckets.get(type);
		if(bucket != null) {
			bucket.add(anima);
			return true;
		}
		
		logger.warning("The anima '" + anima + "' couldn't be submitted to a bucket of type " + type + "!");
		return false;
	}
	
	/**
	 * Render the {@link RenderBucket} corresponding to the specified {@link BucketType}.
	 * It will first call {@link RenderBucket#sort()} and then {@link RenderBucket#render(AbstractRenderer)}.
	 * 
	 * @param type The type of bucket to render.
	 * 
	 * @throws MercuryException 	 Thrown if the type is either {@link BucketType#LEGACY} or {@link BucketType#NONE}.
	 * @throws IllegalStateException Thrown if there is no registered bucket of the specified type in the renderer.
	 */
	protected void renderBucket(BucketType type) {
		if(type.equals(BucketType.LEGACY) || type.equals(BucketType.NONE)) {
			throw new MercuryException("The bucket '" + type + "' cannot be rendered!");
		}
		
		var bucket = buckets.get(type);
		if(bucket == null) {
			throw new IllegalStateException("No bucket for type: " + type + " is defined in the renderer!");
		}
		
		bucket.sort();
		bucket.render(this);
	}
	
	/**
	 * Flushes all registered {@link RenderBucket} in the <code>AbstractRenderer</code>, by
	 * emptying the bucket of its {@link AnimaMundi} and reseting its size to 0.
	 * 
	 * @see RenderBucket#flush()
	 */
	protected void flushBuckets() {
		buckets.values().forEach(RenderBucket::flush);
	}
	
	/**
	 * Render the provided {@link AnimaMundi}.
	 * Override this method in your implementation of <code>AbstractRenderer</code>.
	 * 
	 * @param anima The anima to render.
	 */
	public abstract void render(PhysicaMundi anima);
	
	/**
	 * Sets the clear values of the <code>AbstractRenderer</code> for the color-buffer.
	 * 
	 * @param color The color to be cleared from the buffer (not null).
	 */
	@OpenGLCall
	protected void setClearColor(Color color) {
		Validator.nonNull(color, "The clear color can't be null!");
		if(!clearColor.equals(color)) {
			clearColor.set(color);
			GL11C.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		}
	}
	
	/**
	 * Sets the depth range for the viewport to the provided near and far values.
	 * 
	 * @param nearDepthRange The near depth range (&ge; 0 &le;1, default &rarr; 0).
	 * @param farDepthRange  The far depth range (&ge; 0 &le;1, default &rarr; 1).
	 */
	@OpenGLCall
	protected void setDepthRange(double nearDepthRange, double farDepthRange) {
		GL11C.glDepthRange(nearDepthRange, farDepthRange);
	}
	
	/**
	 * Apply the provided <code>RenderState</code> to the <code>OpenGL</code> context.
	 * 
	 * @param state The render state to apply (not null).
	 */
	@OpenGLCall
	protected void applyRenderState(RenderState state) {
		Validator.nonNull(state, "The render state can't be null!");
		
		switch (state.type()) {
			case FACE_CULLING:
				var cull = (FaceCullingState) state;
				if(cull.isEnabled()) {
					GL11.glEnable(GL11.GL_CULL_FACE);
					switch (cull.face()) {
						case BACK:
							GL11.glCullFace(GL11.GL_BACK);
							break;
						case FRONT:
							GL11.glCullFace(GL11.GL_FRONT);
							break;
						case FRONT_AND_BACK:
							GL11.glCullFace(GL11.GL_FRONT_AND_BACK);
							break;
						default:
							break;
					}
				} else {
					GL11.glDisable(GL11.GL_CULL_FACE);
				}
				switch (cull.windingOrder()) {
					case CLOCKWISE:
						GL11.glFrontFace(GL11.GL_CW);
						break;
					case COUNTER_CLOCKWISE:
						GL11.glFrontFace(GL11.GL_CCW);
						break;
					default:
						break;
				}
				break;
			case POLYGON_MODE:
				var wireframe = (PolygonModeState) state;
				if(wireframe.isEnabled()) {
					PolygonMode fMode = wireframe.polygonMode(Face.FRONT);
					PolygonMode bMode = wireframe.polygonMode(Face.BACK);
					if(fMode == bMode) {
						switch (bMode) {
							case FILL:
								GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_FILL);
								break;
							case LINE:
								GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_LINE);
								break;
							case POINT:
								GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_POINT);
								break;
						}
					} else if(fMode != bMode) {
						switch (fMode) {
							case FILL:
								GL11C.glPolygonMode(GL11C.GL_FRONT, GL11C.GL_FILL);
								break;
							case LINE:
								GL11C.glPolygonMode(GL11C.GL_FRONT, GL11C.GL_LINE);
								break;
							case POINT:
								GL11C.glPolygonMode(GL11C.GL_FRONT, GL11C.GL_POINT);
								break;
						}
						switch (bMode) {
							case FILL:
								GL11C.glPolygonMode(GL11C.GL_BACK, GL11C.GL_FILL);
								break;
							case LINE:
								GL11C.glPolygonMode(GL11C.GL_BACK, GL11C.GL_LINE);
								break;
							case POINT:
								GL11C.glPolygonMode(GL11C.GL_BACK, GL11C.GL_POINT);
								break;
						}
					}
				} else {
					GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_FILL);
				}
				break;
			default:
				break;
		}
	}
	
	/**
	 * Setup the {@link Uniform} corresponding to the needed {@link MatrixType} specified by the provided
	 * {@link Material} and applied for the given {@link ShaderProgram}.
	 * 
	 * @param shader  The shader program to which the matrix uniforms need to be passed.
	 * @param physica The physica-mundi requesting the matrix uniforms.
	 */
	protected void setupMatrixUniforms(ShaderProgram shader, PhysicaMundi physica) {
		var matrixUniforms = physica.getMaterial().getPrefabUniforms();
		
		for(MatrixType type : MatrixType.values()) {
			var name = type.name();
			
			if(matrixUniforms.contains(name)) {
				// First force-compute the matrix before adding it to the uniform.
				if(type.canCompute()) {
					computeMatrix(type);
				}
				
				shader.addUniform(type.getUniformName(), UniformType.MATRIX4F, matrixMap.get(type));	
			}
		}
	}
	
	/**
	 * Setup the {@link UniformStructure} specified by the provided {@link Material} and apply it 
	 * for the given {@link ShaderProgram}.
	 * 
	 * @param shader  The shader program to setup the structure of uniforms for.
	 * @param physica The physica-mundi requesting the prefab uniforms.
	 */
	protected void setupPrefabUniforms(ShaderProgram shader, PhysicaMundi physica) {
		var prefabUniforms = physica.getMaterial().getPrefabUniforms();
		
		for(int i = 0; i < prefabUniforms.size(); i++) {
			
			var prefabName = prefabUniforms.get(i);
			var property = physica.getEnvironmentElement(prefabName);
			
			if(property != null) {
				property.uniforms(shader);
			}
		}
	}
	
	/**
	 * Stores the provided {@link Matrix4f} for the given usage {@link MatrixType}
	 * 
	 * @param type   The type of the rendering matrix.
	 * @param matrix The rendering matrix to store.
	 */
	public void setMatrix(MatrixType type, Matrix4f matrix) {
		var buffer = matrixMap.computeIfAbsent(type, k -> new Matrix4f());
		buffer.set(matrix);
	}
	
	/**
	 * Stores the provided {@link Matrix4f} for the given usage {@link MatrixType}
	 * 
	 * @param type   The type of the rendering matrix.
	 * @param matrix The rendering matrix to store.
	 */
	public void setMatrix(MatrixType type, ReadableTransform transform) {
		var buffer = matrixMap.computeIfAbsent(type, k -> new Matrix4f());
		transform.asModelMatrix(buffer);
	}
	
	/**
	 * Compute the specified {@link MatrixType}, if possible, with the other matrices if they are provided.
	 * The type of matrix that can be computed are marked as {@link MatrixType#canCompute()}.
	 * 
	 * @param type The type of matrix to compute.
	 */
	public void computeMatrix(MatrixType type) {
		if(!type.canCompute()) {
			throw new IllegalArgumentException("The provided type of matrix: " + 
					type + " can't be computed!");
		}
		
		var buffer = matrixMap.computeIfAbsent(type, k -> new Matrix4f());
		
		switch (type) {
			case VIEW_PROJECTION_MODEL:
				var viewProj = matrixMap.get(MatrixType.VIEW_PROJECTION);
				
				// First compute the view projection if not already present.
				if(viewProj == null) {
					computeMatrix(MatrixType.VIEW_PROJECTION);
					viewProj = matrixMap.get(MatrixType.VIEW_PROJECTION);
				}
				
				buffer.set(viewProj);
				
				var model = matrixMap.get(MatrixType.MODEL);
				buffer.mult(model, buffer);
				break;
			case VIEW_PROJECTION:
				var projection = matrixMap.get(MatrixType.PROJECTION);
				var view = matrixMap.get(MatrixType.VIEW);
				
				buffer.set(projection);
				buffer.mult(view, buffer);
				break;
			default:
				throw new UnsupportedOperationException("The provided type of matrix: " + type + " can't be computed!");
		}
	}
	
	/**
	 * Sets the {@link Camera} used for rendering in the <code>AbstractRenderer</code>.
	 * It will automatically set the camera for the registered {@link RenderBucket} as well.
	 * 
	 * @param camera The camera to render with (not null).
	 */
	public void setCamera(Camera camera) {
		Validator.nonNull(camera, "The camera can't be null");
		
		this.buckets.values().forEach(bucket -> bucket.setCamera(camera));
		this.camera = camera;
	}
	
	/**
	 * <code>MatrixType</code> is the enumeration of all matrices used for
	 * rendering inside a {@link ShaderProgram}. 
	 * 
	 * @author GnosticOccultist
	 */
	public enum MatrixType {
		/**
		 * The model matrix used to display a {@link PhysicaMundi} correctly 
		 * in world-space. It should take into account the world transform not the local one.
		 */
		MODEL("modelMatrix", false),
		/**
		 * The view matrix used to display the scene-graph based on camera position.
		 */
		VIEW("viewMatrix", false),
		/**
		 * The projection matrix used to display the scene-graph based on 
		 * window size.
		 */
		PROJECTION("projectionMatrix", false),
		/**
		 * The view-projection matrix is computed by the camera (or the renderer) depending on
		 * implementations. It is mostly used to compute the {@link #VIEW_PROJECTION_MODEL}.
		 */
		VIEW_PROJECTION("viewProjectionMatrix", true),
		/**
		 * The view-projection-model matrix used to display an entire scene-graph
		 * correctly in 3D-space taking into account the camera, window and object's transform.
		 */
		VIEW_PROJECTION_MODEL("viewProjectionModelMatrix", true);
		
		/**
		 * The uniform name used inside the shader.
		 */
		private final String uniformName;
		/**
		 * Whether the matrix type can be computed.
		 */
		private final boolean compute;
		
		private MatrixType(String uniformName, boolean compute) {
			this.uniformName = uniformName;
			this.compute = compute;
		}
		
		/**
		 * Return the {@link Uniform} name used by the <code>MatrixType</code>.
		 * 
		 * @return The uniform name string.
		 */
		public String getUniformName() {
			return uniformName;
		}
		
		/**
		 * Return whether the <code>MatrixType</code> should be first computed,
		 * before being added to a {@link Uniform}.
		 * 
		 * @return Whether the matrix should be computed.
		 */
		public boolean canCompute() {
			return compute;
		}
	}
}
