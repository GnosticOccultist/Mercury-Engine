package fr.mercury.nucleus.utils.gc;

import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>NativeObject</code> is an object which can be assigned to a native object described by an identifier. 
 * The associated object must be cleaned when an instance of this class is no longer used.
 * 
 * @author GnosticOccultist
 */
public abstract class NativeObject {

	/**
	 * This method should be called when the <code>NativeObject</code> is assigned to a native object. 
	 * It registers the object to the {@link NativeObjectCleaner} to be later cleaned.
	 * 
	 * @param id The identifier of the native object (&gt;0).
	 */
	protected void onAssigned(int id) {
		NativeObjectCleaner.register(this);
	}
	
	protected void restart() {
		
	}

	/**
	 * Return the ID of the assigned native object, assuming that one is assigned.
	 * 
	 * @return The native identifier (&gt;0).
	 */
	public abstract int getID();

	/**
	 * Cleanup the <code>NativeObject</code> once its associated native object isn't needed anymore.
	 * This method will also reset the state of the native object to be later reused.
	 */
	@OpenGLCall
	public abstract void cleanup();

	/**
	 * Called by the {@link NativeObjectCleaner} when the <code>NativeObject</code> is marked as unused.
	 * <p>
	 * Implementation of this method, should only call destroy method on the native side staticly, not through
	 * the object's instance.
	 * 
	 * @param id The identifier of the native object (&gt;0).
	 * @return	 A runnable action to execute when the object is marked as unused.
	 */
	@OpenGLCall
	public abstract Runnable onDestroy(int id);

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getID();
	}
}
