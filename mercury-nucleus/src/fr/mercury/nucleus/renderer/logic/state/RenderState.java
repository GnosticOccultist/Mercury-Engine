package fr.mercury.nucleus.renderer.logic.state;

import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;

/**
 * <code>RenderState</code> is an abstract class to describe a state which affects the rendering of any {@link PhysicaMundi}.
 * It is up to the user to choose the state in which a 3D object should be rendered, for example allowing transparency, culling backfaces, or blending pixels.
 * <p>
 * The render state logic is supported by the scenegraph, meaning a parent render state will be applied to all of its descendants if not overriden locally
 * by a child.
 * 
 * @see AnimaMundi#setRenderState(RenderState)
 * 
 * @author GnosticOccultist
 */
public abstract class RenderState {
	
	/**
	 * Whether the render state needs to be updated through the graphics API.
	 */
	protected boolean needsUpdate = true;
	
	/**
	 * Enable the <code>RenderState</code>. Do note that some implementations can't be enabled.
	 */
	public abstract RenderState enable();
	
	/**
	 * Disable the <code>RenderState</code>. Do note that some implementations can't be disabled.
	 */
	public abstract RenderState disable();
	
	/**
	 * Resets the <code>RenderState</code> to its default state, generally to the initial drawing context
	 * of the graphics API used.
	 */
	public abstract void reset();
	
	/**
	 * Return the {@link Type} of this <code>RenderState</code>.
	 * 
	 * @return The type of render state (not null).
	 */
	public abstract Type type();
	
	/**
	 * Return whether the <code>RenderState</code> needs to be updated through the graphics API,
	 * by modifying its context.
	 * 
	 * @return Whether the render state needs to be updated.
	 */
	public boolean needsUpdate() {
		return needsUpdate;
	}
	
	/**
	 * Sets whether the <code>RenderState</code> needs to be updated through the graphics API,
	 * in order to apply the requested changes.
	 * 
	 * @param update Whether the render state needs to be updated.
	 */
	public void setNeedsUpdate(boolean update) {
		this.needsUpdate = update;
	}
	
	/**
	 * <code>Type</code> is an enumeration of all {@link RenderState} type supported by the <code>Mercury-Engine</code>.
	 * 
	 * @author GnosticOccultist
	 */
	public enum Type {
		/**
		 * The face culling state to specify which type of face should be culled.
		 */
		FACE_CULLING,
		/**
		 * The polygon mode state to select a polygon rasterization mode.
		 */
		POLYGON_MODE;
	}
	
	/**
	 * <code>Face</code> is an enumeration of all possible affected faces by culling or rasterization operations.
	 * 
	 * @author GnosticOccultist
	 */
	public enum Face {
		/**
		 * The front face of a geometry.
		 */
		FRONT,
		/**
		 * The back face of a geometry.
		 */
		BACK,
		/**
		 * The front and back faces of a geometry.
		 */
		FRONT_AND_BACK;
	}
}
