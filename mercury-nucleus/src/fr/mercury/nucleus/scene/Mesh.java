package fr.mercury.nucleus.scene;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.Usage;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexArray;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBuffer;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.texture.Texture2D;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>Mesh</code> is an object storing rendering data to use inside a {@link ShaderProgram}.
 * Therefore every visible elements in a scene-graph is using one or multiple meshes.
 * <p>
 * The mesh is described using vertex with informations such as position, texture coordinates, normals,
 * tangents, etc. The mesh also specifies the type of primitive described with the vertices, for example:
 * triangles, points or lines.
 * <p>
 * The mesh can also store indices to lower the amount of passed vertex data during rendering when using
 * duplicate vertex for complex 3D models. Note that this only applies when all the vertices of the mesh 
 * are different, if not the benefits can be null or more space consumption.
 * 
 * @author GnosticOccultist
 */
public class Mesh {
	
	/**
	 * The vertex array which contains the attribute of the buffers.
	 */
	private VertexArray vao;
	/**
	 * The table of the vertex buffer with its associated type.
	 */
	private final Map<VertexBufferType, VertexBuffer> buffers;
	/**
	 * The primitive mode, by default {@link Mode#TRIANGLES}.
	 */
	private Mode mode = Mode.TRIANGLES;
	
	// TODO: Move to a material class.
	public Texture2D texture;
	
	/**
	 * Instantiates a new <code>Mesh</code> with no <code>VertexBuffer</code> set.
	 * The mode is set by default to {@link Mode#TRIANGLES}.
	 * <p>
	 * To setup a buffer for this mesh, use {@link #setupBuffer(VertexBufferType, Usage, float[])}.
	 */
	public Mesh() {
		this.vao = new VertexArray();
		this.buffers = new HashMap<>();
	}
	
	public void bind() {
		vao.bind();
		
		buffers.values().forEach(VertexBuffer::bind);
	}
	
	public void bindBeforeRender() {
		bind();
		
		if(texture != null) {
			texture.bindToUnit(0);
		}
		
		buffers.keySet().forEach(t -> GL20.glEnableVertexAttribArray(t.ordinal()));
	}
	
	public void unbindAfterRender() {
		
		buffers.keySet().forEach(t -> GL20.glDisableVertexAttribArray(t.ordinal()));
		
		if(texture != null) {
			texture.unbind();
		}
		
		unbind();
	}
	
	public void unbind() {
		
		vao.unbind();
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	public void setupBuffer(VertexBufferType type, Usage usage, FloatBuffer data) {
		var vbo = buffers.get(type);
		if(vbo == null) {
			vbo = new VertexBuffer(type, usage);
			vbo.storeData(data);
			buffers.put(type, vbo);
		} else {
			vbo.storeData(data);
		}
	}

	public void setupBuffer(VertexBufferType type, Usage usage, IntBuffer data) {
		var vbo = buffers.get(type);
		if(vbo == null) {
			vbo = new VertexBuffer(type, usage);
			vbo.storeData(data);
			buffers.put(type, vbo);
		} else {
			vbo.storeData(data);
		}
	}
	
	/**
	 * Setup the <code>VertexBuffer</code> for the specified type and usage and store into
	 * it the given array of float values.
	 * <p>
	 * <b>Only one buffer can be set for each {@link VertexBufferType type}</b>, but don't worry this method
	 * automatically update the stored data for the buffer type if it's already set.
	 * <p>
	 * If you want to use the <code>VertexBuffer</code>, you need to upload it to the GPU with the
	 * OpenGL context using {@link #upload()}. Note that this function will upload all the buffers already
	 * setup on this <code>Mesh</code>.
	 * 
	 * @param type	The buffer type.
	 * @param usage The usage for the buffer (how often it will be updated).
	 * @param data  The array of float values to store in the buffer.
	 */
	public void setupBuffer(VertexBufferType type, Usage usage, float[] data) {
		var vbo = buffers.get(type);
		if(vbo == null) {
			vbo = new VertexBuffer(type, usage);
			vbo.storeData(data);
			buffers.put(type, vbo);
		} else {
			vbo.storeData(data);
		}
	}
	
	/**
	 * Setup the <code>VertexBuffer</code> for the specified type and usage and store into
	 * it the given array of integer values.
	 * <p>
	 * <b>Only one buffer can be set for each {@link VertexBufferType type}</b>, but don't worry this method
	 * automatically update the stored data for the buffer type if it's already set.
	 * <p>
	 * If you want to use the <code>VertexBuffer</code>, you need to upload it to the GPU with the
	 * OpenGL context using {@link #upload()}. Note that this function will upload all the buffers already
	 * setup on this <code>Mesh</code>.
	 * 
	 * @param type	The buffer type.
	 * @param usage The usage for the buffer (how often it will be updated).
	 * @param data  The array of integer values to store in the buffer.
	 */
	public void setupBuffer(VertexBufferType type, Usage usage, int[] data) {
		var vbo = buffers.get(type);
		if(vbo == null) {
			vbo = new VertexBuffer(type, usage);
			vbo.storeData(data);
			buffers.put(type, vbo);
		} else {
			vbo.storeData(data);
		}
	}
	
	/**
	 * Upload the <code>Mesh</code> to the GPU using the OpenGL context. It means each correctly 
	 * setup <code>VertexBuffer</code> will be uploaded and defined with an attribute pointer 
	 * to use in a <code>ShaderSource</code>.
	 * After invoking this function, the <code>Mesh</code> can be properly rendered onto the screen
	 * using the currently bound shader (if any).
	 * <p>
	 * If any buffers happen to be modified, you need to call this function again to update the stored
	 * vertex values.
	 */
	@OpenGLCall
	public void upload() {
		vao.upload();
		
		// Sets the vertex attributes and enable it.
		for(VertexBuffer vertexBuffer : buffers.values()) {
			
			vertexBuffer.upload();
			VertexBufferType type = vertexBuffer.getVertexBufferType();
			
			if(!type.equals(VertexBufferType.INDEX)) {
				GL20.glVertexAttribPointer(type.ordinal(), type.getSize(), type.getOpenGLFormat(), false, 0, 0);
				//GL20.glEnableVertexAttribArray(type.ordinal());
			}
		}
	}
	
	/**
	 * Cleanup the <code>Mesh</code> once it isn't needed anymore from the GPU
	 * and the OpenGL context.
	 * <p>
	 * It will cleanup the <code>VertexArray</code> and each <code>VertexBuffer</code>.
	 */
	@OpenGLCall
	public void cleanup() {
		buffers.values().forEach(VertexBuffer::cleanup);
		buffers.clear();
		
		texture.cleanup();
		
		vao.cleanup();
	}
	
	/**
	 * Return the <code>VertexBuffer</code> from the {@link VertexBufferType type}, 
	 * or null if it isn't present.
	 * 
	 * @param type The vertex buffer type.
	 * @return	   The vertex buffer corresponding to the specified type.
	 */
	public VertexBuffer getBuffer(VertexBufferType type) {
		return buffers.get(type);
	}
	
	/**
	 * Return the number of vertices defined in the <code>Mesh</code> using
	 * the indices <code>VertexBuffer</code> or the position <code>VertexBuffer</code>.
	 * <p>
	 * If none of those buffers are present it will return -1.
	 * 
	 * @return The number of vertices or -1 for undetermined count.
	 */
	public int getVertexCount() {
		
		var indices = getBuffer(VertexBufferType.INDEX).getData();
		if(indices != null) {
			return indices.limit();
		}
		
		var vertices = getBuffer(VertexBufferType.POSITION).getData();
		if(vertices != null) {
			return vertices.limit() / VertexBufferType.POSITION.getSize();
		}
		
		return -1;
	}
	
	/**
	 * Return the primitive mode to represent with the vertices.
	 * 
	 * @return The primitive mode.
	 */
	public Mode getMode() {
		return mode;
	}
	
	/**
	 * Sets the primitive mode to represent with the vertices.
	 * <p>
	 * The mode cannot be null.
	 * 
	 * @param mode The primitive mode.
	 */
	public void setMode(Mode mode) {
		Validator.nonNull(mode, "The primitive mode cannot be null!");
		
		this.mode = mode;
	}
	
    /**
     * <code>Mode</code> specifies what kind of OpenGL primitives to render for this <code>Mesh</code>.
     */
    public enum Mode {
    	/**
    	 * A primitive is a single point represented with 1 vertices.
    	 */
    	POINTS(),
    	/**
    	 * A primitive is a segment specified with 2 vertices.
    	 */
    	LINES(),
    	/**
    	 * A primitive is a segment, the first one defined with 2 vertices
    	 * but the next vertices are combined with the previous one to form
    	 * the other lines.
    	 */
    	LINE_STRIP(),
    	/**
    	 * A primitive is a segment defined with 2 vertices excepting the 
    	 * last one which is connected with the first one to form the final
    	 * line.
    	 */
    	LINE_LOOP(),
    	/**
		 * A primitive is a triangle specified with 3 vertices.
		 */
    	TRIANGLES(),
    	/**
    	 * A primitive is a triangle, the first one defined with 3 vertices
    	 * but the next vertices are combined with the two previous one to form
    	 * the other triangles.
    	 */
    	TRIANGLE_STRIP(),
    	/**
    	 * A primitive is a triangle, the first one defined with 3 vertices
    	 * but the next two vertices are combined with the first one to form
    	 * the other triangles.
    	 */
    	TRIANGLE_FAN();
    }
    
    /**
     * Return the equivalent integer value of OpenGL mode for the enumeration {@link Mode}.
     * 
     * @return The OpenGL equivalent as an integer.
     */
    public int toOpenGLMode() {
    	switch(mode) {
    		case POINTS:
    			return GL11.GL_POINT;
			case LINES:
				return GL11.GL_LINES;
			case LINE_STRIP:
				return GL11.GL_LINE_STRIP;
			case LINE_LOOP:
				return GL11.GL_LINE_LOOP;
			case TRIANGLES:
				return GL11.GL_TRIANGLES;
			case TRIANGLE_FAN:
				return GL11.GL_TRIANGLE_FAN;
			case TRIANGLE_STRIP:
				return GL11.GL_TRIANGLE_STRIP;
			default:
				throw new UnsupportedOperationException("Unknown mode: " + mode);
    	}
    }
}
