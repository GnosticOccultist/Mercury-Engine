package fr.mercury.nucleus.renderer.logic.state;

import java.util.EnumMap;

public class WireframeState extends RenderState {

	/**
	 * Whether the wireframe mode is activated (default&rarr;false).
	 */
	private boolean enabled = false;
	/**
	 * The polygon modes for each face (default&rarr;FILL).
	 */
	private final EnumMap<Face, PolygonMode> polygonModes = new EnumMap<>(Face.class);
	
	public WireframeState() {
		reset();
	}
	
	@Override
	public WireframeState enable() {
		this.enabled = true;
		return this;
	}

	@Override
	public WireframeState disable() {
		this.enabled = false;
		return this;
	}

	@Override
	public Type type() {
		return Type.WIREFRAME;
	}

	@Override
	public void reset() {
		this.enabled = false;
		this.polygonModes.put(Face.FRONT, PolygonMode.FILL);
		this.polygonModes.put(Face.BACK, PolygonMode.FILL);
		this.polygonModes.put(Face.FRONT_AND_BACK, PolygonMode.FILL);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public WireframeState setPolygonMode(Face face, PolygonMode mode) {
		if(face == Face.FRONT_AND_BACK) {
			this.polygonModes.put(Face.FRONT, mode);
			this.polygonModes.put(Face.BACK, mode);
		}
		this.polygonModes.put(face, mode);
		return this;
	}
	
	public PolygonMode polygonMode(Face face) {
		return polygonModes.getOrDefault(face, PolygonMode.FILL);
	}
	
	/**
	 * <code>PolygonMode</code> is an enumeration of all possible modes for drawing a polygon by filling it, 
	 * drawing its lines or points.
	 * 
	 * @author GnosticOccultist
	 */
	public enum PolygonMode {
		/**
		 * The filling mode will draw each polygon as if its was filled.
		 */
		FILL,
		/**
		 * The line mode will draw each line composing the polygon.
		 */
		LINE,
		/**
		 * The point mode will draw each point composing the polygon.
		 */
		POINT;
	}
}
