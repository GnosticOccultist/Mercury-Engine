package fr.mercury.nucleus.renderer.opengl.shader.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Vector3f;
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
	 * The uniform type.
	 */
	private UniformType type = null;
	
	@OpenGLCall
	protected void create(ShaderProgram program) {
		if(location == UNKNOWN_LOCATION) {
			var location = GL20.glGetUniformLocation(program.getID(), name);
			if(location < 0) {
				setLocation(-1);
				System.err.println("The uniform '" + name + "' isn't declared in the shader: " + program + ".");
				return;
			}
			setLocation(location);
		}
	}
	
	@OpenGLCall
	public void upload(ShaderProgram program) {
		if(program.getID() == ShaderProgram.INVALID_ID) {
			System.err.println("ShaderProgram isn't yet created, cannot add uniform to it!");
			return;
		}
		
		if(getLocation() == UNDEFINED_LOCATION || getUniformType() == null) {
			System.err.println("The uniform: " + name + " is inactive or has no type!");
			return;
		}
		
		create(program);
		
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
			case VECTOR3F:
				Vector3f vec = (Vector3f) value;
				GL20.glUniform3f(location, vec.x, vec.y, vec.z);
				break;
			case MATRIX4F:
				try (MemoryStack stack = MemoryStack.stackPush()) {
					FloatBuffer fb = stack.mallocFloat(16);
					((Matrix4f) value).fillFloatBuffer(fb, true);
					fb.clear();
					GL20.glUniformMatrix4fv(location, false, fb);
				}
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
			case VECTOR3F:
				if(value instanceof Vector3f) {
					this.value = new Vector3f((Vector3f) value);
				} else if (value instanceof Color) { 
					this.value = ((Color) value).asVector3f();
				}
				break;
			case MATRIX4F:
				this.value = new Matrix4f((Matrix4f) value);
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
		 * Store <code>Vector3f</code> data.
		 */
		VECTOR3F("vec3"),
		/**
		 * Store <code>Matrix4f</code> data.
		 */
		MATRIX4F("mat4");
		
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
