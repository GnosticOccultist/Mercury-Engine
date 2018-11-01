package fr.mercury.nucleus.renderer.opengl.vertex;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.GLBuffer;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>VertexBuffer</code> is an implementation of the <code>GLBuffer</code>, which
 * contains vertex data (<i>position, normal, texture coordinate, etc</i>) used for rendering a <code>Mesh</code>.
 * <p>
 * Each <code>VertexBuffer</code> can be organized inside a <code>VertexArray</code> and be called as an attributes list.
 * The attribute can then be attached to a specific index and be used during the rendering process. 
 * <p>
 * This process is auto-managed inside the <code>Mesh</code> class when calling {@link #upload()}, so you don't want to 
 * bother about all the <code>GLObject</code> invokations, if you don't know anything about them.
 * 
 * @author GnosticOccultist
 */
public class VertexBuffer extends GLBuffer {
	/**
	 * The vertex buffer type.
	 */
	private VertexBufferType vertexBufferType;
	
	/**
	 * Instantiates a new <code>VertexBuffer</code> with no contained data,
	 * but with the provided <code>VertexBufferType</code> and <code>Usage</code>.
	 * 
	 * @param type  The vertex buffer's type.
	 * @param usage The usage's type.
	 */
	public VertexBuffer(VertexBufferType type, Usage usage) {
		Validator.nonNull(type, "The vertex buffer's type cannot be null!");
		Validator.nonNull(usage, "The vertex buffer's usage cannot be null!");
		
		this.vertexBufferType = type;
		this.usage = usage;
	}
	
	@Override
	public void upload() {
		create();
		
		bind();
		
		storeData();
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
		IntBuffer buffer = null;
		try {
			buffer = MemoryUtil.memAllocInt(data.length);
			buffer.put(data).flip();
			storeData(buffer);
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
	public void storeData(Buffer data) {
		if(data != null && data.isReadOnly()) {
			throw new MercuryException("Stored data inside a VertexBuffer "
					+ "cannot be readable-only!");
		}
		
		this.data = data;
	}
	
	/**
	 * Store the provided float data array to the <code>VertexBuffer</code>.
	 * <p>
	 * Note that the buffer won't be usable until you call {@link #upload()}, to
	 * update the stored value.
	 * 
	 * @param data The data as an float array.
	 */
	public void storeData(float[] data) {
		FloatBuffer buffer = null;
		try {
			buffer = MemoryUtil.memAllocFloat(data.length);
			buffer.put(data).flip();
			storeData(buffer);
		} finally {
			if (buffer != null) {
                MemoryUtil.memFree(buffer);
            }
		}
	}
	
	/**
	 * Return the {@link VertexBufferType} of the <code>VertexBuffer</code>.
	 * 
	 * @return The type of vertex data contained in the vertex buffer.
	 */
	public VertexBufferType getVertexBufferType() {
		return vertexBufferType;
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
