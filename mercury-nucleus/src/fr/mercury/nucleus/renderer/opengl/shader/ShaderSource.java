package fr.mercury.nucleus.renderer.opengl.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import fr.mercury.nucleus.renderer.opengl.GLObject;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>ShaderSource</code> is a specific <code>ShaderProgram</code> part which is
 * defined by its {@link ShaderType}.
 * <p>
 * Attaching multiple <code>ShaderSource</code> to a <code>ShaderProgram</code> will construct
 * a graphics pipeline used to render an object in the scene-graph.
 * 
 * @author GnosticOccultist
 */
public final class ShaderSource extends GLObject {

	private ShaderType type; 
	private String source;
	
	public ShaderSource(ShaderType type, String source) {
		this.type = type;
		this.source = source;
	}
	
	@OpenGLCall
	private void create() {
		if(getID() == INVALID_ID) {
			
			var id = GL20.glCreateShader(type.toOpenGLType());
			if(id <= 0) {
				throw new MercuryException("Failed to create shader source!");
			}
			
			setID(id);
		}
	}
	
	@Override
	@OpenGLCall
	protected void upload() {
		create();
		
		if(source.isEmpty() || source == null) {
			System.err.println("Warning: The source code is null or empty for " + type);
		}
	
		GL20.glShaderSource(id, source);
		GL20.glCompileShader(id);

		if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			throw new IllegalArgumentException("Error while compiling shader code: " 
						+ GL20.glGetShaderInfoLog(id, 1024));
		}
	}
	
	@Override
	@OpenGLCall
	protected void cleanup() {
		if(getID() < 0) {
			System.err.println("Shader program not yet uploaded to GPU, cannot delete.");
			return;
		}
		
		GL20.glDeleteShader(id);
		setID(-1);
	}
	
	/**
	 * <code>ShaderType</code> represents the type of OpenGL Shader, which control its own
	 * pipeline.
	 */
	public enum ShaderType {
		/**
		 * Vertex processing shader. Use it for computing gl_Position, the vertex position.
		 */
		VERTEX(GL20.GL_VERTEX_SHADER),
		/**
		 * Geometry assembly shader. For example it can compile a polygon mesh from data.
		 */
		GEOMETRY(GL32.GL_GEOMETRY_SHADER),
		/**
		 * Fragment rasterization shader. It can be used to determine the color of the pixel.
		 */
		FRAGMENT(GL20.GL_FRAGMENT_SHADER),
		/**
		 * Tesselation control shader. For example it can determine how often an input 
		 * VBO Patch should be subdivided.
		 */
		TESS_CONTROL(GL40.GL_TESS_CONTROL_SHADER),
		/**
		 * Tesselation evaluation shader. It is similar to the vertex shader, as it computes 
		 * the interpolated positions and some other per-vertex data.
		 */
		TESS_EVAL(GL40.GL_TESS_EVALUATION_SHADER),
		/**
		 * Compute shader. Should be used entirely for computing arbitrary information. 
		 * While it can do rendering, it is generally used for tasks not directly 
		 * related to drawing triangles and pixels.
		 */
		COMPUTE(GL43.GL_COMPUTE_SHADER);
		
		private int openGLType;
		
		private ShaderType(int openGLType) {
			this.openGLType = openGLType;
		}
		
		/**
		 * @return The OpenGL type equivalent to this <code>ShaderType</code>.
		 */
		public int toOpenGLType() {
			return openGLType;
		}
	}
}
