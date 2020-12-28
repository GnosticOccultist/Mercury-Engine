package fr.mercury.nucleus.renderer.opengl.shader.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Matrix3f;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Vector2f;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.math.objects.Vector4f;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.scenegraph.environment.Fog;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.data.Allocator;

/**
 * <code>Uniform</code> defines a variable inside a "glsl" file (<i>for example a shader</i>), to which the user can 
 * attribute a value to pass through the {@link ShaderProgram}.
 * There multiple ways to declare a uniform inside a shader program:
 * <ul><li>Invoke the basic method from an existing program {@link ShaderProgram#addUniform(String, UniformType, Object)}.</li>
 * <li>Register all uniforms declared in a class using {@link ShaderProgram#register(Object)}.</li>
 * <li>Create a structure class composed of uniforms which implements {@link UniformStructure}.</li></ul>
 * In this last case, you must declare the structure in a material file as a prefab, for an example look at the {@link Fog} class.
 * 
 * <p>
 * Note that the uniform is the same within a particular rendering call for each shader stage (rendering pipeline), 
 * but you can change its value between each invocation.
 * 
 * @see ShaderProgram
 * @see UniformField
 * @see UniformStructure
 * 
 * @author GnosticOccultist
 */
public class Uniform {
	
	/**
	 * The logger of the OpenGL context.
	 */
	private static final Logger logger = FactoryLogger.getLogger("mercury.opengl");
	
	/**
	 * The location for an inactive uniform or with syntax error.
	 */
	public static final int UNDEFINED_LOCATION = -1;
	/**
	 * The location for an unregistered uniform.
	 */
	public static final int UNKNOWN_LOCATION = -2;
	
	/**
	 * The assigned location of the uniform.
	 */
	private int location = UNKNOWN_LOCATION;
	/**
	 * The name of the uniform used inside the shader.
	 */
	private String name = null;
	/**
	 * The associated value with the uniform.
	 */
	private Object value = null;
	/**
	 * A float buffer for matrices or array, most efficient format
	 * to be sent to GL faster.
	 */
	private FloatBuffer buffer = null;
	/**
	 * The uniform type.
	 */
	private UniformType type = null;
	/**
	 * Whether the uniform has been changed and needs 
	 * to be re-uploaded to program.
	 */
	private boolean needsUpdate = true;
	
	/**
	 * Creates the <code>Uniform</code> by acquiring its location from the provided
	 * {@link ShaderProgram} and set it as its ID.
	 * 
	 * @param program The program to be queried.
	 */
	@OpenGLCall
	protected void create(ShaderProgram program) {
		if(location == UNKNOWN_LOCATION) {
			var location = GL20.glGetUniformLocation(program.getID(), name);
			if(location < 0) {
				setLocation(-1);
				logger.warning("The uniform '" + name + "' isn't declared in the shader: " + program + ".");
				return;
			}
			setLocation(location);
		}
	}
	
	/**
	 * Uploads the <code>Uniform</code> with its stored value to be used inside 
	 * the {@link ShaderProgram} currently bounded.
	 * <p>
	 * Note that the uniform is capable of knowing when its value needs to be resent
	 * through the program, so calling this method won't always result in OpenGL calls.
	 * 
	 * @param program The program on which the uniform is used.
	 */
	@OpenGLCall
	public void upload(ShaderProgram program) {
		if(program.getID() == ShaderProgram.INVALID_ID) {
			logger.error("ShaderProgram isn't yet created, cannot add uniform to it!");
			return;
		}
		
		create(program);
		
		if(getLocation() == UNDEFINED_LOCATION || getUniformType() == null) {
			logger.error("The uniform '" + name + "' is inactive or has no type!");
			return;
		}
		
		if(!needsUpdate()) {
			return;
		}
		
		if(program != ShaderProgram.CURRENT) {
			throw new GLException("The program on which the uniform needs to be sent, "
					+ "isn't the one currently bound to the GL context!");
		}
		
		switch (getUniformType()) {
			case FLOAT:
				GL20.glUniform1f(location, (float) value);
				break;
			case TEXTURE2D:
			case INTEGER:
				GL20.glUniform1i(location, (int) value);
				break;
			case BOOLEAN:
				Boolean bool = (Boolean) value;
				GL20.glUniform1i(location, bool ? GL11.GL_TRUE : GL11.GL_FALSE);
				break;
			case VECTOR2F:
				Vector2f vec2 = (Vector2f) value;
				GL20.glUniform2f(location, vec2.x, vec2.y);
				break;
			case VECTOR3F:
				if(value instanceof Vector3f) {
					Vector3f vec3 = (Vector3f) value;
					GL20.glUniform3f(location, vec3.x, vec3.y, vec3.z);
				} else if(value instanceof Color) {
					Color color = (Color) value;
					GL20.glUniform3f(location, color.r, color.g, color.b);
				}
				break;
			case VECTOR4F:
				if(value instanceof Vector4f) {
					Vector4f vec4 = (Vector4f) value;
					GL20.glUniform4f(location, vec4.x, vec4.y, vec4.z, vec4.w);
				} else if(value instanceof Color) {
					Color color = (Color) value;
					GL20.glUniform4f(location, color.r, color.g, color.b, color.a);
				}
				break;
			case MATRIX3F:
				buffer.rewind();
				GL20.glUniformMatrix3fv(location, false, buffer);
				break;
			case MATRIX4F:
				buffer.rewind();
				GL20.glUniformMatrix4fv(location, false, buffer);
				break;
			default:
				throw new UnsupportedOperationException(
						"Unsupported uniform type: " + getUniformType());
		}
		
		this.needsUpdate = false;
	}
	
	/**
	 * Set the value contained by the <code>Uniform</code>.
	 * <p>
	 * The value and the type cannot be null.
	 * 
	 * @param type  The type of value to contain.
	 * @param value The value to contain.
	 */
	public void setValue(UniformType type, Object value) {
		if(getLocation() == UNDEFINED_LOCATION) {
			return;
		}
		
		if(value == null || type == null) {
			throw new IllegalStateException("The uniform: " + name + 
					" cannot have a value or type null!");
		}
	
		switch (type) {
			case TEXTURE2D:
			case FLOAT:
			case INTEGER:
			case BOOLEAN:
				if(value.equals(this.value)) {
					return;
				}
				
				this.value = value;
				break;
			case VECTOR2F:
				if(value.equals(this.value)) {
					return;
				}
				
				if(this.value == null) {
					this.value = new Vector2f((Vector2f) value);
				} else {
					((Vector2f) this.value).set((Vector2f) value);
				}
				
				break;
			case VECTOR3F:
				if(value.equals(this.value)) {
					return;
				}
				
				if(value instanceof Vector3f && !(this.value instanceof Vector3f)) {
					this.value = new Vector3f((Vector3f) value);
					break;
				} else if (value instanceof Color && !(this.value instanceof Color)) { 
					this.value = new Color((Color) value);
					break;
				}
				
				if(value instanceof Vector3f) {
					((Vector3f) this.value).set((Vector3f) value);
				}
				if(value instanceof Color) {
					((Color) this.value).set((Color) value);
				}
				
				break;
			case VECTOR4F:
				if(value.equals(this.value)) {
					return;
				}
				
				if(value instanceof Vector4f && !(this.value instanceof Vector4f)) {
					this.value = new Vector4f((Vector4f) value);
					break;
				} else if (value instanceof Color && !(this.value instanceof Color)) { 
					this.value = new Color((Color) value);
					break;
				}
				
				if(value instanceof Vector4f) {
					((Vector4f) this.value).set((Vector4f) value);
				}
				if(value instanceof Color) {
					((Color) this.value).set((Color) value);
				}
				
				break;
			case MATRIX3F:
				if(value.equals(this.value)) {
					return;
				}
				
				var matrix3 = (Matrix3f) value;
				if(buffer == null) {
					buffer = Allocator.allocFloat(9);
				} else {
					buffer.clear();
				}
				matrix3.populate(buffer);
				buffer.flip();
				if(this.value == null) {
					this.value = new Matrix3f(matrix3);
				} else {
					((Matrix3f) this.value).set(matrix3);
				}
				
				break;
			case MATRIX4F:
				if(value.equals(this.value)) {
					return;
				}
				
				var matrix4 = (Matrix4f) value;
				if(buffer == null) {
					buffer = Allocator.allocFloat(16);
				} else {
					buffer.clear();
				}
				matrix4.populate(buffer, false);
				buffer.flip();
				if(this.value == null) {
					this.value = new Matrix4f(matrix4);
				} else {
					((Matrix4f) this.value).set(matrix4);
				}
				break;
			default:
				throw new UnsupportedOperationException(
						"Unsupported uniform type: " + getUniformType());
		}
		
		this.type = type;
		this.needsUpdate = true;
	}
	
	/**
	 * Cleanup the <code>Uniform</code> by resetting its ID and setting its 
	 * {@link #needsUpdate() update tag} to true.
	 */
	public void cleanup() {
		this.needsUpdate = true;
		setLocation(UNKNOWN_LOCATION);
	}
	
	/**
	 * Return whether the <code>Uniform</code> needs its value uploaded to the 
	 * {@link ShaderProgram}.
	 * 
	 * @return Whether the uniform value needs to be reuploaded through the shader program.
	 */
	protected boolean needsUpdate() {
		return needsUpdate;
	}
	
	/**
	 * Return the name of the <code>Uniform</code>.
	 * 
	 * @return The name of the uniform.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name of the <code>Uniform</code>.
	 * 
	 * @param name The name of the uniform.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Return the location of the <code>Uniform</code>.
	 * 
	 * @return The location of the uniform.
	 */
	public int getLocation() {
		return location;
	}
	
	/**
	 * Set the location of the <code>Uniform</code>.
	 * 
	 * @param location The location of the uniform.
	 */
	public void setLocation(int location) {
		this.location = location;
	}
	
	/**
	 * Return the <code>UniformType</code> of the <code>Uniform</code>.
	 * 
	 * @return The uniform's type.
	 */
	public UniformType getUniformType() {
		return type;
	}
	
	/**
	 * Set the <code>UniformType</code> of the <code>Uniform</code>.
	 * 
	 * @param type The uniform's type.
	 */
	public void setUniformType(UniformType type) {
		this.type = type;
	}
	
	/**
	 * <code>UniformType</code> is the enumeration for the
	 * uniform variable type.
	 * 
	 * @author GnosticOccultist
	 */
	public enum UniformType {
		/**
		 * Store float data.
		 */
		FLOAT("float"),
		/**
		 * Store integer data.
		 */
		INTEGER("int"),
		/**
		 * Store boolean data.
		 */
		BOOLEAN("bool"),
		/**
		 * Store <code>Vector2f</code> data.
		 */
		VECTOR2F("vec2"),
		/**
		 * Store <code>Vector3f</code> data.
		 */
		VECTOR3F("vec3"),
		/**
		 * Store <code>Vector4f</code> data.
		 */
		VECTOR4F("vec4"),
		/**
		 * Store <code>Matrix3f</code> data.
		 */
		MATRIX3F("mat3"),
		/**
		 * Store <code>Matrix4f</code> data.
		 */
		MATRIX4F("mat4"),
		/**
		 * Store <code>Texture2D</code> data.
		 */
		TEXTURE2D("sampler2D");
		
		private String GLSLType;
		
		private UniformType(String GLSLType) {
			this.GLSLType = GLSLType;
		}
		
		/**
		 * Return the 'glsl' equivalent for this <code>UniformType</code>.
		 * 
		 * @return The GLSL equivalent for this uniform type.
		 */
		public String toGLSLType() {
			return GLSLType;
		}
		
		/**
		 * Return the <code>UniformType</code> corresponding to the given 
		 * 'glsl' <code>Uniform</code> type.
		 * 
		 * @return The uniform type from the glsl type.
		 */
		public static UniformType getUniformType(String glslType) {
			for(UniformType value : UniformType.values()) {
				if(value.toGLSLType().equals(glslType)) {
					return value;
				}
			}
			throw new UnsupportedOperationException("Cannot find any equivalent uniform type for: " + glslType);
		}
	}
}
