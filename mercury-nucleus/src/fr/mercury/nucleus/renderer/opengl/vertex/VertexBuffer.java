package fr.mercury.nucleus.renderer.opengl.vertex;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.GLBuffer;
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
	 * Store the data of the <code>VertexBuffer</code> to the GPU using the OpenGL context.
	 * This method is called internally in {@link #upload()} to update the stored data.
	 * <p>
	 * Note that the stored data cannot be null.
	 */
	@OpenGLCall
	protected void storeData() {
		Validator.nonNull(data, "Can't upload vertex buffer with null data.");
		
		if(data instanceof FloatBuffer) {
			GL15.glBufferData(getOpenGLType(), (FloatBuffer) data, getOpenGLUsage());
		} else if(data instanceof IntBuffer) {
			GL15.glBufferData(getOpenGLType(), (IntBuffer) data, getOpenGLUsage());
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
		IntBuffer buffer = null;
		try {
			buffer = MemoryUtil.memAllocInt(data.length);
			buffer.put(data).flip();
			this.data = buffer;
		} finally {
			if (buffer != null) {
                MemoryUtil.memFree(buffer);
            }
		}
	}
	
	public void storeData(IntBuffer data) {
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
			this.data = buffer;
		} finally {
			if (buffer != null) {
                MemoryUtil.memFree(buffer);
            }
		}
	}
	
	public void storeData(FloatBuffer data) {
		this.data = data;
	}
	
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
