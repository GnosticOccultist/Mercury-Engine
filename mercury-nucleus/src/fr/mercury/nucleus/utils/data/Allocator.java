package fr.mercury.nucleus.utils.data;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.function.Consumer;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.utils.gc.NativeObjectCleaner;

/**
 * <code>Allocator</code> is a utility class to allocate memory using direct or heap {@link Buffer}. 
 * It also handles deallocation of native memory buffers when no longer used.
 * 
 * @author GnosticOccultist
 */
public final class Allocator {

	/**
	 * The logger for the allocator.
	 */
	protected static final Logger logger = FactoryLogger.getLogger("mercury.alloc");
	/**
	 * The default allocation type to use (native heap).
	 */
	private static final Type DEFAULT_ALLOC_TYPE = Type.NATIVE_HEAP;

	/**
	 * Private constructor to inhibit instantiation of <code>Allocator</code>.
	 */
	private Allocator() {}

	/**
	 * Allocates a new {@link ByteBuffer} of the provided size using the
	 * {@link #DEFAULT_ALLOC_TYPE}.
	 * 
	 * @param size The size of the buffer in bytes (&gt;0).
	 * @return 	   A new allocated buffer of the specified size.
	 */
	public static ByteBuffer alloc(int size) {
		return alloc(size, DEFAULT_ALLOC_TYPE);
	}
	
	/**
	 * Allocates a new {@link ByteBuffer} of the provided size and of the given
	 * allocation {@link Type}.
	 * <p>
	 * If the type used is {@link Type#NATIVE_STACK}, the {@link #stackPop()} method
	 * must be called once the buffer is no longer used.
	 * 
	 * @param size The size of the buffer in bytes (&gt;0).
	 * @param type The type of allocation to use (not null).
	 * @return 	   A new allocated buffer of the specified size.
	 */
	public static ByteBuffer alloc(int size, Type type) {
		Validator.positive(size, "The size of the memory block to allocate must be positive!");
		Validator.nonNull(type, "The allocation type can't be null!");

		ByteBuffer buffer = null;
		switch (type) {
			case JAVA_HEAP:
				buffer = ByteBuffer.allocate(size);
				break;
			case DIRECT:
				buffer = BufferUtils.createByteBuffer(size);
				break;
			case NATIVE_HEAP:
				buffer = MemoryUtil.memAlloc(size);
				register(buffer);
				break;
			case NATIVE_STACK:
				MemoryStack stack = MemoryStack.stackPush();
				buffer = stack.calloc(size);
				break;
		}

		assert buffer != null;
		return buffer;
	}
	
	/**
	 * Allocates a new {@link ShortBuffer} of the provided size using the
	 * {@link #DEFAULT_ALLOC_TYPE}.
	 * 
	 * @param size The size of the buffer in shorts (&gt;0).
	 * @return 	   A new allocated buffer of the specified size.
	 */
	public static ShortBuffer allocShort(int size) {
		return allocShort(size, DEFAULT_ALLOC_TYPE);
	}

	/**
	 * Allocates a new {@link ShortBuffer} of the provided size and of the given
	 * allocation {@link Type}.
	 * <p>
	 * If the type used is {@link Type#NATIVE_STACK}, the {@link #stackPop()} method
	 * must be called once the buffer is no longer used.
	 * 
	 * @param size The size of the buffer in shorts (&gt;0).
	 * @param type The type of allocation to use (not null).
	 * @return	   A new allocated buffer of the specified size.
	 */
	public static ShortBuffer allocShort(int size, Type type) {
		Validator.positive(size, "The size of the memory block to allocate must be positive!");
		Validator.nonNull(type, "The allocation type can't be null!");

		ShortBuffer buffer = null;
		switch (type) {
			case JAVA_HEAP:
				buffer = ShortBuffer.allocate(size);
				break;
			case DIRECT:
				buffer = BufferUtils.createShortBuffer(size);
				break;
			case NATIVE_HEAP:
				buffer = MemoryUtil.memAllocShort(size);
				register(buffer);
				break;
			case NATIVE_STACK:
				MemoryStack stack = MemoryStack.stackPush();
				buffer = stack.callocShort(size);
				break;
		}

		assert buffer != null;
		return buffer;
	}
	
	/**
	 * Allocates a new {@link IntBuffer} of the provided size using the
	 * {@link #DEFAULT_ALLOC_TYPE}.
	 * 
	 * @param size The size of the buffer in ints (&gt;0).
	 * @return 	   A new allocated buffer of the specified size.
	 */
	public static IntBuffer allocInt(int size) {
		return allocInt(size, DEFAULT_ALLOC_TYPE);
	}

	/**
	 * Allocates a new {@link IntBuffer} of the provided size and of the given
	 * allocation {@link Type}.
	 * <p>
	 * If the type used is {@link Type#NATIVE_STACK}, the {@link #stackPop()} method
	 * must be called once the buffer is no longer used.
	 * 
	 * @param size The size of the buffer in ints (&gt;0).
	 * @param type The type of allocation to use (not null).
	 * @return 	   A new allocated buffer of the specified size.
	 */
	public static IntBuffer allocInt(int size, Type type) {
		Validator.positive(size, "The size of the memory block to allocate must be positive!");
		Validator.nonNull(type, "The allocation type can't be null!");

		IntBuffer buffer = null;
		switch (type) {
			case JAVA_HEAP:
				buffer = IntBuffer.allocate(size);
				break;
			case DIRECT:
				buffer = BufferUtils.createIntBuffer(size);
				break;
			case NATIVE_HEAP:
				buffer = MemoryUtil.memAllocInt(size);
				register(buffer);
				break;
			case NATIVE_STACK:
				MemoryStack stack = MemoryStack.stackPush();
				buffer = stack.callocInt(size);
				break;
		}

		assert buffer != null;
		return buffer;
	}

	/**
	 * Allocates a new {@link FloatBuffer} of the provided size using the
	 * {@link #DEFAULT_ALLOC_TYPE}.
	 * 
	 * @param size The size of the buffer in floats (&gt;0).
	 * @return 	   A new allocated buffer of the specified size.
	 */
	public static FloatBuffer allocFloat(int size) {
		return allocFloat(size, DEFAULT_ALLOC_TYPE);
	}

	/**
	 * Allocates a new {@link FloatBuffer} of the provided size and of the given
	 * allocation {@link Type}.
	 * <p>
	 * If the type used is {@link Type#NATIVE_STACK}, the {@link #stackPop()} method
	 * must be called once the buffer is no longer used in the same {@link Thread}.
	 * 
	 * @param size The size of the buffer in floats (&gt;0).
	 * @param type The type of allocation to use (not null).
	 * @return 	   A new allocated buffer of the specified size.
	 */
	public static FloatBuffer allocFloat(int size, Type type) {
		Validator.positive(size, "The size of the memory block to allocate must be positive!");
		Validator.nonNull(type, "The allocation type can't be null!");

		FloatBuffer buffer = null;
		switch (type) {
			case JAVA_HEAP:
				buffer = FloatBuffer.allocate(size);
				break;
			case DIRECT:
				buffer = BufferUtils.createFloatBuffer(size);
				break;
			case NATIVE_HEAP:
				buffer = MemoryUtil.memAllocFloat(size);
				register(buffer);
				break;
			case NATIVE_STACK:
				MemoryStack stack = MemoryStack.stackPush();
				buffer = stack.callocFloat(size);
				break;
		}

		assert buffer != null;
		return buffer;
	}

	/**
	 * Allocates a new {@link LongBuffer} of the provided size and of the given
	 * allocation {@link Type}.
	 * <p>
	 * If the type used is {@link Type#NATIVE_STACK}, the {@link #stackPop()} method
	 * must be called once the buffer is no longer used in the same {@link Thread}.
	 * 
	 * @param size The size of the buffer in longs (&gt;0).
	 * @param type The type of allocation to use (not null).
	 * @return 	   A new allocated buffer of the specified size.
	 */
	public static LongBuffer allocLong(int size, Type type) {
		Validator.positive(size, "The size of the memory block to allocate must be positive!");
		Validator.nonNull(type, "The allocation type can't be null!");

		LongBuffer buffer = null;
		switch (type) {
			case JAVA_HEAP:
				buffer = LongBuffer.allocate(size);
				break;
			case DIRECT:
				buffer = BufferUtils.createLongBuffer(size);
				break;
			case NATIVE_HEAP:
				buffer = MemoryUtil.memAllocLong(size);
				register(buffer);
				break;
			case NATIVE_STACK:
				MemoryStack stack = MemoryStack.stackPush();
				buffer = stack.callocLong(size);
				break;
		}

		assert buffer != null;
		return buffer;
	}

	/**
	 * Allocates a new {@link DoubleBuffer} of the provided size and of the given
	 * allocation {@link Type}.
	 * <p>
	 * If the type used is {@link Type#NATIVE_STACK}, the {@link #stackPop()} method
	 * must be called once the buffer is no longer used in the same {@link Thread}.
	 * 
	 * @param size The size of the buffer in doubles (&gt;0).
	 * @param type The type of allocation to use (not null).
	 * @return 	   A new allocated buffer of the specified size.
	 */
	public static DoubleBuffer allocDouble(int size, Type type) {
		Validator.positive(size, "The size of the memory block to allocate must be positive!");
		Validator.nonNull(type, "The allocation type can't be null!");

		DoubleBuffer buffer = null;
		switch (type) {
			case JAVA_HEAP:
				buffer = DoubleBuffer.allocate(size);
				break;
			case DIRECT:
				buffer = BufferUtils.createDoubleBuffer(size);
				break;
			case NATIVE_HEAP:
				buffer = MemoryUtil.memAllocDouble(size);
				register(buffer);
				break;
			case NATIVE_STACK:
				MemoryStack stack = MemoryStack.stackPush();
				buffer = stack.callocDouble(size);
				break;
		}

		assert buffer != null;
		return buffer;
	}

	/**
	 * Performs safely the provided action using a pushed {@link MemoryStack}.
	 * <p>
	 * Once the action has been executed the stack is popped.
	 * 
	 * @param stackAction The action to perform using the pushed stack (not null).
	 */
	public static void stackSafe(Consumer<MemoryStack> stackAction) {
		Validator.nonNull(stackAction, "The stack action can't be null!");

		int count = stackFrameIndex();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			stackAction.accept(stack);
		}

		assert stackFrameIndex() == count;
	}

	/**
	 * Pop the lastly pushed stack in the current {@link Thread}.
	 * 
	 * @see #stackSafe(Consumer)
	 */
	public static void stackPop() {
		MemoryStack.stackPop();
	}

	/**
	 * Register a newly allocated native {@link Buffer} to the
	 * {@link NativeObjectCleaner} to be de-allocated when no longer used.
	 * 
	 * @param buffer The native buffer to register (not null).
	 */
	private static void register(Buffer buffer) {
		long address = MemoryUtil.memAddress(buffer);
		NativeObjectCleaner.register(buffer, () -> {

			logger.debug("De-allocated native buffer with address " + address);
			MemoryUtil.nmemFree(address);
		});
	}

	/**
	 * Return the current number of pushed stacks through {@link MemoryStack}.
	 * 
	 * @return The count of push calls (&ge;0).
	 */
	public static int stackFrameIndex() {
		return MemoryStack.stackGet().getFrameIndex();
	}

	/**
	 * <code>Type</code> enumerates the different type of memory allocation.
	 * 
	 * @author GnosticOccultist
	 */
	public enum Type {
		/**
		 * Allocation using Java heap.
		 */
		JAVA_HEAP,
		/**
		 * Direct allocation.
		 */
		DIRECT,
		/**
		 * Allocation using native heap.
		 */
		NATIVE_HEAP,
		/**
		 * Allocation using native stack.
		 */
		NATIVE_STACK;
	}
}
