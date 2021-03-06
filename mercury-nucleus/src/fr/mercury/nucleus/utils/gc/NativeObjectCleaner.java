package fr.mercury.nucleus.utils.gc;

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.Application;
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
	private static Cleaner CLEANER = Cleaner.create();
	/**
	 * The list of cleaning actions to be executed at the end of the frame.
	 */
	private static final List<Runnable> CLEAN_ACTIONS = new ArrayList<>();
	/**
	 * The list of all cleanables.
	 */
	private static final List<Cleanable> CLEANABLES = new ArrayList<>();
	/**
	 * The list of registered native objects.
	 */
	private static final List<NativeObject> NATIVE_OBJECTS = new ArrayList<>();

	/**
	 * Private constructor to inhibit instantiation of <code>NativeObjectCleaner</code>.
	 */
	private NativeObjectCleaner() {}

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
		var name = nativeObj.toString();
		
		NATIVE_OBJECTS.add(nativeObj);
		
		var cleanupTask = nativeObj.onDestroy(id);
		return register(nativeObj, () -> { 
			
			logger.debug("Cleaning up " + name + ".");
			CLEAN_ACTIONS.add(cleanupTask);
		});
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
			logger.debug("Registered " + obj + ".");
			
			if(CLEANER == null) {
				CLEANER = Cleaner.create();
			}
			
			var cleanable = CLEANER.register(obj, cleanAction);
			CLEANABLES.add(cleanable);
			
			return cleanable;
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
	
	/**
	 * Clean all previously registered objects by invoking their cleaning action.
	 * The method should be called on the render thread if it can handle {@link GLObject}.
	 * The method is usually invoked when the {@link Application} is closing.
	 */
	public static void cleanAll() {
		if (CLEANABLES.isEmpty()) {
			return;
		}
		
		synchronized (LOCK) {
			for (var cleanable : CLEANABLES) {
				cleanable.clean();
			}
			
			CLEANABLES.clear();
		}
		
		cleanUnused();
	}
	
	public static void reset() {
		synchronized (LOCK) {
			for(var obj : NATIVE_OBJECTS) {
				System.out.println("Cleaning up " + obj);
				obj.cleanup();
			}
			
			CLEANABLES.clear();
			CLEAN_ACTIONS.clear();
			
			CLEANER = null;
		}
	}
	
	public static void restart() {
		synchronized (LOCK) {
			var copy = new ArrayList<>(NATIVE_OBJECTS);
			NATIVE_OBJECTS.clear();
			
			for(var obj : copy) {
				obj.restart();
				System.out.println("Restart up " + obj);
			}
		}
	}
}
