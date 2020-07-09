package fr.mercury.nucleus.renderer.opengl.shader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import fr.mercury.nucleus.renderer.opengl.shader.uniform.UniformField;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>ShaderProgram</code> is a program defined by the user to compute rendering effects on the graphics hardware (GPU). 
 * The object to render go through a rendering pipeline to determine positions of vertices, vertices subdivisions, 
 * pixels colors, shading, lighting, normal/bump mapping and more effects. 
 * It can also be using variable called {@link Uniform} which can be modified at any time (<i>for example a color, or a texture sampler</i>).
 * <p>
 * In a final stage, it can also be used to apply a post-processing effect to the scene using a framebuffer
 * altering every desired objects (SSAO, Fog, FXAA/MSSA).
 * 
 * @see Uniform
 * @see ShaderSource
 * 
 * @author GnosticOccultist
 */
public final class ShaderProgram extends GLObject {
	
	/**
	 * The logger of the OpenGL context.
	 */
	private static final Logger logger = FactoryLogger.getLogger("mercury.opengl");
	
	/**
	 * The currenlty used program by the rendering state of <code>OpenGL</code>.
	 */
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
	 * Whether the shader program has been modified and needs to be re-linked.
	 */
	private boolean needsUpdate = true;
	
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
		// Check if we need to create the program first.
		create();
		
		if(needsUpdate()) {
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
				needsUpdate = false;
			}
		}
		
		// Use the program to correctly upload uniform.
		use();
		
		// Upload all the uniforms to the program if needed.
		for(var uniform : uniforms.values()) {
			uniform.upload(this);
		}
	}
	
	/**
	 * Install this <code>ShaderProgram</code> as part of the current rendering state
	 * of <code>OpenGL</code>. 
	 * It keeps track of the bounded program, to prevent useless call of this function.
	 * 
	 * @throws GLException Thrown if the program isn't yet created.
	 */
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
	 * Attach a {@link ShaderSource} to the <code>ShaderProgram</code>.
	 * <p>
	 * The source cannot be null.
	 * 
	 * @param source The source to attach.
	 * @return		 The program with the attached source.
	 */
	public ShaderProgram attachSource(ShaderSource source) {
		Validator.nonNull(source, "The shader source cannot be null.");
		
		sources.add(source);
		needsUpdate = true;
		return this;
	}
	
	/**
	 * Attach an array of {@link ShaderSource} to the <code>ShaderProgram</code>.
	 * <p>
	 * Each of the provided sources must not be null.
	 * 
	 * @param source The sources to attach.
	 * @return		 The program with the attached sources.
	 */
	public ShaderProgram attachSources(ShaderSource... sources) {
		for(int i = 0; i < sources.length; i++) {
			attachSource(sources[i]);
		}
		return this;
	}
	
	/**
	 * Attach a list of {@link ShaderSource} to the <code>ShaderProgram</code>.
	 * <p>
	 * Each of the provided sources must not be null.
	 * 
	 * @param source The sources to attach.
	 * @return		 The program with the attached sources.
	 */
	public ShaderProgram attachSources(List<ShaderSource> sources) {
		for(int i = 0; i < sources.size(); i++) {
			attachSource(sources.get(i));
		}
		return this;
	}
	
	/**
	 * Detach the provided {@link ShaderSource} from the <code>ShaderProgram</code>, if
	 * it was previously attached to it.
	 * Note that if the source or the program isn't yet created, it will not continue 
	 * and leave a warning message.
	 * 
	 * @param source The source to be detached from the program.
	 */
	@OpenGLCall
	public void detachShader(ShaderSource source) {
		if(!sources.contains(source)) {
			logger.warning("The source to detach has never been attached to the program!");
		}
		
		if(getID() == INVALID_ID || source.getID() == INVALID_ID) {
			logger.warning("Can't detach shader source from program, if it isn't yet created!");
			return;
		}
		
		GL20.glDetachShader(getID(), source.getID());
		sources.remove(source);
	}
	
	/**
	 * Add a {@link Uniform} to the <code>ShaderProgram</code> with the specified
	 * name, type and value. 
	 * If a uniform with the name already exists, it will simply set its type and 
	 * value without instantiating a new one.
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
		
		// Check if the uniform doesn't already exists.
		var uniform = getUniform(name);
		if(uniform == null) {
			uniform = new Uniform();
			uniform.setName(name);
			uniforms.put(name, uniform);
		}
		
		uniform.setValue(type, value);
		return this;
	}
	
	/**
	 * Registers all {@link Uniform} present on the provided object into the <code>ShaderProgram</code>.
	 * This method identifies getter methods of uniform value using the {@link UniformField} annotation, 
	 * which must specify the name and {@link UniformType} of the uniform.
	 * <p>
	 * A uniform getter method is declared as valid by the method if:
	 * <ul><li>The method is annotated with the <code>UniformField</code> annotation.</li>
	 * <li>The method is declared as public and accessible.</li>
	 * <li>The method doesn't need any parameters.</li></ul>
	 * 
	 * Note that the method, however, doesn't need to start with the 'get...' syntax, as opposed to 
	 * <code>Java</code> getters.
	 * 
	 * @param object The object instance to register uniforms from (not null).
	 * 
	 * @see UniformField
	 */
	public void register(Object object) {
		var clazz = object.getClass();
		
    	// Find all methods that are annotated with 'UniformField'.
    	Method[] methods = clazz.getMethods();
    	for(Method method : methods) {
    		var mods = method.getModifiers();
    		if(!Modifier.isPublic(mods)) {
    			continue;
    		}
    		if(!method.isAnnotationPresent(UniformField.class)) {
    			continue;
    		}
    		if(method.getParameterTypes().length != 0) {
    			continue;
    		}
    		
    		// Make sure we are using the right shader program.
    		use();
    		
    		var annotation = method.getAnnotation(UniformField.class);
    		try {
    			var uniformValue = method.invoke(object);
    			addUniform(annotation.name(), annotation.type(), uniformValue);
    		} catch(IllegalAccessException | InvocationTargetException ex) {
    			throw new RuntimeException("Error occured during registering of uniforms for: " + object, ex);
    		}
    	}
	}
	
	/**
	 * Return the registered {@link Uniform} with the provided name, or
	 * null if it doesn't exist.
	 * 
	 * @param name The name of the uniform to get.
	 * @return	   The uniform matching the name, or null if none.
	 */
	public Uniform getUniform(String name) {
		return uniforms.get(name);
	}
	
	/**
	 * Return whether the <code>ShaderProgram</code> needs to be relinked,
	 * to take into account the latest changes.
	 * 
	 * @return Whether the shader program needs to be relinked.
	 */
	protected boolean needsUpdate() {
		return needsUpdate;
	}
	
	@Override
	@OpenGLCall
	public void cleanup() {
		if(CURRENT == this) {
			CURRENT = null;
		}
		
		uniforms.values().forEach(Uniform::cleanup);
		uniforms.clear();
		for(int i = 0; i < sources.size(); i++) {
			var source = sources.get(i);
			detachShader(source);
			source.cleanup();
		}
		sources.clear();
		
		super.cleanup();
	}
	
	@Override
	protected void restart() {
		this.needsUpdate = true;
		
		super.restart();
	}
	
	@Override
	@OpenGLCall
	public Runnable onDestroy(int id) {
		return () -> GL20.glDeleteProgram(id);
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
