package fr.mercury.nucleus.scene;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Transform;

/**
 * <code>AnimaMundi</code> is an abstraction layer for the <code>Tree-Data-Structure</code> representing 
 * an element inside a 3D scene in the <code>Mercury-Engine</code>. 
 * <p>
 * The engine provides two types of element: 
 * <li>{@link PhysicaMundi}, which represents a geometric element capable of being rendered on the screen.</li>
 * <li>{@link NucleusMundi}, which represents a node element capable of having one or multiple children (either node or geometric element).</li>
 * <p>
 * This technique allows for recursive traversal over each elements, by searching for the children of each one if they
 * can possess some and process the rendering of some of them.
 * <li><i>Note: A tree hierarchy can't have any cycle and a structure with no nodes is called <b>null</b> or <b>empty</b> tree.</i></li>
 * <p>
 * Each of these anima must have a parent except for the root element of the tree. They are also defined by a {@link Transform} representing 
 * their position/rotation/scale inside the global or local (node) space.
 * 
 * @author GnosticOccultist
 */
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
	 * <p>
	 * The parent cannot be null.
	 * 
	 * @param parent The parent of the anima-mundi.
	 */
	public void setParent(NucleusMundi parent) {
		Validator.nonNull(parent);
		this.parent = parent;
	}
	
	/**
	 * Return whether the <code>AnimaMundi</code> is orphan, 
	 * meaning it has no parent.
	 * 
	 * @return Whether the anima-mundi has no parent.
	 */
	public boolean isOrphan() {
		return parent == null;
	}
	
	/**
	 * Return whether the <code>AnimaMundi</code> has the provided {@link NucleusMundi}
	 * for ancestor (parent, parent's parent, etc.).
	 * 
	 * @param ancestor The nucleus-mundi to test as an ancestor.
	 * 
	 * @return Whether the provided nucleus-mundi is an ancestor of this anima-mundi.
	 */
	public boolean hasAncestor(NucleusMundi ancestor) {
		return parent != null && (parent.equals(ancestor) || parent.hasAncestor(ancestor));
	}
}
