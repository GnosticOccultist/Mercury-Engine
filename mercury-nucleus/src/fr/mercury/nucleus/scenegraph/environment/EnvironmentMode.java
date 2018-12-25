package fr.mercury.nucleus.scenegraph.environment;

import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>EnvironmentMode</code> is an enumeration defining how an environment property applied
 * to a {@link AnimaMundi element} of the scene-graph should be handled by its ancestor or descendants.
 * 
 * @author GnosticOccultist
 */
public enum EnvironmentMode {
	/**
	 * Always search on the local-defined environment and return null if nothing
	 * was found. 
	 */
	LOCAL_ONLY,
	/**
	 * Always search on the ancestor's environment and then search on the local's one
	 * if the ancestor doesn't have the specific environment registered.
	 */
	ANCESTOR_PRIORITY,
	/**
	 * Always search on the local's environment and then search on the ancestor's one
	 * if the caller doesn't have the specific environment registered.
	 */
	LOCAL_PRIORITY;
}
