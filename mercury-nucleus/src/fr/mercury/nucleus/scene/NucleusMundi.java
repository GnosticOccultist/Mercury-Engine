package fr.mercury.nucleus.scene;

import java.util.ArrayList;
import java.util.List;

import fr.alchemy.utilities.Validator;

/**
 * <code>NucleusMundi</code> represents a node ('<i>nucleus</i>') constituting a manifestation of the <code>AnimaMundi</code>.
 * The node can contain children either another node or a <code>PhysicaMundi</code> and can own a parent node or be orphan.
 * <p>
 * The node is mainly used for translating/rotating/scaling a group of children easily all at once, or for very fast culling of
 * the entire node.
 * 
 * @author GnosticOccultist
 */
public class NucleusMundi extends AnimaMundi {
	
	/**
	 * The children of the nucleus-mundi (either another node or a physica-mundi).
	 */
	private final List<AnimaMundi> children = new ArrayList<>();
	
	/**
	 * Attach an <code>AnimaMundi</code> to the <code>NucleusMundi</code>.
	 * <p>
	 * The anima-mundi cannot be null.
	 * 
	 * @param child The anima-mundi to attach.
	 */
	public void attach(AnimaMundi child) {
		Validator.nonNull(child, "The child to attach cannot be null!");
		
		if(child.getParent() != this && child != this) {
			if (child.getParent() != null) {
				child.getParent().detach(child);
			}
			
			child.setParent(this);
			children.add(child);
		}
	}
	
	/**
	 * Detach an <code>AnimaMundi</code> from the <code>NucleusMundi</code>.
	 * <p>
	 * The anima-mundi cannot be null.
	 * 
	 * @param child The anima-mundi to detach.
	 */
	public void detach(AnimaMundi child) {
		Validator.nonNull(child, "The child to attach cannot be null!");
		
		if(child.getParent() == this) {
			children.remove(child);
			child.setParent(null);
		}
	}
}
