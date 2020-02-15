package fr.mercury.nucleus.renderer.logic.state;

import java.util.EnumMap;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Face;
import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>PolygonModeState</code> is an implementation of {@link RenderState} to determine a polygon rasterization mode.
 * For example when drawing a cube shape, <code>OpenGL</code> will draw the edge of said cube and fill its faces. With this state you can choose
 * how each {@link Face} type is drawn.
 * <p>
 * More deep information on this subject can be found for <code>OpenGL</code> in: <a href="https://www.khronos.org/opengl/wiki/GLAPI/glPolygonMode">Polygon Mode</a>
 * 
 * @author GnosticOccultist
 */
public class PolygonModeState extends RenderState {
	
	/**
	 * The polygon modes for each face (default&rarr;FILL).
	 */
	private EnumMap<Face, PolygonMode> polygonModes = new EnumMap<>(Face.class);
	
	/**
	 * Instantiates a new disabled <code>PolygonModeState</code> with all {@link Face} type set to the 
	 * {@link PolygonMode#FILL} polygon mode, which means the interior of each polygon will be rasterized as filled.
	 * 
	 * @see #reset()
	 */
	public PolygonModeState() {
		reset();
	}
	
	/**
	 * Enable the <code>PolygonModeState</code>. When applied as a render state for an {@link AnimaMundi},
	 * it will enable a special rasterization of polygons of the specified {@link Face} using the set {@link PolygonMode}.
	 * 
	 * @see #setPolygonMode(Face, PolygonMode)
	 * @see #setFace(Face)
	 */
	@Override
	public PolygonModeState enable() {
		super.enable();
		return this;
	}

	/**
	 * Disable the <code>PolygonModeState</code>. When applied as a render state for an {@link AnimaMundi},
	 * it will disable the special rasterization of polygons of the specified {@link Face} using the set {@link PolygonMode}.
	 * It means that the interior of each polygon will be filled.
	 * <p>
	 * Note that the polygon mode state is disabled by default.
	 * 
	 * @see #setPolygonMode(Face, PolygonMode)
	 * @see #setFace(Face)
	 */
	@Override
	public PolygonModeState disable() {
		super.disable();
		return this;
	}
	
	/**
	 * Return the {@link PolygonMode} used by the <code>PolygonModeState</code> for the provided 
	 * {@link Face} type to determine how each polygons are rasterized.
	 * 
	 * @param  The face type to get the polygon mode (not null).
	 * @return The polygon mode used for the rasterization of each polygon.
	 */
	public PolygonMode polygonMode(Face face) {
		Validator.nonNull(face, "The face's type can't be null!");
		return polygonModes.getOrDefault(face, PolygonMode.FILL);
	}
	
	/**
	 * Sets the {@link PolygonMode} to use by the <code>PolygonModeState</code> for the rasterization 
	 * of the polygons of the given {@link Face} type.
	 * 
	 * @param face The face type to set the polygon mode (not null).
	 * @param mode The polygon mode used for the rasterization of each polygon (not null).
	 * @return	   The polygon mode state for chaining purposes.
	 */
	public PolygonModeState setPolygonMode(Face face, PolygonMode mode) {
		Validator.nonNull(face, "The face's type can't be null!");
		Validator.nonNull(mode, "The polygon mode can't be null!");
		boolean needsUpdate = polygonModes.get(face) != mode;
		
		// Only update the polygon modes if the requested mode isn't already set for the face.
		if(needsUpdate) {
			if(face == Face.FRONT_AND_BACK) {
				this.polygonModes.put(Face.FRONT, mode);
				this.polygonModes.put(Face.BACK, mode);
			}
			this.polygonModes.put(face, mode);
			
			setNeedsUpdate(true);
		}
		return this;
	}
	
	/**
	 * Returns {@link Type#POLYGON_MODE}.
	 * 
	 * @return The polygon mode state type.
	 */
	@Override
	public Type type() {
		return Type.POLYGON_MODE;
	}
	
	/**
	 * Resets the <code>PolygonModeState</code> to its default state, meaning its disabled with all {@link Face}
	 * type set to {@link PolygonMode#FILL}.
	 */
	@Override
	public void reset() {
		this.enabled = false;
		// Make sure the EnumMap exists cause it may not if the user is chaining constructor and methods.
		if(polygonModes == null) {
			this.polygonModes = new EnumMap<>(Face.class);
		}
		
		this.polygonModes.put(Face.FRONT, PolygonMode.FILL);
		this.polygonModes.put(Face.BACK, PolygonMode.FILL);
		this.polygonModes.put(Face.FRONT_AND_BACK, PolygonMode.FILL);
		
		setNeedsUpdate(true);
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
