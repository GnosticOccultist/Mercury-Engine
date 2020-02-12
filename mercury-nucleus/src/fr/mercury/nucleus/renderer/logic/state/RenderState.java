package fr.mercury.nucleus.renderer.logic.state;

public abstract class RenderState {
	
	public abstract RenderState enable();
	
	public abstract RenderState disable();
	
	public abstract Type type();
	
	public abstract void reset();
	
	public enum Type {
		
		FACE_CULLING,
		WIREFRAME;
	}
	
	/**
	 * <code>Face</code> is an enumeration of all possible faces to be culled by the {@link FaceCullingState}.
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
