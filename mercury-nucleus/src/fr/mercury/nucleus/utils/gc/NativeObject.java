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

	/**
	 * Return the ID of the assigned native object, assuming that one is assigned.
	 * 
	 * @return The native identifier (&gt;0).
	 */
	public abstract int getID();

	/**
	 * Cleanup the <code>NativeObject</code> once its associated native object isn't needed anymore.
	 * <p>
	 * This method is generally called automatically by the {@link NativeObjectCleaner}.
	 * 
	 * @param id The identifier of the associated native object (&gt;0).
	 */
	@OpenGLCall
	public abstract void cleanup(int id);

	public abstract Runnable onDestroy();

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getID();
	}

}
