package fr.mercury.nucleus.renderer.opengl.shader;

import java.util.function.Consumer;

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
	
	/**
	 * The type of shader for the source.
	 */
	private ShaderType type; 
	/**
	 * The source code of the shader.
	 */
	private String source;
	
	/**
	 * Instantiates a new <code>ShaderSource</code> with the provided {@link ShaderType type}
	 * and the source code.
	 * <p>
	 * It should only be used by the <code>GLSLLoader</code> when reading a 'glsl' file.
	 */
	public ShaderSource(ShaderType type, String source) {
		this.type = type;
		this.source = source;
	}
	
	@Override
	@OpenGLCall
	protected void upload() {
		if(source.isEmpty() || source == null) {
			logger.warning("The source code is null or empty for " + type);
			return;
		}
		
		create();
	
		GL20.glShaderSource(id, source);
		GL20.glCompileShader(id);

		if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			throw new MercuryException("Error while compiling shader code: " 
						+ GL20.glGetShaderInfoLog(id, 1024));
		}
	}
	
	@Override
	@OpenGLCall
	protected Integer acquireID() {
		return GL20.glCreateShader(type.toOpenGLType());
	}
	
	@Override
	@OpenGLCall
	protected Consumer<Integer> deleteAction() {
		return GL20::glDeleteShader;
	}
	
	/**
	 * <code>ShaderType</code> represents the type of OpenGL Shader, which control its own
	 * pipeline.
	 */
	public enum ShaderType {
		/**
		 * Vertex processing shader. Use it for computing gl_Position, the vertex position.
		 */
		VERTEX(GL20.GL_VERTEX_SHADER, "vert"),
		/**
		 * Geometry assembly shader. For example it can compile a polygon mesh from data.
		 */
		GEOMETRY(GL32.GL_GEOMETRY_SHADER, "geom"),
		/**
		 * Fragment rasterization shader. It can be used to determine the color of the pixel.
		 */
		FRAGMENT(GL20.GL_FRAGMENT_SHADER, "frag"),
		/**
		 * Tesselation control shader. For example it can determine how often an input 
		 * VBO Patch should be subdivided.
		 */
		TESS_CONTROL(GL40.GL_TESS_CONTROL_SHADER, "tessctrl"),
		/**
		 * Tesselation evaluation shader. It is similar to the vertex shader, as it computes 
		 * the interpolated positions and some other per-vertex data.
		 */
		TESS_EVAL(GL40.GL_TESS_EVALUATION_SHADER, "tesseval"),
		/**
		 * Compute shader. Should be used entirely for computing arbitrary information. 
		 * While it can do rendering, it is generally used for tasks not directly 
		 * related to drawing triangles and pixels.
		 */
		COMPUTE(GL43.GL_COMPUTE_SHADER, "comp");
		
		private int openGLType;
		private String extension;
		
		private ShaderType(int openGLType, String extension) {
			this.openGLType = openGLType;
			this.extension = extension;
		}
		
		/**
		 * Return the <code>ShaderType</code> from an extension.
		 * 
		 * @param extension The extension.
		 * @return			The shader type or null.
		 */
		public static ShaderType fromExtension(String extension) {
			for(int i = 0; i < ShaderType.values().length; i++) {
				if(ShaderType.values()[i].extension.equals(extension)) {
					return ShaderType.values()[i];
				}
			}
			return null;
		}
		
		/**
		 * Return the OpenGL type equivalent to this <code>ShaderType</code>
		 * 
		 * @return The OpenGL type equivalent to the type.
		 */
		public int toOpenGLType() {
			return openGLType;
		}
	}
}
