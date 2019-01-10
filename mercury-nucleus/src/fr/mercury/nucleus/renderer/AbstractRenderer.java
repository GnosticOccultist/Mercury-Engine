package fr.mercury.nucleus.renderer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.readable.ReadableTransform;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.UniformStructure;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.renderer.queue.BucketType;
import fr.mercury.nucleus.renderer.queue.RenderBucket;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.MercuryException;

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
	 * The camera used by the renderer.
	 */
	protected final Camera camera;
	
	protected AbstractRenderer(Camera camera) {
		Validator.nonNull(camera);
		
		this.camera = camera;
		registerBucket(BucketType.OPAQUE);
		
		// Instantiates the matrices used for rendering.
		matrixMap.put(MatrixType.MODEL, new Matrix4f());
		matrixMap.put(MatrixType.VIEW, new Matrix4f());
		matrixMap.put(MatrixType.PROJECTION, new Matrix4f());
		matrixMap.put(MatrixType.VIEW_PROJECTION_MODEL, new Matrix4f());
	}
	
	/**
	 * Register a new {@link RenderBucket} with the specified {@link BucketType} for
	 * the <code>Renderer</code>.
	 * 
	 * @param type The bucket type to register.
	 */
	public void registerBucket(BucketType type) {
		Validator.nonNull(type);
		if(type.equals(BucketType.LEGACY) || type.equals(BucketType.NONE)) {
			throw new MercuryException("The bucket '" + type + "' cannot be registered!");
		}
		
		buckets.put(type, new RenderBucket(camera));
	}
	
	/**
	 * Submit the specified {@link AnimaMundi} to a {@link RenderBucket} matching 
	 * the {@link BucketType}. 
	 * <p>
	 * If the type of bucket isn't registered for this <code>Renderer</code>,
	 * it will not add the anima to be rendered, call {@link #registerBucket(BucketType)}
	 * to register the needed bucket's type.
	 * 
	 * @param anima The anima-mundi to add to a bucket.
	 * @return		Whether the anima-mundi has been added to a bucket.
	 */
	protected boolean submitToBucket(AnimaMundi anima) {
		if(anima.getBucket().equals(BucketType.NONE)) {
			return false;
		}
		
		var type = anima.getBucket();
		if(type.equals(BucketType.LEGACY)) {
			type = BucketType.OPAQUE;
		}
		
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
	 * Flushes all registered {@link RenderBucket} in the <code>Renderer</code>, by
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
	 * Setup the {@link Uniform} corresponding to the needed {@link MatrixType} specified by the provided
	 * {@link Material} and applied for the given {@link ShaderProgram}.
	 * 
	 * @param shader   The shader program to which the matrix uniforms need to be passed.
	 * @param material The material specifying which matrix type should be passed.
	 */
	protected void setupMatrixUniforms(ShaderProgram shader, Material material) {
		for(MatrixType type : MatrixType.values()) {
			var name = type.name();
			
			if(material.getPrefabUniforms().contains(name)) {
				// First force-compute the matrix before adding it to the uniform.
				if(type.isComputed()) {
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
			var property = physica.getEnvironmentProperty(prefabName);
			
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
		var buffer = matrixMap.get(type);
		buffer.set(matrix);
	}
	
	/**
	 * Stores the provided {@link Matrix4f} for the given usage {@link MatrixType}
	 * 
	 * @param type   The type of the rendering matrix.
	 * @param matrix The rendering matrix to store.
	 */
	public void setMatrix(MatrixType type, ReadableTransform transform) {
		var buffer = matrixMap.get(type);
		transform.asModelMatrix(buffer);
	}
	
	/**
	 * Compute the specified {@link MatrixType}, if possible, with the other matrices if they are provided.
	 * The type of matrix that can be computed are marked as {@link MatrixType#isComputed()}.
	 * 
	 * @param type The type of matrix to compute.
	 */
	public void computeMatrix(MatrixType type) {
		if(!type.isComputed()) {
			throw new IllegalArgumentException("The provided type of matrix: " + 
					type + " can't be computed!");
		}
		
		var buffer = matrixMap.get(type);
		
		switch (type) {
			case VIEW_PROJECTION_MODEL:
				var projection = matrixMap.get(MatrixType.PROJECTION);
				var view = matrixMap.get(MatrixType.VIEW);
				
				buffer.set(projection.mult(view, buffer));
				
				var model = matrixMap.get(MatrixType.MODEL);
				buffer.mult(model, buffer);
				break;
			default:
				throw new UnsupportedOperationException("The provided type of matrix: " + type + " can't be computed!");
		}
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
		 * The view-projection-model matrix used to display an entire scene-graph
		 * correctly in 3D-space taking into account the camera, window and object's transform.
		 */
		VIEW_PROJECTION_MODEL("viewProjectionModelMatrix", true);
		
		/**
		 * The uniform name used inside the shader.
		 */
		private final String uniformName;
		/**
		 * Whether the matrix type should be computed.
		 */
		private final boolean computed;
		
		private MatrixType(String uniformName, boolean computed) {
			this.uniformName = uniformName;
			this.computed = computed;
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
		public boolean isComputed() {
			return computed;
		}
	}
}
