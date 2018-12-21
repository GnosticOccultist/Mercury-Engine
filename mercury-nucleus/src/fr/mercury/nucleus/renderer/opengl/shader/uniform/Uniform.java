package fr.mercury.nucleus.renderer.opengl.shader.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Vector2f;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.math.objects.Vector4f;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>Uniform</code> defines a variable inside a "glsl" file (<i>for example a shader</i>), to
 * which the user can attribute a value to pass through the <code>ShaderProgram</code>.
 * <p>
 * Note that the uniform is the same within a particular rendering call for each shader stage 
 * (rendering pipeline), but you can change its value between each invocation.
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
	 * 
	 * @param program
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
		
		switch (getUniformType()) {
			case FLOAT:
				GL20.glUniform1f(location, (float) value);
				break;
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
				Vector3f vec3 = (Vector3f) value;
				GL20.glUniform3f(location, vec3.x, vec3.y, vec3.z);
				break;
			case VECTOR4F:
				Vector4f vec4 = (Vector4f) value;
				GL20.glUniform4f(location, vec4.x, vec4.y, vec4.z, vec4.w);
				break;
			case MATRIX4F:
				
				try (MemoryStack stack = MemoryStack.stackPush()) {
					FloatBuffer fb = stack.mallocFloat(16);
					fb.put((FloatBuffer) buffer);
					fb.clear();
					GL20.glUniformMatrix4fv(location, false, fb);
				}
				break;
			case TEXTURE2D:
				GL20.glUniform1i(location, (int) value);
				break;
			default:
				throw new UnsupportedOperationException(
						"Unsupported uniform type: " + getUniformType());
		}
	}
	
	public void cleanup() {
		setLocation(-2);
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
			case FLOAT:
			case INTEGER:
			case BOOLEAN:
				if (value.equals(this.value)) {
					return;
				}
				this.value = value;
				break;
			case VECTOR2F:
				this.value = new Vector2f((Vector2f) value);
				break;
			case VECTOR3F:
				if(value instanceof Vector3f) {
					this.value = new Vector3f((Vector3f) value);
				} else if (value instanceof Color) { 
					this.value = ((Color) value).asVector3f();
				}
				break;
			case VECTOR4F:
				if(value instanceof Vector4f) {
					this.value = new Vector4f((Vector4f) value);
				} else if (value instanceof Color) { 
					this.value = ((Color) value).asVector4f();
				}
				break;
			case MATRIX4F:
				if(value.equals(this.value)) {
					return;
				}
				
				Matrix4f matrix = (Matrix4f) value;
				if(buffer == null) {
					buffer = MemoryUtil.memAllocFloat(16);
				} else {
					buffer.clear();
				}
				matrix.fillFloatBuffer(buffer, true);
				buffer.clear();
				if(this.value == null) {
					this.value = new Matrix4f(matrix);
				} else {
					((Matrix4f) this.value).set(matrix);
				}
				break;
			case TEXTURE2D:
				this.value = value;	
				break;
			default:
				throw new UnsupportedOperationException(
						"Unsupported uniform type: " + getUniformType());
		}
		
		this.type = type;
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
