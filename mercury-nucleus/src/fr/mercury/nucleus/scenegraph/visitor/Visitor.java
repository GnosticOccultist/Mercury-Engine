package fr.mercury.nucleus.scenegraph.visitor;

import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>Visitor</code> is a functional interface to use as an <i>Depth-first search</i> algorithm
 * for traversing or searching inside the scenegraph structure.
 * <br>
 * The type of visit to perform can be specfied using the enumeration of {@link VisitType} and by calling 
 * {@link AnimaMundi#visit(Visitor, VisitType)}.
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
