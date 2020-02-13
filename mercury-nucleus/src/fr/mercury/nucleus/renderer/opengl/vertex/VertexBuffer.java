package fr.mercury.nucleus.renderer.opengl.vertex;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.GLBuffer;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType.Format;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>VertexBuffer</code> is an implementation of the {@link GLBuffer}, which
 * contains vertex data (<i>position, normal, texture coordinate, etc</i>) used for rendering a {@link Mesh}.
 * <p>
 * Each <code>VertexBuffer</code> can be organized inside a {@link VertexArray} and be called as an attributes list.
 * The attribute can then be attached to a specific index and be used during the rendering process. 
 * <p>
 * This process is auto-managed inside the <code>Mesh</code> class when calling {@link #upload()}, so you don't want to 
 * bother about all the <code>GLObject</code> invokations, if you don't know anything about them.
 * 
 * @author GnosticOccultist
 */
public class VertexBuffer extends GLBuffer {
	
	/**
	 * The upper-limit to which short buffer should be preferred instead of an integer buffer to store indices.
	 * This means that if indices count &ge; 65536, it needs to use integer buffer.
	 */
	public static final int USE_SHORT_LIMIT = 65536;
	/**
	 * The vertex buffer type.
	 */
	private VertexBufferType vertexBufferType;
	/**
	 * The format to use for the vertex data, if null it will use the preferred format of {@link VertexBufferType}.
	 */
	private Format format;
	/**
	 * The offset at which the vertex data is situated in a {@link VertexArray}, by default 0.
	 */
	private short offset = 0;
	/**
	 * The amount of bytes between each vertex data, by default 0 &rarr; the data is tightly packed and <code>OpenGL</code>
	 * will compute the stride based on the size per component and the data format used.
	 */
	private short stride = 0;
	/**
	 * Return whether the vertex data should be normalized. Only works for non floating-point type format.
	 */
	private boolean normalized = false;
	
	/**
	 * Instantiates a new <code>VertexBuffer</code> with no contained data, but with 
	 * the provided {@link VertexBufferType} and {@link Usage}.
	 * 
	 * @param type  The vertex buffer's type.
	 * @param usage The usage's type.
	 */
	public VertexBuffer(VertexBufferType type, Usage usage) {
		this(type, usage, type.getPreferredFormat());
	}
	
	/**
	 * Instantiates a new <code>VertexBuffer</code> with no contained data, but with 
	 * the provided {@link VertexBufferType}, {@link Usage} and {@link Format} of the data to store.
	 * 
	 * @param type   The vertex buffer's type (not null).
	 * @param usage  The usage's type (not null).
	 * @param format The format of the data to store (not null).
	 */
	public VertexBuffer(VertexBufferType type, Usage usage, Format format) {
		Validator.nonNull(type, "The vertex buffer's type cannot be null!");
		Validator.nonNull(usage, "The vertex buffer's usage cannot be null!");
		Validator.nonNull(format, "The vertex buffer's format cannot be null!");
		
		this.vertexBufferType = type;
		this.usage = usage;
		this.format = format;
	}
	
	@Override
	public void upload() {
		create();
		
		bind();
		
		if(needsUpdate()) {
			storeData();
		}
	}
	
	/**
	 * Return the <code>VertexBuffer</code> {@link BufferType type}.
	 * It corresponds to {@link VertexBufferType#getBufferType()}.
	 * 
	 * @return The vertex buffer's type.
	 */
	@Override
	protected BufferType getType() {
		return vertexBufferType.getBufferType();
	}
	
	/**
	 * Store the provided integer data array to the <code>VertexBuffer</code>.
	 * <p>
	 * Note that the buffer won't be usable until you call {@link #upload()}, to
	 * update the stored value.
	 * 
	 * @param data The data as an integer array.
	 */
	public void storeData(int[] data) {
		if(format != Format.UNSIGNED_INT) {
			throw new IllegalArgumentException("The format of the vertex buffer "
					+ "can only accept integer data values!");
		}
		
		IntBuffer buffer = null;
		try {
			buffer = MemoryUtil.memAllocInt(data.length);
			buffer.put(data).flip();
			storeDataBuffer(buffer);
		} finally {
			if (buffer != null) {
                MemoryUtil.memFree(buffer);
            }
		}
	}
	
	/**
	 * Store the provided short data array to the <code>VertexBuffer</code>.
	 * <p>
	 * Note that the buffer won't be usable until you call {@link #upload()}, to
	 * update the stored value.
	 * 
	 * @param data The data as a short array.
	 */
	public void storeData(short[] data) {
		if(format != Format.UNSIGNED_SHORT) {
			throw new IllegalArgumentException("The format of the vertex buffer "
					+ "can only accept integer data values!");
		}
		
		ShortBuffer buffer = null;
		try {
			buffer = MemoryUtil.memAllocShort(data.length);
			buffer.put(data).flip();
			storeDataBuffer(buffer);
		} finally {
			if (buffer != null) {
                MemoryUtil.memFree(buffer);
            }
		}
	}
	
	/**
	 * Store the provided float data array to the <code>VertexBuffer</code>.
	 * <p>
	 * Note that the buffer won't be usable until you call {@link #upload()}, to
	 * update the stored value.
	 * 
	 * @param data The data as a float array.
	 */
	public void storeData(float[] data) {
		if(format != Format.FLOAT) {
			throw new IllegalArgumentException("The format of the vertex buffer "
					+ "can only accept float data values!");
		}
		
		FloatBuffer buffer = null;
		try {
			buffer = MemoryUtil.memAllocFloat(data.length);
			buffer.put(data).flip();
			storeDataBuffer(buffer);
		} finally {
			if (buffer != null) {
                MemoryUtil.memFree(buffer);
            }
		}
	}
	
	/**
	 * Sets the provided {@link Buffer} as the data of the <code>VertexBuffer</code>.
	 * <p>
	 * Note that the buffer cannot be {@link Buffer#isReadOnly() readable-only} and won't 
	 * be usable until you call {@link #upload()}, to update the stored value.
	 * 
	 * @param data The buffer storing vertex data.
	 */
	public void storeDataBuffer(Buffer data) {
		if(data.isReadOnly()) {
			throw new MercuryException("Stored data inside a VertexBuffer "
					+ "cannot be readable-only!");
		}
		
		this.data = data;
		this.needsUpdate = true;
	}
	
	/**
	 * Return the {@link VertexBufferType} of the <code>VertexBuffer</code>.
	 * 
	 * @return The type of vertex data contained in the vertex buffer.
	 */
	public VertexBufferType getVertexBufferType() {
		return vertexBufferType;
	}
	
	/**
	 * Return whether the <code>VertexBuffer</code> is an {@link VertexBufferType#INDEX}.
	 * 
	 * @return Whether the vertex buffer contains index data.
	 */
	public boolean isIndexBuffer() {
		return vertexBufferType == VertexBufferType.INDEX;
	}
	
	/**
	 * Return the {@link Format} to use for the <code>VertexBuffer</code>, or null
	 * to default to the preferred format of {@link VertexBufferType}.
	 * 
	 * @return The format to use for the vertex data.
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * Return whether the vertex data of the <code>VertexBuffer</code> should be normalized,
	 * when passing as attributes.
	 * <p>
	 * Note that it only works for non floating-point type {@link Format}.
	 * 
	 * @return Whether the vertex data should be normalized.
	 */
	public boolean isNormalized() {
		return normalized;
	}
	
	/**
	 * Sets whether the vertex data of the <code>VertexBuffer</code> should be normalized,
	 * when passing as attributes.
	 * <p>
	 * Note that it only works for non floating-point type {@link Format}.
	 * 
	 * @param normalized Whether the vertex data should be normalized.
	 */
	public void setNormalized(boolean normalized) {
		if(this.normalized == normalized) {
			return;
		}
		
		this.normalized = normalized;
		this.needsUpdate = true;
	}
	
	/**
	 * Return the stride used by the <code>VertexBuffer</code> in bytes.
	 * 
	 * @return The stride of the buffer in bytes (&ge;0).
	 */
	public short getStride() {
		return stride;
	}
	
	/**
	 * Sets the stride to use for the <code>VertexBuffer</code> in bytes,
	 * to indicate the size in bytes of the data leaved between each components of the vertex data.
	 * By default, the value is set to 0 meaning the data is tightly packed in the buffer and
	 * <code>OpenGL</code> will automatically compute the stride from the size per component and {@link Format}
	 * used for vertex data, i.e. 12 bytes for {@link VertexBufferType#POSITION} 4 bytes per float * 3 float per component.
	 * <p>
	 * The specified value must be greater or equal to 0.
	 * 
	 * @param stride The stride of the buffer in bytes (&ge;0).
	 */
	public void setStride(short stride) {
		if(this.stride == stride) {
			return;
		}
		
		Validator.positive(stride);
		
		this.stride = stride;
		this.needsUpdate = true;
	}
	
	/**
	 * Return the offset used by the <code>VertexBuffer</code> in bytes.
	 * 
	 * @return The offset of the buffer in bytes (&ge;0).
	 */
	public short getOffset() {
		return offset;
	}
	
	/**
	 * Sets the offset to use for the <code>VertexBuffer</code> in bytes,
	 * to indicate the position of the data to sent to the GPU from the start of the buffer.
	 * By default, the value is set to 0 meaning it will be reading the vertex data from
	 * the start of the buffer.
	 * <p>
	 * The specified value must be greater or equal to 0.
	 * 
	 * @param offset The offset of the buffer in bytes (&ge;0).
	 */
	public void setOffset(short offset) {
		if(this.offset == offset) {
			return;
		}
		
		Validator.positive(offset);
		
		this.offset = offset;
		this.needsUpdate = true;
	}
	
	@Override
	@OpenGLCall
	protected Integer acquireID() {
		return GL15.glGenBuffers();
	}

	@Override
	@OpenGLCall
	protected Consumer<Integer> deleteAction() {
		return GL15::glDeleteBuffers;
	}
}
