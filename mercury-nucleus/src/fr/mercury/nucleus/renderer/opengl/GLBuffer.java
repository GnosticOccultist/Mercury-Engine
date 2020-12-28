package fr.mercury.nucleus.renderer.opengl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBuffer;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType.Format;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>GLBuffer</code> represents an abstraction layer for every OpenGL buffers 
 * such as the {@link VertexBuffer}.
 * <p>
 * It extends the {@link GLObject} class and provides and internal data buffer to keep track
 * of the stored data. The {@link BufferType} and {@link Usage} for the OpenGL buffer are also defined.
 * Note, however, that before performing any action with the <code>GLBuffer</code> you must bind it to
 * the OpenGL context using {@link #bind()}, you can also {@link #unbind()} the currently bound buffer.
 * 
 * @author GnosticOccultist
 */
public abstract class GLBuffer extends GLObject {
	
	/**
	 * The array of currently bound GL buffers to the context, normally one for each buffer type.
	 */
	private static final GLBuffer[] CURRENTS = new GLBuffer[BufferType.values().length];
	
	/**
	 * The usage of the buffer.
	 */
	protected Usage usage;
	/**
	 * The format used for the data.
	 */
	protected Format format;
	/**
	 * The data contained in the buffer.
	 */
	protected Buffer data = null;
	/**
	 * Whether the buffer needs to be updated through OpenGL.
	 */
	protected boolean needsUpdate = true;

	/**
	 * Determines if the provided ID correspond to an OpenGL <code>GLBuffer</code>.
	 * 
	 * @param id The ID of the GLObject to check.
	 * @return	 Whether the ID correspond to a GLBuffer.
	 */
	public static boolean valid(int id) {
		return GL30.glIsBuffer(id);
	}
	
	/**
	 * Unbinds the currently bound <code>GLBuffer</code> from the OpenGL context.
	 * <p>
	 * The methods is mainly used for proper cleaning of the OpenGL context or to avoid errors of
	 * misbindings, because it doesn't need to be called before binding a new buffer.
	 * <p>
	 * The method has been set static because it can be called from any <code>GLBuffer</code> instance,
	 * and will only unbind the lastest bind on the <code>OpenGL</code> context matching the provided
	 * {@link BufferType}.
	 * 
	 * @param type The buffer type to unbind from the context (not null).
	 */
	public static void unbind(BufferType type) {
		Validator.nonNull(type, "The buffer type can't be null!");
		
		GL15.glBindBuffer(GLBuffer.getOpenGLType(type), 0);
		CURRENTS[type.ordinal()] = null;
	}
	
	/**
	 * Binds the <code>GLBuffer</code> to the OpenGL context, allowing it to be used or updated. 
	 * <p>
	 * Note that there is only one bound buffer per {@link BufferType}.
	 */
	@OpenGLCall
	public void bind() {
		if(CURRENTS[getType().ordinal()] == this) {
			return;
		}
		
		if(getID() == INVALID_ID) {
			throw new GLException("The " + getClass().getSimpleName() + " isn't created yet!");
		}
		
		GL15.glBindBuffer(getOpenGLType(), getID());
		CURRENTS[getType().ordinal()] = this;
	}
	
	/**
	 * Store the data of the <code>GLBuffer</code> to the GPU using the OpenGL context.
	 * This method should be called internally in {@link #upload()} to update the stored data,
	 * for each implementation of this class.
	 * <p>
	 * Note that the stored data cannot be null.
	 */
	@OpenGLCall
	protected void storeData(boolean newVBO) {
		
		if(data == null) {
			this.needsUpdate = false;
			return;
		}
		
		// Rewind the buffer to prepare for reading.
		this.data.rewind();
		
		if(newVBO) {
			var byteSize = data.capacity() * format.getSizeInByte();
			GL15C.glBufferData(getOpenGLType(), byteSize, getOpenGLUsage());
		}
		
		if(data instanceof FloatBuffer) {
			GL15C.glBufferSubData(getOpenGLType(), 0, (FloatBuffer) data);
		} else if(data instanceof IntBuffer) {
			GL15C.glBufferSubData(getOpenGLType(), 0, (IntBuffer) data);
		} else if(data instanceof ShortBuffer) {
			GL15C.glBufferSubData(getOpenGLType(), 0, (ShortBuffer) data);
		} else if(data instanceof ByteBuffer) {
			GL15C.glBufferSubData(getOpenGLType(), 0, (ByteBuffer) data);
		} else {
			throw new IllegalArgumentException("Can't upload data from buffer type: " + data.getClass().getSimpleName());
		}
		
		this.needsUpdate = false;
	}
	
	/**
	 * Return whether the <code>GLBuffer</code> needs its data buffer uploaded to the 
	 * OpenGL context.
	 * 
	 * @return Whether the data buffer needs to be reuploaded through the OpenGL context.
	 */
	public boolean needsUpdate() {
		return needsUpdate;
	}
	
	/**
	 * Unbinds the currently bound <code>GLBuffer</code> from the OpenGL context.
	 * <p>
	 * This methods is mainly used for proper cleaning of the OpenGL context or to avoid errors of
	 * misbindings, because it doesn't need to be called before binding a new buffer.
	 * Note that it works even if the currently bound buffer isn't the one invoking this method.
	 */
	@OpenGLCall
	public void unbind() {
		unbind(getType());
	}
	
	@Override
	@OpenGLCall
	public void cleanup() {
		unbind();
		
		super.cleanup();
	}
	
	@Override
	protected void restart() {
		this.needsUpdate = true;
		
		super.restart();
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
	
	/**
	 * Return the <code>BufferType</code> of the <code>GLBuffer</code>, it must
	 * be changed when implementing a sub-class.
	 * <p>
	 * By default: {@link BufferType#VERTEX_DATA}.
	 * 
	 * @return The buffer's type.
	 */
	protected BufferType getType() {
		return BufferType.VERTEX_DATA;
	}
	
	/**
	 * Return the <code>OpenGL</code> equivalent buffer type as an int corresponding to the enum
	 * value of the {@link BufferType}.
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
	 * Return the <code>OpenGL</code> equivalent buffer type as an int corresponding to the provided
	 * enum value of the {@link BufferType}.
	 * 
	 * @return The type of buffer (not null).
	 */
	public static int getOpenGLType(BufferType type) {
		Validator.nonNull(type, "The buffer type to convert can't be null!");
		
		switch(type) {
			case VERTEX_DATA:
				return GL15.GL_ARRAY_BUFFER;
			case VERTEX_INDEXING:
				return GL15.GL_ELEMENT_ARRAY_BUFFER;
			default:
				throw new UnsupportedOperationException("Cannot convert the buffer type: " 
						+ type + " to an OpenGL equivalent!");
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
	
	/**
	 * Return the data stored in the <code>GLBuffer</code>.
	 * <p>
	 * Note that this return the raw internal buffer of the <code>GLBuffer</code>
	 * and isn't thread-safe. If you want to access it from multiple thread, use {@link #getReadOnlyData()}.
	 * 
	 * @return The stored data in the buffer.
	 */
	public Buffer getData() {
		return data;
	}
	
	/**
	 * Returns a safe readable-only {@link Buffer} of the content of the <code>GLBuffer</code>.
	 * Since the two buffers' position, limit, and markvalues are independent, the returned duplicate
	 * is safe to read in other thread. 
	 * 
	 * @return A rewound buffer representing the readable-only data of the <code>GLBuffer</code>.
	 */
	public Buffer getReadOnlyData() {
		if(data == null) {
			return null;
		}
		
		Buffer readOnly = null;
		if(data instanceof ByteBuffer) {
			readOnly = ((ByteBuffer) data).asReadOnlyBuffer();
		} else if(data instanceof FloatBuffer) {
			readOnly = ((FloatBuffer) data).asReadOnlyBuffer();
		} else if(data instanceof ShortBuffer) {
			readOnly = ((ShortBuffer) data).asReadOnlyBuffer();
		} else if( data instanceof IntBuffer ) {
			readOnly = ((IntBuffer) data).asReadOnlyBuffer();
        } else {
        	throw new UnsupportedOperationException("Cannot get a readable-only for type: " + data);
        }
		
		// Rewinding before returning the readable-data.
		readOnly.rewind();
		
		return readOnly;
	}
	
	/**
	 * Return the {@link Usage} of the <code>GLBuffer</code>.
	 * 
	 * @return The usage of the buffer.
	 */
	protected Usage getUsage() {
		return usage;
	}
	
	/**
	 * Sets the {@link Usage} of the <code>GLBuffer</code>.
	 * <p>
	 * The provided usage can't be null.
	 * 
	 * @param usage The usage of the buffer.
	 */
	public void setUsage(Usage usage) {
		if(this.usage == usage) {
			return;
		}
		
		Validator.nonNull(usage);
		
		this.usage = usage;
		this.needsUpdate = true;
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
