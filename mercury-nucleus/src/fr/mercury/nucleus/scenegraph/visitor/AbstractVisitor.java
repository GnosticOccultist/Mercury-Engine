package fr.mercury.nucleus.scenegraph.visitor;

import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>AbstractVisitor</code> is an implementation of a {@link Visitor} to use
 * as an abstraction for every implemented visitor function. 
 * 
 * @author GnosticOccultist
 */
public abstract class AbstractVisitor implements Visitor {
	
	/**
	 * This method can't be overriden, you should implement your visiting 
	 * logic in the {@link #onVisit(AnimaMundi)} method. 
	 */
	@Override
	public final void visit(AnimaMundi anima) {
		onVisit(anima);
	}
	
	/**
	 * Perform the actual visit of the {@link AnimaMundi}.
	 * This is called from {@link #visit(AnimaMundi)}.
	 * 
	 * @param anima The anima-mundi to visit.
	 */
	public abstract void onVisit(AnimaMundi anima);
}
