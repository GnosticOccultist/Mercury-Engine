package fr.mercury.nucleus.renderer.opengl;

import java.nio.Buffer;

import org.lwjgl.opengl.GL15;

import fr.mercury.nucleus.utils.OpenGLCall;

public abstract class GLBuffer extends GLObject {
	
	/**
	 * The usage of the buffer.
	 */
	protected Usage usage;
	/**
	 * The data contained in the buffer.
	 */
	protected Buffer data = null;

	@OpenGLCall
	protected void bind() {
		GL15.glBindBuffer(getOpenGLType(), getID());
	}
	
	@OpenGLCall
	protected void unbind() {
		GL15.glBindBuffer(getOpenGLType(), 0);
	}
	
	/**
	 * <code>BufferType</code> gives informations about the uses of the <code>GLBuffer</code> and its
	 * purpose.
	 * <p>
	 * For example:
	 * <pre> 
	 * VERTEX_DATA defines that the <code>GLBuffer</code> is used for storing vertex data and can
	 * be passed to a shader for using it.
	 *
	 */
	public enum BufferType {
		/**
		 * The buffer is used for storing vertex data and can be used to pass those informations through a shader
		 * if needed.
		 */
		VERTEX_DATA,
		/**
		 * The buffer is used for storing indices that will help the GL rendering function "glDrawElements" 
		 * to perform indexed rendering.
		 */
		VERTEX_INDEXING,
	}
	
	protected BufferType getType() {
		return BufferType.VERTEX_DATA;
	}
	
	/**
	 * Return the OpenGL equivalent buffer type as an int corresponding to the enum
	 * value of the <code>BufferType</code>
	 * 
	 * @return The type of buffer.
	 */
	protected int getOpenGLType() {
		switch(getType()) {
			case VERTEX_DATA:
				return GL15.GL_ARRAY_BUFFER;
			case VERTEX_INDEXING:
				return GL15.GL_ELEMENT_ARRAY_BUFFER;
			default:
				throw new UnsupportedOperationException("Cannot convert the buffer type: " 
						+ getType() + " to an OpenGL equivalent!");
		}
	}
	
	/**
	 * <code>Usage</code> gives hints about how exactly the <code>GLBuffer</code>
	 * will be used. The usage pattern is composed of a reading/writing buffer information and
	 * updating rate of the buffer.
	 */
	public enum Usage {
		/**
		 * The user will be uploading the data once and will not read it, only the GL is reading
		 * the data. This is used when holding vertex data that has no reason to be updated.
		 */
		STATIC_DRAW,
		/**
		 * The user will be able to read data from the buffer but it is the GL that will upload data to it, for
		 * example exporting pixels into a buffer object.
		 */
		STATIC_READ,
		/**
		 * The user will be updating the data multiple times and will not read it, only the GL is reading
		 * the data. This is used when holding vertex data that needs to be updated.
		 */
		DYNAMIC_DRAW,
		/**
		 * The user will be able to read data from the buffer but it is the GL that will upload data to it multiple times, for
		 * example rendering to a Texture Buffer.
		 */
		DYNAMIC_READ;
	}
	
	protected Usage getUsage() {
		return usage;
	}
	
	/**
	 * Return the OpenGL equivalent usage as an int corresponding to the enum
	 * value of the <code>Usage</code>.
	 * 
	 * @return The usage of the buffer.
	 */
	public int getOpenGLUsage() {
		switch(getUsage()) {
			case STATIC_DRAW:
				return GL15.GL_STATIC_DRAW;
			case STATIC_READ:
				return GL15.GL_STATIC_READ;
			case DYNAMIC_DRAW:
				return GL15.GL_DYNAMIC_DRAW;
			case DYNAMIC_READ:
				return GL15.GL_DYNAMIC_READ;
			default:
				throw new UnsupportedOperationException("Cannot convert the buffer type: " + getType() + " to an OpenGL equivalent!");
		}
	}
}
