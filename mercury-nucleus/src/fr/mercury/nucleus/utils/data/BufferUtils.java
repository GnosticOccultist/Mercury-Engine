package fr.mercury.nucleus.utils.data;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Vector2f;
import fr.mercury.nucleus.math.objects.Vector3f;
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
	 * Private constructor to inhibit instantiation of <code>BufferUtils</code>.
	 */
	private BufferUtils() {}
	
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
	 * Reads the vector in the given {@link FloatBuffer} at the specified starting index and return the
	 * provided {@link Vector2f} filled with the data.
	 * 
	 * @param buffer The float buffer to read from (not null).
	 * @param store  The vector to potentially store the result in.
	 * @param index  The starting index to read from (&ge;0).
	 * @return		 The store vector or a new instance one filled with the data (not null).
	 */
	public static Vector2f read(FloatBuffer buffer, Vector2f store, int index) {
		Validator.nonNull(buffer, "The buffer to read from can't be null!");
		Validator.nonNegative(index, "The index to start reading can't be negative");
		Vector2f result = store == null ? new Vector2f() : store;
		
		result.x = buffer.get(index + 0);
		result.y = buffer.get(index + 1);
		return result;
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
	 * Reads the vector in the given {@link FloatBuffer} at the specified starting index and return the
	 * provided {@link Vector3f} filled with the data.
	 * 
	 * @param buffer The float buffer to read from (not null).
	 * @param store  The vector to potentially store the result in.
	 * @param index  The starting index to read from (&ge;0).
	 * @return		 The store vector or a new instance one filled with the data (not null).
	 */
	public static Vector3f read(FloatBuffer buffer, Vector3f store, int index) {
		Validator.nonNull(buffer, "The buffer to read from can't be null!");
		Validator.nonNegative(index, "The index to start reading can't be negative");
		Vector3f result = store == null ? new Vector3f() : store;
		
		result.x = buffer.get(index + 0);
		result.y = buffer.get(index + 1);
		result.z = buffer.get(index + 2);
		return result;
	}
	
	/**
	 * Writes the ints from the provided source {@link IntBuffer} into the given {@link Buffer}.
	 * <p>
	 * The method is using a relative put method.
	 * 
	 * @param buffer The buffer to write to (not null).
	 * @param source The source buffer to read the ints from (not null).
	 * @return		 The buffer with the new data written (not null).
	 */
	public static Buffer put(Buffer buffer, IntBuffer source) {
		Validator.nonNull(buffer, "The buffer to write to can't be null!");
		Validator.nonNull(source, "The buffer to read from can't be null!");
		
		int n = source.remaining();
		for(int i = 0; i < n; i++) {
			if(buffer instanceof ByteBuffer) {
				((ByteBuffer) buffer).put((byte) source.get());
			} else if(buffer instanceof ShortBuffer) {
				((ShortBuffer) buffer).put((short) source.get());
			} else if(buffer instanceof IntBuffer) {
				((IntBuffer) buffer).put(source);
			} else if(buffer instanceof FloatBuffer) {
				((FloatBuffer) buffer).put((float) source.get());
			} else if(buffer instanceof LongBuffer) {
				((LongBuffer) buffer).put((long) source.get());
			} else if(buffer instanceof DoubleBuffer) {
				((DoubleBuffer) buffer).put((double) source.get());
			}
		}
		
		return buffer;
	}
	
	/**
	 * Creates a new <code>Buffer</code> by allocating a block of <code>size</code> of bytes memory.
	 * The buffer's byte order is set to {@link ByteOrder#nativeOrder()}.
	 * <p>
	 * The type of the buffer depends on the provided max index value, for a value less than 256, then a {@link ByteBuffer} is created,
	 * if the value is less than 65536 then a {@link ShortBuffer} is created, otherwise an {@link IntBuffer} is created and returned.
	 * 
	 * @param size 	   The size in bytes of the allocated buffer (&gt;0).
	 * @param maxIndex The maximum index value to store in the buffer (&gt;0).
	 * @return	   	   A new allocated buffer of the given size, the type depending on the max index (not null).
	 */
	public static Buffer createIndicesBuffer(int size, int maxIndex) {
		Validator.positive(size, "The size of the buffer to allocate must be strictly positive!");
		Validator.positive(maxIndex, "The maximum index of the buffer to allocate must be strictly positive!");
		if(maxIndex < 256) {
			return createByteBuffer(size);
		} else if(maxIndex < 65536) {
			return createShortBuffer(size);
		}
		return createIntBuffer(size);
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
		ByteBuffer buffer = Allocator.alloc(size);
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
		ShortBuffer buffer = Allocator.allocShort(size);
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
		IntBuffer buffer = Allocator.allocInt(size);
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
		FloatBuffer buffer = Allocator.allocFloat(size);
		buffer.clear();
		return buffer;
	}
}
