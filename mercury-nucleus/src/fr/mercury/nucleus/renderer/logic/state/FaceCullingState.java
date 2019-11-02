package fr.mercury.nucleus.renderer.logic.state;

public class FaceCullingState extends RenderState {

	/**
	 * Whether the face culling is activated (default&rarr;false).
	 */
	private boolean enabled = false;
	/**
	 * The face to cull triangles from (default&rarr;BACK).
	 */
	private Face face = Face.BACK;
	/**
	 * The winding order to determine which side is considered to be the front, only used
	 * when drawing triangles (default&rarr;COUNTER_CLOCKWISE).
	 */
	private WindingOrder windingOrder = WindingOrder.COUNTER_CLOCKWISE;
	
	@Override
	public FaceCullingState enable() {
		this.enabled = true;
		return this;
	}

	@Override
	public FaceCullingState disable() {
		this.enabled = false;
		return this;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public WindingOrder windingOrder() {
		return windingOrder;
	}
	
	public Face face() {
		return face;
	}
	
	public FaceCullingState setFace(Face face) {
		this.face = face;
		return this;
	}
	
	public FaceCullingState setWindingOrder(WindingOrder windingOrder) {
		this.windingOrder = windingOrder;
		return this;
	}
	
	@Override
	public void reset() {
		this.enabled = false;
		this.face = Face.BACK;
		this.windingOrder = WindingOrder.COUNTER_CLOCKWISE;
	}
	
	@Override
	public Type type() {
		return Type.FACE_CULLING;
	}
	
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
	
	public enum WindingOrder {
		CLOCKWISE,
		COUNTER_CLOCKWISE;
	}
}
