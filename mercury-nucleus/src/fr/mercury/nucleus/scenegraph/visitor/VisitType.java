package fr.mercury.nucleus.scenegraph.visitor;

/**
 * <code>VisitType</code> is an enumeration which specifies the type of
 * visit the {@link Visitor} should bend to.
 * <p>
 * <li>By using a DFS (depth-first search), the search will operate as far as possible within the same branch of the tree (in a direct-linear way), 
 * before backtracking to the previously checked parent node and searching for other children.
 * <br>
 * If you want more info, go check: <a>https://en.wikipedia.org/wiki/Depth-first_search</a>.</li>
 * <pre> {@link #PRE_ORDER}, {@link #POST_ORDER}</pre>
 * <p>
 * <li>By using a BFS (breadth-first search), the search will operate at the same depth of the tree (in an horizontal way), 
 * before moving to the next depth level and checking the sub-nodes.
 * <br>
 * If you want more info, go check: <a>https://en.wikipedia.org/wiki/Breadth-first_search</a>.</li>
 * <p>
 * <pre> {@link #DEPTH_LAYER} </pre>
 * 
 * @author GnosticOccultist
 */
public enum VisitType {
    /**
     * The visit applies to all the <code>AnimaMundi</code> within a same branch, similar to {@link VisitType#POST_ORDER}, 
     * but the concerned <code>AnimaMundi</code> is first visited, then its children (DFS).
     */
    PRE_ORDER,
    /**
     * The visit applies to all the <code>AnimaMundi</code> within a same branch, similar to {@link VisitType#PRE_ORDER}, 
     * but the concerned <code>AnimaMundi</code> is visited at last, after its children (DFS).
     */
    POST_ORDER,
    /**
     * The visit applies to all the <code>AnimaMundi</code> present at the same depth, before moving to the next depth 
     * level (BFS).
     */
    DEPTH_LAYER;
}
