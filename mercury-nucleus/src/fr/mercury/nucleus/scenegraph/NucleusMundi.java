package fr.mercury.nucleus.scenegraph;

import fr.alchemy.utilities.ReadOnlyException;
import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.collections.array.ReadOnlyArray;
import fr.mercury.nucleus.math.objects.Transform;
import fr.mercury.nucleus.scenegraph.visitor.DirtyType;
import fr.mercury.nucleus.scenegraph.visitor.VisitType;
import fr.mercury.nucleus.scenegraph.visitor.Visitor;

/**
 * <code>NucleusMundi</code> represents a node ('<i>nucleus</i>') constituting a manifestation of the {@link AnimaMundi}.
 * The node can contain children either another node or a {@link PhysicaMundi} and can own a parent node or be orphan.
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
	private final Array<AnimaMundi> children = Array.ofType(AnimaMundi.class);
	
	/**
	 * Instantiates a new <code>NucleusMundi</code> with the {@link Transform}
	 * set to identity values.
	 */
	public NucleusMundi() {
		super();
	}
	
	/**
	 * Instantiates a new <code>AnimaMundi</code> with the {@link Transform}
	 * set to identity values and the given name.
	 * 
	 * @param name The name of the nucleus-mundi (not null).
	 */
	public NucleusMundi(String name) {
		super(name);
	}
	
	/**
	 * Attach all the provided <code>AnimaMundi</code> to the <code>NucleusMundi</code>.
	 * 
	 * @param children The anima-mundis to attach (each element not null).
	 */
	public void attachAll(AnimaMundi... children) {
		for(AnimaMundi child : children) {
			attach(child);
		}
	}
	
	/**
	 * Attach an <code>AnimaMundi</code> to the <code>NucleusMundi</code>.
	 * 
	 * @param child The anima-mundi to attach (not null).
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
	 * Detach all the provided <code>AnimaMundi</code> from the <code>NucleusMundi</code>.
	 * 
	 * @param children The anima-mundis to detach (each element not null).
	 */
	public void detachAll(AnimaMundi... children) {
		for(AnimaMundi child : children) {
			attach(child);
		}
	}
	
	/**
	 * Detach an <code>AnimaMundi</code> from the <code>NucleusMundi</code>.
	 * 
	 * @param child The anima-mundi to detach (not null).
	 */
	public void detach(AnimaMundi child) {
		Validator.nonNull(child, "The child to detach cannot be null!");
		
		if(child.getParent() == this) {
			children.remove(child);
			child.setParent(null);
		}
	}
	
	@Override
	protected void propagateDown(DirtyType type) {
		super.propagateDown(type);
		
		for(int i = 0; i < size(); i++) {
			var child = children.get(i);
			child.propagateDown(type);
		}
	}
	
	/**
	 * Return a readable-only array containing the children of the <code>NucleusMundi</code>.
	 * To actually modify the array, use {@link #attach(AnimaMundi)} or {@link #detach(AnimaMundi)} 
	 * methods instead.
	 * 
	 * @return A readable-only array with the children of the nucleus-mundi.
	 * 
	 * @throws ReadOnlyException Thrown if the read-only array is being modified.
	 */
	public ReadOnlyArray<AnimaMundi> children() {
		return children.readOnly();
	}
	
	/**
	 * Return the number of {@link AnimaMundi} contained in the <code>NucleusMundi</code>.
	 * 
	 * @return The number of elements.
	 */
	public int size() {
		return children.size();
	}
	
	/**
	 * Return whether the <code>NucleusMundi</code> is an external one, meaning it has no children.
	 * 
	 * @return Whether the node is external.
	 */
	public boolean isLeaf() {
		return children.isEmpty();
	}
	
	/**
	 * Visit the <code>NucleusMundi</code> with the specified {@link Visitor} and {@link VisitType}.
	 * <br>
	 * If the type corresponds to {@link VisitType#PRE_ORDER}, it will first visit the node and then
	 * its children otherwise, if the type equals to {@link VisitType#POST_ORDER}, it will first visit 
	 * each children and then the node itself.
	 */
	@Override
	public void visit(Visitor visitor, VisitType type) {
		Validator.nonNull(type);
		Validator.nonNull(visitor);
		
		if(type.equals(VisitType.PRE_ORDER)) {
			visitor.visit(this);
		}
		
		for(int i = 0; i < size(); i++) {
			AnimaMundi child = children.get(i);
			child.visit(visitor, type);
		}
		
		if(type.equals(VisitType.POST_ORDER)) {
			visitor.visit(this);
		}
	}
}
