package fr.mercury.nucleus.renderer.opengl.shader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.renderer.opengl.GLObject;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>ShaderProgram</code> is a program defined by the user to compute rendering effects 
 * on the graphics hardware (GPU). The object to render go through a rendering pipeline to determine 
 * positions of vertices, vertices subdivisions, pixels colors, shading, lighting, normal/bump mapping 
 * and more effects.
 * <p>
 * In a final stage, it can also be used to apply a post-processing effect to the scene using a framebuffer
 * altering every desired objects (SSAO, Fog, FXAA/MSSA).
 * 
 * @author GnosticOccultist
 */
public final class ShaderProgram extends GLObject {
	
	/**
	 * The logger of the OpenGL context.
	 */
	private static final Logger logger = FactoryLogger.getLogger("mercury.opengl");
	
	public static ShaderProgram CURRENT = null;
	
	/**
	 * Return whether the provided <code>ShaderProgram</code> is a valid one.
	 * 
	 * @param program The shader program to check authenticity.
	 * @return		  Whether the shader program is valid.
	 */
	public static boolean valid(ShaderProgram program) {
		return GL20.glIsProgram(program.getID());
	}
	
	/**
	 * The list of shader sources.
	 */
	private final List<ShaderSource> sources;
	/**
	 * The table with the uniforms classed by their name.
	 */
	private final Map<String, Uniform> uniforms;
	
	/**
	 * Instantiates a new <code>ShaderProgram</code> with empty sources.
	 * <p>
	 * Please use {@link #attachSource(ShaderSource)} to add a <code>ShaderSource</code> and 
	 * {@link #upload()} to upload the program to the GPU.
	 */
	public ShaderProgram() {
		this.sources = new ArrayList<>();
		this.uniforms = new HashMap<>();
	}
	
	@Override
	@OpenGLCall
	public void upload() {
		create();
		
		for(var source : sources) {
			source.upload();
			
			// Attach the shader source.
			GL20.glAttachShader(id, source.getID());
		}
		
		// Link the program.
		GL20.glLinkProgram(id);
		
		// If failed, show info log.
		if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			throw new GLException("Error while linking shader program: "
					+ GL20.glGetProgramInfoLog(id, 1024));
		} else {
			logger.info("Successfully linked shader program!");
		}
		
		// Use the program to correctly upload uniform.
		use();
		
		for(var uniform : uniforms.values()) {
			uniform.upload(this);
		}
	}
	
	@OpenGLCall
	public void use() {
		if(CURRENT == this) {
			return;
		}
		
		if(getID() == INVALID_ID) {
			throw new GLException("The program isn't yet created!");
		}
		
		GL20.glUseProgram(id);
		
		CURRENT = this;
	}
	
	/**
	 * Attach a <code>ShaderSource</code> to the <code>ShaderProgram</code>.
	 * <p>
	 * The source cannot be null.
	 * 
	 * @param source The source to attach.
	 * @return		 The program with the attached source.
	 */
	public ShaderProgram attachSource(ShaderSource source) {
		Validator.nonNull(source, "The shader source cannot be null.");
		
		sources.add(source);
		return this;
	}
	
	/**
	 * Add a {@link Uniform} to the <code>ShaderProgram</code> with the specified
	 * name, type and value.
	 * 
	 * @param name  The name of the uniform.
	 * @param type  The value's type of the uniform.
	 * @param value The value contained by the uniform.
	 * @return		The program with the new uniform.
	 */
	public ShaderProgram addUniform(String name, UniformType type, Object value) {
		Validator.nonNull(type, "The uniform's type cannot be null!");
		Validator.nonNull(value, "The uniform's value cannot be null!");
		Validator.nonNull(name, "The uniform's name cannot be null!");
		
		Uniform uniform = new Uniform();
		uniform.setName(name);
		uniform.setValue(type, value);
		
		uniforms.put(name, uniform);
		return this;
	}
	
	/**
	 * Return the registered {@link Uniform} with the provided name, or
	 * null if it doesn't exist.
	 * 
	 * @param name The name of the uniform to get.
	 * @return	   The uniform matching the name.
	 */
	public Uniform getUniform(String name) {
		return uniforms.get(name);
	}
	
	@Override
	public void cleanup() {
		uniforms.values().forEach(Uniform::cleanup);
		super.cleanup();
	}
	
	@Override
	@OpenGLCall
	protected Integer acquireID() {
		return GL20.glCreateProgram();
	}
	
	@Override
	@OpenGLCall
	protected Consumer<Integer> deleteAction() {
		return GL20::glDeleteProgram;
	}
}
