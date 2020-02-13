package fr.mercury.nucleus.renderer.opengl.vertex;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.BufferType;

/**
 * <code>VertexBufferType</code> is an enumeration defining the various type of vertex data
 * a {@link VertexBuffer} can hold.
 * 
 * @author GnosticOccultist
 */
public enum VertexBufferType {
	
	/**
	 * Defines the position of the vertex with 3 floats component.
	 */
	POSITION(BufferType.VERTEX_DATA, 3, Format.FLOAT),
	/**
	 * Defines the texture coordinates for the vertex with 2 floats component.
	 */
	TEX_COORD(BufferType.VERTEX_DATA, 2, Format.FLOAT),
	/**
	 * Defines the normal vector for the vertex with 3 floats component.
	 * It is expected to be normalized as it express a direction.
	 */
	NORMAL(BufferType.VERTEX_DATA, 3, Format.FLOAT),
	/**
	 * Defines the tangent vector for the vertex with 3 floats component.
	 * It is expected to be normalized as it express a direction.
	 */
	TANGENT(BufferType.VERTEX_DATA, 3, Format.FLOAT),
	/**
	 * Defines the bitangent/binormal vector for the vertex with 3 floats component.
	 * It is expected to be normalized as it express a direction.
	 * <p>
	 * <b> IMPORTANT: This is optional since it can be easily computed with the normal and tangent vectors.</b>  
	 */
	BITANGENT(BufferType.VERTEX_DATA, 3, Format.FLOAT),
	/**
	 * Defines the weight for the joint with 4 floats component.
	 * This is used for an animated mesh.
	 */
	WEIGHT(BufferType.VERTEX_DATA, 4, Format.FLOAT),
	/**
	 * Defines the index for the joint with 4 floats component.
	 * This is used for an animated mesh.
	 */
	JOINT_INDEX(BufferType.VERTEX_DATA, 4, Format.FLOAT),
	/**
	 * Defines the index for constructing the vertices with 1 uint component.
	 */
	INDEX(BufferType.VERTEX_INDEXING, 1, Format.UNSIGNED_INT);
	
	/**
	 * The buffer type to use either {@link BufferType#VERTEX_DATA} or 
	 * {@link BufferType#VERTEX_INDEXING}.
	 */
	private BufferType bufferType;
	/**
	 * The preferred format for the type.
	 */
	private Format format;
	/**
	 * The size per component, for example 3 for a normal.
	 */
	private int size;
	
	VertexBufferType(BufferType bufferType, int size, Format format) {
		this.bufferType = bufferType;
		this.size = size;
		this.format = format;
	}
	
	VertexBufferType(BufferType bufferType, Format format) {
		this.bufferType = bufferType;
		this.format = format;
	}
	
	/**
	 * Return the buffer type of the vertex buffer. Either {@link BufferType#VERTEX_DATA} or 
	 * {@link BufferType#VERTEX_INDEXING}
	 * 
	 * @return The vertex buffer's type.
	 */
	public BufferType getBufferType() {
		return bufferType;
	}
	
	/**
	 * Return the equivalent OpenGL buffer type. Either {@link GL15#GL_ARRAY_BUFFER} or
	 * {@link GL15#GL_ELEMENT_ARRAY_BUFFER}.
	 * 
	 * @return The equivalent OpenGL buffer type.
	 */
	public int getOpenGLType() {
		switch (bufferType) {
			case VERTEX_DATA:
				return GL15.GL_ARRAY_BUFFER;
			case VERTEX_INDEXING:
				return GL15.GL_ELEMENT_ARRAY_BUFFER;
			default:
				throw new IllegalStateException("Vertex Buffer cannot accept the following buffer type: " + bufferType);
		}
	}
	
	/**
	 * Return the size per component of the <code>VertexBufferType</code>. 
	 * 
	 * @return The size per component.
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Return the format for each values of the <code>VertexBufferType</code>.
	 * It's only the recommended format that is used by default if not declared in the <code>VertexBuffer</code>.
	 * 
	 * @return The preferred format.
	 */
	public Format getPreferredFormat() {
		return format;
	}
	
	/**
	 * Return the OpenGL equivalent format to this <code>VertexBufferType</code>'s {@link Format}.
	 * 
	 * @return The OpenGL equivalent to the preferred format.
	 */
	public int getOpenGLFormat() {
		return getOpenGLFormat(format);
	}
	
	/**
	 * Return the OpenGL equivalent format to the specified {@link Format}.
	 * 
	 * @param format The format to get the OpenGL equivalent one (not null).
	 * @return		 The OpenGL equivalent format.
	 */
	public static int getOpenGLFormat(Format format) {
		Validator.nonNull(format, "The format can't be null!");
		switch (format) {
			case UNSIGNED_BYTE:
				return GL11.GL_UNSIGNED_BYTE;
			case UNSIGNED_SHORT:
				return GL11.GL_UNSIGNED_SHORT;
			case FLOAT:
				return GL11.GL_FLOAT;
			case UNSIGNED_INT:
				return GL11.GL_UNSIGNED_INT;
			default:
				throw new IllegalStateException("Unknown format: " + format);
		}
	}
	
	/**
	 * Return a {@link Format} corresponding to the provided {@link Buffer}.
	 * 
	 * @param format The buffer to get the format from (not null).
	 * @return		 The format matching the buffer's type.
	 */
	public static Format getFormatFromBuffer(Buffer buffer) {
		Validator.nonNull(buffer, "The buffer can't be null!");
		if(buffer instanceof ByteBuffer) {
			return Format.UNSIGNED_BYTE;
		} else if(buffer instanceof ShortBuffer) {
			return Format.UNSIGNED_SHORT;
		} else if(buffer instanceof IntBuffer) {
			return Format.UNSIGNED_INT;
		} else if(buffer instanceof FloatBuffer) {
			return Format.FLOAT;
		} else {
			throw new IllegalStateException("Unknown format for buffer type: " + buffer.getClass());
		}
	}
	
	/**
	 * <code>Format</code> is an enumeration which describes the various format handled
	 * by <code>Mercury-Engine</code> and the graphics API OpenGL.
	 * 
	 * @author Stickxy
	 */
	public enum Format {
		/**
		 * An unsigned 8-bit value (1 bytes).
		 */
		UNSIGNED_BYTE(1),
		/**
		 * An unsigned 16-bit value (2 bytes).
		 */
		UNSIGNED_SHORT(2),
		/**
		 * A 32-bit single-precision floating-point value (4 bytes).
		 */
		FLOAT(4, true),
		/**
		 * An unsigned 32-bit value (4 bytes).
		 */
		UNSIGNED_INT(4);
		
		/**
		 * The size in bytes of the format.
		 */
		private int byteSize = 0;
		/**
		 * Whether the format is a floating-point type.
		 */
		private boolean floatingPoint = false;
	
		Format(int byteSize) {
			this.byteSize = byteSize;
		}
		
		Format(int byteSize, boolean floatingPoint) {
			this.byteSize = byteSize;
			this.floatingPoint = floatingPoint;
		}
		
		/**
		 * Returns the size in bytes of the <code>Format</code>.
		 * 
		 * @return The size in bytes of the format.
		 */
		public int getSizeInByte() {
			return byteSize;
		}
		
		/**
		 * Returns whether the <code>Format</code> is a floating-point type.
		 * If this is <code>true</code> the value can't be normalized.
		 * 
		 * @return Whether the format is a floating-point type.
		 */
		public boolean isFloatingPoint() {
			return floatingPoint;
		}
	}
}
