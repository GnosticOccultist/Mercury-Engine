package fr.mercury.nucleus.renderer.opengl.vertex;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import fr.mercury.nucleus.renderer.opengl.GLBuffer.BufferType;

/**
 * 
 * @author GnosticOccultist
 *
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
	
	private BufferType bufferType;
	private Format format;
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
	
	public int getOpenGLFormat() {
		return getOpenGLFormat(format);
	}
	
	public static int getOpenGLFormat(Format format) {
		switch (format) {
			case FLOAT:
				return GL11.GL_FLOAT;
			case UNSIGNED_INT:
				return GL11.GL_UNSIGNED_INT;
			default:
				throw new IllegalStateException("Unknown format: " + format);
		}
	}
	
	public enum Format {
		
		FLOAT(4),
		
		UNSIGNED_INT(4);
		
		private int byteSize = 0;
		
		Format(int byteSize) {
			this.byteSize = byteSize;
		}
		
		/**
		 * Returns the size in bytes of the format.
		 * 
		 * @return The size in bytes of the format.
		 */
		public int getSizeInByte() {
			return byteSize;
		}	
	}
}
