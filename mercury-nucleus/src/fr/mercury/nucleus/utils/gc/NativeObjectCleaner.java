package fr.mercury.nucleus.utils.gc;

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.alchemy.utilities.logging.LoggerLevel;
import fr.mercury.nucleus.renderer.opengl.GLObject;
import fr.mercury.nucleus.utils.data.Allocator;

/**
 * <code>NativeObjectCleaner</code> uses an internal {@link Cleaner} to handle the references of tracked objects and 
 * clean them when reclaimed by the GC.
 * This class is used internally by the {@link Allocator} to de-allocate native {@link Buffer}.
 * 
 * @author GnosticOccultist
 * 
 * @see Allocator
 * @see NativeObject
 */
public final class NativeObjectCleaner {

	/**
	 * The logger for the GC.
	 */
	protected static final Logger logger = FactoryLogger.getLogger("mercury.gc");
	/**
	 * The reference object to serve as a lock for thread-safety.
	 */
	private static final Object LOCK = new Object();
	/**
	 * The cleaner used to clean objects when references are reclaimed.
	 */
	private static final Cleaner CLEANER = Cleaner.create();
	/**
	 * The list of cleaning actions to be executed at the end of the frame.
	 */
	private static final List<Runnable> CLEAN_ACTIONS = new ArrayList<>();

	static {
		logger.setActive(LoggerLevel.DEBUG, true);
	}

	/**
	 * Private constructor to inhibit instantiation of <code>NativeObjectCleaner</code>.
	 */
	private NativeObjectCleaner() {
	}

	/**
	 * Registers the specified {@link NativeObject} to be referenced and cleaned by
	 * the <code>NativeObjectCleaner</code> when no longer used.
	 * 
	 * @param nativeObj The native object to track (not null).
	 * @return A cleanable to clean the object instantly.
	 * 
	 * @see #register(Object, Runnable)
	 */
	public static Cleanable register(NativeObject nativeObj) {
		var id = nativeObj.getID();
		Runnable cleanupTask = () -> nativeObj.cleanup(id);
		return register(nativeObj, () -> CLEAN_ACTIONS.add(cleanupTask));
	}

	/**
	 * Registers the specified object to be referenced and cleaned, using the provided {@link Runnable}, 
	 * by the <code>NativeObjectCleaner</code> when no longer used.
	 * 
	 * @param obj         The object to track (not null).
	 * @param cleanAction The action to execute when the object is reclaimed by the GC.
	 * @return A cleanable to clean the object instantly.
	 * 
	 * @see #register(NativeObject)
	 */
	public static Cleanable register(Object obj, Runnable cleanAction) {
		synchronized (LOCK) {
			logger.debug("Registered " + obj + "");
			return CLEANER.register(obj, cleanAction);
		}
	}

	/**
	 * Clean the objects marked as unused by the <code>NativeObjectCleaner</code>.
	 * The method should be called on the render thread if it can handle {@link GLObject}.
	 */
	public static void cleanUnused() {
		if (CLEAN_ACTIONS.isEmpty()) {
			return;
		}

		synchronized (LOCK) {
			for (var action : CLEAN_ACTIONS) {
				action.run();
			}
			
			CLEAN_ACTIONS.clear();
		}
	}
}
