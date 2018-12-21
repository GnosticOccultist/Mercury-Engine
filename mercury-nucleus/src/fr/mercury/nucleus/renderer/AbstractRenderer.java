package fr.mercury.nucleus.renderer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.readable.ReadableTransform;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.renderer.queue.BucketType;
import fr.mercury.nucleus.renderer.queue.RenderBucket;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.MercuryException;

public abstract class AbstractRenderer {
	
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
	 * The shader program used for rendering. This need to be changed to allow multiple
	 * render pass.
	 */
	protected ShaderProgram program;
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
		buckets.put(type, new RenderBucket(camera));
	}
	
	protected boolean submitToBucket(AnimaMundi anima) {
		if(anima.getBucket().equals(BucketType.NONE)) {
			return false;
		}
		
		BucketType type = anima.getBucket();
		
		if(type.equals(BucketType.LEGACY)) {
			type = BucketType.OPAQUE;
		}
		
		buckets.get(type).add(anima);
		return true;
	}
	
	protected void renderBucket(BucketType type) {
		if(type.equals(BucketType.LEGACY) || type.equals(BucketType.NONE)) {
			throw new MercuryException("The bucket '" + type + "' cannot be rendered!");
		}
		
		RenderBucket bucket = buckets.get(type);
		if(bucket == null) {
			throw new IllegalStateException("No bucket for type: " + type + " is defined in the renderer!");
		}
		bucket.sort();
		for(int i = 0; i < bucket.size(); i++) {
			AnimaMundi anima = bucket.array()[i];
			if(anima instanceof PhysicaMundi) {
				render((PhysicaMundi) anima);
			}
			anima.queueDistance = Double.NEGATIVE_INFINITY;
		}
	}
	
	protected abstract void render(PhysicaMundi anima);

	protected void flushBuckets() {
		buckets.values().forEach(RenderBucket::flush);
	}
	
	protected void setupUniforms() {
		// Only pass the view projection model for now..
		var name = MatrixType.VIEW_PROJECTION_MODEL.getUniformName();
		if(program.getUniform(name) == null) {
			program.addUniform(name, UniformType.MATRIX4F, matrixMap.get(MatrixType.VIEW_PROJECTION_MODEL));
		} else {
			program.getUniform(name).setValue(UniformType.MATRIX4F, matrixMap.get(MatrixType.VIEW_PROJECTION_MODEL));
			program.getUniform(name).upload(program);
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
	 * Compute the {@link MatrixType#VIEW_PROJECTION_MODEL} with the other
	 * matrices if they are provided.
	 * 
	 * @param type The type of matrix to compute, for now only view-projection-model.
	 */
	public void computeMatrix(MatrixType type) {
		if(!type.equals(MatrixType.VIEW_PROJECTION_MODEL)) {
			throw new IllegalArgumentException("The provided type of matrix: " + 
					type + " can't be computed!");
		}
		
		var buffer = matrixMap.get(type);
		
		var projection = matrixMap.get(MatrixType.PROJECTION);
		var view = matrixMap.get(MatrixType.VIEW);
		
		buffer.set(projection.mult(view, buffer));
		
		var model = matrixMap.get(MatrixType.MODEL);
		buffer.mult(model, buffer);
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
		MODEL("worldMatrix"),
		/**
		 * The view matrix used to display the scene-graph based on camera position.
		 */
		VIEW("viewMatrix"),
		/**
		 * The projection matrix used to display the scene-graph based on 
		 * window size.
		 */
		PROJECTION("projectionMatrix"),
		/**
		 * The view-projection-model matrix used to display an entire scene-graph
		 * correctly in 3D-space taking into account the camera, window and object's transform.
		 */
		VIEW_PROJECTION_MODEL("viewProjectionWorldMatrix");
		
		/**
		 * The uniform name used inside the shader.
		 */
		private final String uniformName;
		
		private MatrixType(String uniformName) {
			this.uniformName = uniformName;
		}
		
		/**
		 * Return the {@link Uniform} name used by the <code>MatrixType</code>.
		 * 
		 * @return The uniform name string.
		 */
		public String getUniformName() {
			return uniformName;
		}
	}
}
