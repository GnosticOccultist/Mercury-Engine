package fr.mercury.nucleus.renderer.opengl.vertex;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

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
	INDEX(BufferType.VERTEX_INDEXING, 1, Format.UNSIGNED_INT),
	/**
	 * Defines an information per specified number of instances (1, 2, 3, ...) or per vertex,
	 * this is defined by calling {@link GL33#glVertexAttribDivisor(int, int)}. 
	 * It should be used whenever you create a new {@link InstancedAttribute}.
	 * <p>
	 * The data can be a matrix, a vector, an integer, a float...
	 */
	INSTANCE_DATA(BufferType.VERTEX_DATA, Format.FLOAT);
	
	private InstancedDataType dataType;
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
	 * Sets the type of instance information. Either a float, vector, matrix...
	 * 
	 * @param dataType The instanced data type to store.
	 * @return		   The instance data type with the size of the information.
	 */
	public VertexBufferType setType(InstancedDataType dataType) {
		this.dataType = dataType;
		this.size = dataType.size;
		return VertexBufferType.INSTANCE_DATA;
	}
	
	/**
	 * @return The instance data type that the instance data store.
	 *		   (float, vector, matrix...)
	 */		   
	public InstancedDataType getDataType() {
		return dataType;
	}
	
	/**
	 * @return The buffer type of the vertex buffer. Either {@link BufferType#VERTEX_DATA} or 
	 * 		   {@link BufferType#VERTEX_INDEXING}
	 */
	public BufferType getBufferType() {
		return bufferType;
	}
	
	/**
	 * @return The equivalent OpenGL buffer type. Either {@link GL15#GL_ARRAY_BUFFER} or
	 * 		   {@link GL15#GL_ELEMENT_ARRAY_BUFFER}.
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
	 * @return The size per component of the <code>VertexBufferType</code>. 
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * @return The format for each values of the <code>VertexBufferType</code>.
	 *		   It's only the recommended format that is used by default if not declared in the <code>VertexBuffer</code>.
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
	
	public enum InstancedDataType {
		/**
		 * The instance data type as a signed byte.
		 */
		BYTE(1, 1),
		/**
		 * The instance data type as a float.
		 */
		FLOAT(1, 4),
		/**
		 * The instance data type as a signed integer.
		 */
		INTEGER(1, 4),
		/**
		 * The instance data type as a signed short.
		 */
		SHORT(1, 2),
		/**
		 * The instance data type as a <code>Vector2f</code>.
		 */
		VECTOR2F(2, 8),
		/**
		 * The instance data type as a <code>Vector3f</code>.
		 */
		VECTOR3F(3, 12),
		/**
		 * The instance data type as a <code>Vector4f</code>.
		 */
		VECTOR4F(4, 16),
		/**
		 * The instance data type as a <code>Matrix3f</code>.
		 */
		MATRIX3F(9, 36),
		/**
		 * The instance data type as a <code>Matrix4f</code>.
		 */
		MATRIX4F(16, 64);
		
		private int size;
		private int sizeBytes;
		
		InstancedDataType(int size, int sizeBytes) {
			this.size = size;
			this.sizeBytes = sizeBytes;
		}
		
		/**
		 * @return The size of the instance data type.
		 */
		public int getSize() {
			return size;
		}
		
		/**
		 * @return The size in bytes of the instance data type.
		 */
		public int getSizeBytes() {
			return sizeBytes;
		}
	}
}
