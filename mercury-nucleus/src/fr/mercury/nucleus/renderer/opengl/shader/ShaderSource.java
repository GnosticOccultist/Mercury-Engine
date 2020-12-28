package fr.mercury.nucleus.renderer.opengl.shader;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.asset.GLSLLoader;
import fr.mercury.nucleus.renderer.opengl.GLObject;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>ShaderSource</code> is a specific {@link ShaderProgram} part which is
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
	 * The defines for the shader source.
	 */
	private String defines;
	/**
	 * The string buffer used to upload shader source.
	 */
	private final StringBuffer buffer = new StringBuffer(250);
	/**
	 * Whether the source has been changed and needs 
	 * to be re-uploaded to program.
	 */
	private boolean needsUpdate = true;
	
	/**
	 * Instantiates a new <code>ShaderSource</code> with the provided {@link ShaderType type}
	 * and the source code. The provided source must not be empty or null.
	 * <p>
	 * It should only be used by the {@link GLSLLoader} when reading a 'glsl' file.
	 */
	public ShaderSource(ShaderType type, String source) {
		Validator.nonNull(type);
		Validator.nonEmpty(source);
		
		this.type = type;
		this.source = source;
	}
	
	@Override
	@OpenGLCall
	protected void upload() {
		// Can't compile null or empty source for shader.
		if(source.isEmpty() || source == null) {
			logger.warning("The source code is null or empty for " + type);
			return;
		}
		
		create();
		
		// The source doesn't need to be re-uploaded.
		if(!needsUpdate()) {
			return;
		}
	
		GL20.glShaderSource(id, generateSource());
		GL20.glCompileShader(id);

		if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			throw new MercuryException("Error while compiling shader code: " 
						+ GL20.glGetShaderInfoLog(id, 1024));
		}
	}
	
	/**
	 * Generates the entire shader code of the <code>ShaderSource</code>,
	 * based on the provided source and defines.
	 * 
	 * @return The buffer containing the generated shader code.
	 */
	private StringBuffer generateSource() {
		buffer.setLength(0);
		
		// Access the version since it needs to be at top.
		String version = source.substring(0, source.indexOf("\n"));
		buffer.append(version + "\n");
		
		// Append defines if any.
		if(defines != null && !defines.isEmpty()) {
			buffer.append(defines);
		}
		
		// Append the source code without the version.
		buffer.append(source.substring(version.length(), source.length()));
		
		return buffer;
	}
	
	/**
	 * Sets the source code used by the <code>ShaderSource</code>.
	 * <p>
	 * The source code can't be empty or null.
	 * 
	 * @param source The source code to be used by the shader.
	 */
	public void setSource(String source) {
		Validator.nonEmpty(source, "Can't use null or empty source for shader!");
		
		this.source = source;
		this.needsUpdate = true;
	}
	
	/**
	 * Sets the defines list used by the <code>ShaderSource</code>.
	 * <p>
	 * The defines list can't be empty or null.
	 * 
	 * @param source The defines list to be used by the shader.
	 */
	public void setDefines(String defines) {
		Validator.nonEmpty(source, "Can't use null or empty source for shader!");
		
		this.defines = defines;
		this.needsUpdate = true;
	}
	
	/**
	 * Return whether the <code>ShaderSource</code> needs to be recompiled,
	 * to take into account the latest changes.
	 * 
	 * @return Whether the shader source needs to be recompiled.
	 */
	protected boolean needsUpdate() {
		return needsUpdate;
	}
	
	@Override
	protected void restart() {
		this.needsUpdate = true;
		
		super.restart();
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
	
	@Override
	@OpenGLCall
	public Runnable onDestroy(int id) {
		return () -> GL20.glDeleteShader(id);
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
