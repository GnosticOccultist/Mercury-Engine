package fr.mercury.nucleus.scenegraph.visitor;

/**
 * <code>VisitType</code> is an enumeration which specifies the type of
 * visit the {@link Visitor} should bend to.
 * 
 * @author GnosticOccultist
 */
public enum VisitType {
	/**
	 * The concerned <code>AnimaMundi</code> is first visited, then its children if any.
	 */
	PRE_ORDER,
	/**
	 * The concerned <code>AnimaMundi</code> is visited at last, after its children.
	 */
	POST_ORDER;
}
