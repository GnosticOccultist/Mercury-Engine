package fr.mercury.nucleus.scenegraph.visitor;

import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>Visitor</code> is a functional interface to use as an <i>Depth-first search</i> algorithm
 * for traversing or searching inside the scenegraph structure.
 * <p>
 * The search will operate as far as possible within the same branch of the tree (in a direct-linear way), 
 * before backtracking to the previously checked parent node and searching for other children.
 * <br>
 * If you want more info, go check: <a>https://en.wikipedia.org/wiki/Depth-first_search</a>.
 * 
 * @author GnosticOccultist
 */
@FunctionalInterface
public interface Visitor {
	
	/**
	 * Visit the specified {@link AnimaMundi}.
	 * The method should always be called when invoking {@link AnimaMundi#visit(Visitor, VisitType)}.
	 * 
	 * @param anima The anima-mundi to visit.
	 */
	void visit(AnimaMundi anima);
}
