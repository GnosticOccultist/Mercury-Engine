package fr.mercury.nucleus.utils.data;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.readable.ReadableVector2f;
import fr.mercury.nucleus.math.readable.ReadableVector3f;

/**
 * <code>BufferUtils</code> is a utility class to help creating, manipulating or populating the NIO {@link Buffer} class
 * with the maths objects provided by Mercury-Engine such as {@link ReadableVector2f} or {@link ReadableVector3f}.
 * 
 * @author GnosticOccultist
 */
public final class BufferUtils {
	
	/**
	 * Populates the provided {@link FloatBuffer} with the given {@link ReadableVector2f} at the current 
	 * position of the buffer.
	 * 
	 * @param buffer The buffer to populate with the vector (not null).
	 * @param vector The vector to populate the buffer with (not null).
	 * @return		 The populated buffer with the vector (not null).
	 */
	public static FloatBuffer populate(FloatBuffer buffer, ReadableVector2f vector) {
		Validator.nonNull(buffer, "The buffer to populate can't be null!");
		Validator.nonNull(vector, "The vector to populate from can't be null!");
		buffer.put(vector.x()).put(vector.y());
		
		return buffer;
	}
	
	/**
	 * Populates the provided {@link FloatBuffer} with the given {@link ReadableVector2f} at the given starting index.
	 * 
	 * @param buffer The buffer to populate with the vector (not null).
	 * @param vector The vector to populate the buffer with (not null).
	 * @param index	 The index at which to start populating the buffer (&ge;0).
	 * @return		 The populated buffer with the vector (not null).
	 */
	public static FloatBuffer populate(FloatBuffer buffer, ReadableVector2f vector, int index) {
		Validator.nonNull(buffer, "The buffer to populate can't be null!");
		Validator.nonNull(vector, "The vector to populate from can't be null!");
		Validator.nonNegative(index, "The index can't be negative!");
		buffer.put(index * 2, vector.x()).put(index * 2 + 1, vector.y());
		
		return buffer;
	}
	
	/**
	 * Populates the provided {@link FloatBuffer} with the given {@link ReadableVector3f} at the current 
	 * position of the buffer.
	 * 
	 * @param buffer The buffer to populate with the vector (not null).
	 * @param vector The vector to populate the buffer with (not null).
	 * @return		 The populated buffer with the vector (not null).
	 */
	public static FloatBuffer populate(FloatBuffer buffer, ReadableVector3f vector) {
		Validator.nonNull(buffer, "The buffer to populate can't be null!");
		Validator.nonNull(vector, "The vector to populate from can't be null!");
		buffer.put(vector.x()).put(vector.y()).put(vector.z());
		
		return buffer;
	}
	
	/**
	 * Populates the provided {@link FloatBuffer} with the given {@link ReadableVector3f} at the given starting index.
	 * 
	 * @param buffer The buffer to populate with the vector (not null).
	 * @param vector The vector to populate the buffer with (not null).
	 * @param index	 The index at which to start populating the buffer (&ge;0).
	 * @return		 The populated buffer with the vector (not null).
	 */
	public static FloatBuffer populate(FloatBuffer buffer, ReadableVector3f vector, int index) {
		Validator.nonNull(buffer, "The buffer to populate can't be null!");
		Validator.nonNull(vector, "The vector to populate from can't be null!");
		Validator.nonNegative(index, "The index can't be negative!");
		buffer.put(index * 3, vector.x()).put(index * 3 + 1, vector.y()).put(index * 3 + 2, vector.z());
		
		return buffer;
	}
	
	/**
	 * Creates a new <code>ByteBuffer</code> by allocating a block of <code>size</code> of bytes memory.
	 * The buffer's byte order is set to {@link ByteOrder#nativeOrder()}.
	 * 
	 * @param size The size in bytes of the allocated buffer (&gt;0).
	 * @return	   A new allocated float buffer of the given size (not null).
	 */
	public static ByteBuffer createByteBuffer(int size) {
		Validator.positive(size, "The size of the buffer to allocate must be strictly positive!");
		ByteBuffer buffer = MemoryUtil.memAlloc(size).order(ByteOrder.nativeOrder());
		buffer.clear();
		return buffer;
	}

	/**
	 * Creates a new <code>ShortBuffer</code> by allocating a block of <code>size</code> * {@link Short#SIZE} of bytes memory.
	 * The buffer's byte order is set to {@link ByteOrder#nativeOrder()}.
	 * 
	 * @param size The size in shorts of the allocated buffer (&gt;0).
	 * @return	   A new allocated float buffer of the given size (not null).
	 */
	public static ShortBuffer createShortBuffer(int size) {
		Validator.positive(size, "The size of the buffer to allocate must be strictly positive!");
		ShortBuffer buffer = MemoryUtil.memAlloc(Short.BYTES * size).order(ByteOrder.nativeOrder()).asShortBuffer();
		buffer.clear();
		return buffer;
	}
	
	/**
	 * Creates a new <code>IntBuffer</code> by allocating a block of <code>size</code> * {@link Integer#SIZE} of bytes memory.
	 * The buffer's byte order is set to {@link ByteOrder#nativeOrder()}.
	 * 
	 * @param size The size in ints of the allocated buffer (&gt;0).
	 * @return	   A new allocated float buffer of the given size (not null).
	 */
	public static IntBuffer createIntBuffer(int size) {
		Validator.positive(size, "The size of the buffer to allocate must be strictly positive!");
		IntBuffer buffer = MemoryUtil.memAlloc(Integer.BYTES * size).order(ByteOrder.nativeOrder()).asIntBuffer();
		buffer.clear();
		return buffer;
	}
	
	/**
	 * Creates a new <code>FloatBuffer</code> by allocating a block of <code>size</code> * {@link Float#SIZE} of bytes memory.
	 * The buffer's byte order is set to {@link ByteOrder#nativeOrder()}.
	 * 
	 * @param size The size in floats of the allocated buffer (&gt;0).
	 * @return	   A new allocated float buffer of the given size (not null).
	 */
	public static FloatBuffer createFloatBuffer(int size) {
		Validator.positive(size, "The size of the buffer to allocate must be strictly positive!");
		FloatBuffer buffer = MemoryUtil.memAlloc(Float.BYTES * size).order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer.clear();
		return buffer;
	}
}
