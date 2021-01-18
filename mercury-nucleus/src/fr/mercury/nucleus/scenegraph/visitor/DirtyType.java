package fr.mercury.nucleus.scenegraph.visitor;

import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>DirtyType</code> is an enumeration which contains the types of update
 * that can occur on an {@link AnimaMundi}.
 * 
 * @author GnosticOccultist
 */
public enum DirtyType {
    /**
     * Flag to notify the {@link AnimaMundi} about a dirty transform.
     */
    TRANSFORM,
    /**
     * Flag to notify the {@link AnimaMundi} about some dirty render states.
     */
    RENDER_STATE;
}
