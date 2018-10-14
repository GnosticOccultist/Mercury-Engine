package fr.mercury.nucleus.scene;

import fr.alchemy.utilities.Validator;

public abstract class AnimaMundi {
	
	/**
	 * The parent of the anima-mundi.
	 */
	protected NucleusMundi parent = null;
	
	/**
	 * Return the parent of the <code>AnimaMundi</code>.
	 * 
	 * @return The anima-mundi's parent.
	 */
	public NucleusMundi getParent() {
		return parent;
	}
	
	/**
	 * Sets the parent of the <code>AnimaMundi</code>.
	 * 
	 * @param parent The parent of the anima-mundi.
	 */
	public void setParent(NucleusMundi parent) {
		Validator.nonNull(parent);
		this.parent = parent;
	}
	
	/**
	 * Return whether the <code>AnimaMundi</code> is orphan.
	 * 
	 * @return Whether the anima-mundi has no parent.
	 */
	public boolean isOrphan() {
		return parent == null;
	}
}
