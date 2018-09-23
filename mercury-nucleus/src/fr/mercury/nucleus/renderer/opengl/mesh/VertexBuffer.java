package fr.mercury.nucleus.renderer.opengl.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import fr.mercury.nucleus.renderer.opengl.GLBuffer;
import fr.mercury.nucleus.utils.OpenGLCall;

public class VertexBuffer extends GLBuffer {

	private VertexBufferType vertexBufferType;
	
	public VertexBuffer(VertexBufferType type, Usage usage) {
		this.vertexBufferType = type;
		this.usage = usage;
	}
	
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
	
	@OpenGLCall
	protected void bind() {
		GL15.glBindBuffer(getOpenGLType(), getID());
	}
	
	@OpenGLCall
	protected void storeData() {
		if(data instanceof FloatBuffer) {
			GL15.glBufferData(getOpenGLType(), (FloatBuffer) data, getOpenGLUsage());
		} else if(data instanceof IntBuffer) {
			GL15.glBufferData(getOpenGLType(), (IntBuffer) data, getOpenGLUsage());
		}
	}
	
	@OpenGLCall
	protected void unbind() {
		GL15.glBindBuffer(getOpenGLType(), 0);
	}
	
	@Override
	protected BufferType getType() {
		return vertexBufferType.getBufferType();
	}

	@Override
	public void upload() {
		create();
		
		bind();
		
		storeData();
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
