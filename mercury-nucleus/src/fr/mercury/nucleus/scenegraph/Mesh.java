package fr.mercury.nucleus.scenegraph;

import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.Usage;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexArray;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBuffer;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType.Format;
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
	public VertexArray vao;
	/**
	 * The table of the vertex buffer with its associated type.
	 */
	private final Map<String, VertexBuffer> buffers;
	/**
	 * The primitive mode, by default {@link Mode#TRIANGLES}.
	 */
	private Mode mode = Mode.TRIANGLES;
	/**
	 * The count of instances to draw for the mesh.
	 */
	private int instanceCount = 1;
	/**
	 * The count of vertices in the mesh.
	 */
	private int vertexCount = -1;
	
	/**
	 * Instantiates a new <code>Mesh</code> with no {@link VertexBuffer} set.
	 * The mode is set by default to {@link Mode#TRIANGLES}.
	 * <p>
	 * To setup a buffer for this mesh, use {@link #setupBuffer(VertexBufferType, Usage, Buffer)}.
	 * 
	 * @see #setupBuffer(VertexBufferType, Usage, Buffer)
	 * @see #setupIndexBuffer(int[])
	 */
	public Mesh() {
		this.vao = new VertexArray();
		this.buffers = new HashMap<>();
	}
	
	/**
	 * Return whether the <code>Mesh</code> is dirty meaning at least one of its {@link VertexBuffer}
	 * has unuploaded changes to it.
	 * 
	 * @return Wether the mesh is dirty. 
	 */
	public boolean isDirty() {
		return buffers.values().stream()
				.filter(VertexBuffer::needsUpdate)
				.findAny().isPresent();
	}
	
	/**
	 * Binds the <code>Mesh</code> to be used by the <code>OpenGL</code> context,
	 * by binding its {@link VertexArray}.
	 * 
	 * @see VertexArray#bind()
	 */
	public void bind() {
		vao.bind();
	}
	
	/**
	 * Unbinds the <code>Mesh</code> to be no longer used by the <code>OpenGL</code> 
	 * context, by unbinding the current {@link VertexArray}.
	 * 
	 * @see VertexArray#unbind()
	 */
	public void unbind() {
		VertexArray.unbind();
	}
	
	/**
	 * Setup a {@link VertexBuffer} for the specified array of indices for the <code>Mesh</code>.
	 * <p>
	 * <b>Only one index buffer can be set</b>, but don't worry this method automatically update 
	 * the stored data for the buffer type if it's already set.
	 * <p>
	 * If you want to use the <code>VertexBuffer</code>, you need to upload it to the GPU with the
	 * OpenGL context using {@link #upload()}. Note that this function will upload all the buffers already
	 * setup on this <code>Mesh</code>.
	 * 
	 * @param data The array of indices as integer values to store in the buffer.
	 */
	public void setupIndexBuffer(Buffer data) {
		setupBuffer(VertexBufferType.INDEX, Usage.STATIC_DRAW, data);
	}
	
	/**
	 * Setup the {@link VertexBuffer} for the specified type and usage and store into
	 * it the given buffer containing vertex data.
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
	 * @param data  The buffer containing the vertex data.
	 */
	public void setupBuffer(VertexBufferType type, Usage usage, Buffer data) {
		var key = type.toString();
		var vbo = buffers.get(key);
		if(vbo == null) {
			vbo = new VertexBuffer(type, usage, VertexBufferType.getFormatFromBuffer(data));
			vbo.storeDataBuffer(data);
			buffers.put(key, vbo);
		} else {
			vbo.storeDataBuffer(data);
		}
		
		if(type == VertexBufferType.POSITION) {
			updateVertexCount();
		}
	}
	
	/**
	 * Setup the {@link VertexBuffer} for the specified type and usage and store into
	 * it the given buffer containing vertex data.
	 * <p>
	 * <b>Only one buffer can be set for each {@link VertexBufferType type}</b>, but don't worry this method
	 * automatically update the stored data for the buffer type if it's already set.
	 * <p>
	 * If you want to use the <code>VertexBuffer</code>, you need to upload it to the GPU with the
	 * OpenGL context using {@link #upload()}. Note that this function will upload all the buffers already
	 * setup on this <code>Mesh</code>.
	 * 
	 * @param size	The size for each vertex data that the buffer will contain.
	 * @param usage The usage for the buffer (how often it will be updated).
	 * @param data  The buffer containing the vertex data.
	 */
	public void setupBuffer(String key, int size, Usage usage, Buffer data) {
		var vbo = buffers.get(key);
		if(vbo == null) {
			vbo = new VertexBuffer(size, usage, VertexBufferType.getFormatFromBuffer(data));
			vbo.storeDataBuffer(data);
			buffers.put(key, vbo);
		} else {
			vbo.storeDataBuffer(data);
		}
	}

	/**
	 * Setup the {@link VertexBuffer} for the specified type and usage and store into
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
		var key = type.toString();
		var vbo = buffers.get(key);
		if(vbo == null) {
			vbo = new VertexBuffer(type, usage);
			vbo.storeData(data);
			buffers.put(key, vbo);
		} else {
			vbo.storeData(data);
		}
		
		if(type == VertexBufferType.POSITION) {
			updateVertexCount();
		}
	}
	
	/**
	 * Setup the {@link VertexBuffer} for the specified type and usage and store into
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
		var key = type.toString();
		var vbo = buffers.get(key);
		if(vbo == null) {
			vbo = new VertexBuffer(type, usage);
			vbo.storeData(data);
			buffers.put(key, vbo);
		} else {
			vbo.storeData(data);
		}
		
		if(type == VertexBufferType.POSITION) {
			updateVertexCount();
		}
	}
	
	/**
	 * Upload the <code>Mesh</code> to the GPU using the OpenGL context. It means each correctly 
	 * setup {@link VertexBuffer} will be uploaded and defined with an attribute pointer 
	 * to use in a {@link ShaderProgram}.
	 * After invoking this function, the <code>Mesh</code> can be properly rendered onto the screen
	 * using the currently bound shader (if any).
	 * <p>
	 * If any buffers happen to be modified, you need to call this function again to update the stored
	 * vertex values.
	 */
	@OpenGLCall
	public void upload() {
		vao.upload();
		
		buffers.values().forEach(VertexBuffer::upload);
	}
	
	/**
	 * Cleanup the <code>Mesh</code> once it isn't needed anymore from the GPU
	 * and the OpenGL context.
	 * <p>
	 * It will cleanup the {@link VertexArray} and each {@link VertexBuffer}.
	 */
	@OpenGLCall
	public void cleanup() {
		buffers.values().forEach(VertexBuffer::cleanup);
		buffers.clear();
		
		vao.cleanup();
	}
	
	/**
	 * Return whether the <code>Mesh</code> as an indices {@link VertexBuffer} present.
	 * <p>
	 * Note that it only check its presence and not its content or if it had been correctly
	 * uploaded to the GPU, this must be guaranteed by the user of the mesh.
	 * 
	 * @return Whether the mesh has an indices buffer setup.
	 */
	public boolean hasIndices() {
		return hasBuffer(VertexBufferType.INDEX);
	}
	
	public boolean hasBuffer(VertexBufferType type) {
		return hasBuffer(type.toString());
	}
	
	public boolean hasBuffer(String key) {
		return buffers.containsKey(key);
	}
	
	/**
	 * Return the {@link VertexBuffer} from the {@link VertexBufferType type}, 
	 * or null if it isn't present.
	 * 
	 * @param type The vertex buffer type.
	 * @return	   The vertex buffer corresponding to the specified type.
	 */
	public VertexBuffer getBuffer(VertexBufferType type) {
		return getBuffer(type.toString());
	}
	
	/**
	 * Return the {@link VertexBuffer} using the provided key, 
	 * or null if it isn't present.
	 * 
	 * @param type The key of the data.
	 * @return	   The vertex buffer corresponding to the specified key.
	 */
	public VertexBuffer getBuffer(String key) {
		return buffers.get(key);
	}
	
	/**
	 * Return the {@link Format} of the index {@link VertexBuffer} of the <code>Mesh</code>, 
	 * or null if it has no indices buffer defined.
	 * 
	 * @return The vertex buffer corresponding to the specified type.
	 */
	public Format getIndicesFormat() {
		return hasIndices() ? getBuffer(VertexBufferType.INDEX).getFormat() : null;
	}
	
	/**
	 * Return the number of vertices defined in the <code>Mesh</code> using
	 * the indices <code>VertexBuffer</code> or the position <code>VertexBuffer</code>.
	 * <p>
	 * If none of those buffers are present it will return -1.
	 * 
	 * @return The number of vertices or -1 for undetermined count.
	 */
	private void updateVertexCount() {
		var vertices = getBuffer(VertexBufferType.POSITION).getData();
		if(vertices != null) {
			var result = vertices.limit() / VertexBufferType.POSITION.getSize();
			this.vertexCount = result;
		}
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public int getElementCount() {
		var result = getBuffer(VertexBufferType.INDEX).getData().limit();
		return result;
	}
	
	/**
	 * Return the {@link Mode} of the primitive to render with the vertices.
	 * 
	 * @return The primitive mode.
	 */
	public Mode getMode() {
		return mode;
	}
	
	/**
	 * Sets the {@link Mode} of the primitive to render with the vertices.
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
	 * Return whether the <code>Mesh</code> is using instanced rendering to be drawn
	 * multiple times in one draw call.
	 * 
	 * @return Whether the mesh is using instanced rendering.
	 */
	public boolean isInstanced() {
		return instanceCount > 1;
	}
	
	/**
	 * Return the number of instance to drawn for the <code>Mesh</code>. If the value
	 * is greater than one, then instanced rendering will be used to draw the mesh.
	 * 
	 * @return The count of instances to draw for the mesh (default&rarr;1). 
	 */
	public int getInstanceCount() {
		return instanceCount;
	}
	
	public void setInstanceCount(int instanceCount) {
		Validator.positive(instanceCount, "The count of instances must be strictly positive!");
		this.instanceCount = instanceCount;
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
