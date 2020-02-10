package fr.mercury.nucleus.renderer.logic.state;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>FaceCullingState</code> is an implementation of {@link RenderState} to determine the apparent facing of the triangle and to discard
 * them based on this parameter.
 * For example when drawing a cube shape, <code>OpenGL</code> will draw outer as well as inner faces of said cube. With this state you can choose
 * which type of {@link Face} is to be discarded but also how to determine if a face is a front or a back face by using the {@link WindingOrder}.
 * <p>
 * More deep information on this subject can be found for <code>OpenGL</code> in: <a href="https://www.khronos.org/opengl/wiki/Face_Culling">Face Culling</a>
 * 
 * @author GnosticOccultist
 */
public final class FaceCullingState extends RenderState {

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
	
	/**
	 * Enable the <code>FaceCullingState</code>. When applied as a render state for an {@link AnimaMundi},
	 * it will enable the culling of the specified {@link Face} using the set {@link WindingOrder}.
	 * 
	 * @see #setWindingOrder(WindingOrder)
	 * @see #setFace(Face)
	 */
	@Override
	public FaceCullingState enable() {
		this.enabled = true;
		return this;
	}

	/**
	 * Disable the <code>FaceCullingState</code>. When applied as a render state for an {@link AnimaMundi},
	 * it will disable the culling of the specified {@link Face} using the set {@link WindingOrder}.
	 * <p>
	 * Note that the face culling state is disabled by default.
	 * 
	 * @see #setWindingOrder(WindingOrder)
	 * @see #setFace(Face)
	 */
	@Override
	public FaceCullingState disable() {
		this.enabled = false;
		return this;
	}
	
	/**
	 * Return whether the <code>FaceCullingState</code> is enabled.
	 * 
	 * @return Whether the face culling state is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Return the {@link WindingOrder} used by the <code>FaceCullingState</code> to determine which 
	 * side is considered to be the front (either clockwise or counter-clockwise).
	 * It is only used when the primitive used for drawing are triangles.
	 * 
	 * @return The winding order used by the culling state.
	 */
	public WindingOrder windingOrder() {
		return windingOrder;
	}
	
	/**
	 * Return the {@link Face} to cull by the <code>FaceCullingState</code> when enabled (either
	 * front face, back face or both).
	 * It is only used when the primitive used for drawing are triangles.
	 * 
	 * @return The face to cull by the culling state.
	 */
	public Face face() {
		return face;
	}
	
	/**
	 * Sets the {@link Face} to cull by the <code>FaceCullingState</code> when enabled (either
	 * front face, back face or both).
	 * It is only used when the primitive used for drawing are triangles.
	 * 
	 * @param face The face to cull by the culling state (not null).
	 * @return	   The face culling state for chaining purposes.
	 */
	public FaceCullingState setFace(Face face) {
		Validator.nonNull(face, "The face to cull can't be null!");
		this.face = face;
		return this;
	}
	
	/**
	 * Sets the {@link WindingOrder} used by the <code>FaceCullingState</code> to determine which 
	 * side is considered to be the front (either clockwise or counter-clockwise).
	 * It is only used when the primitive used for drawing are triangles.
	 * 
	 * @param windingOrder The winding order used by the culling state (not null).
	 * @return	  		   The face culling state for chaining purposes.
	 */
	public FaceCullingState setWindingOrder(WindingOrder windingOrder) {
		Validator.nonNull(windingOrder, "The winding order for face culling can't be null!");
		this.windingOrder = windingOrder;
		return this;
	}
	
	/**
	 * Resets the <code>FaceCullingState</code> to its default state, meaning its disabled with
	 * the face to cull as {@link Face#BACK} and the winding order as {@link WindingOrder#COUNTER_CLOCKWISE}.
	 */
	@Override
	public void reset() {
		this.enabled = false;
		this.face = Face.BACK;
		this.windingOrder = WindingOrder.COUNTER_CLOCKWISE;
	}
	
	/**
	 * Returns {@link Type#FACE_CULLING}.
	 * 
	 * @return The face culling state type.
	 */
	@Override
	public Type type() {
		return Type.FACE_CULLING;
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
	
	/**
	 * <code>WindingOrder</code> is an enumeration of all possible method to determine which side is considered 
	 * to be the front in the {@link FaceCullingState}.
	 * 
	 * @author GnosticOccultist
	 */
	public enum WindingOrder {
		/**
		 * The clockwise winding order will select the 3 vertices of the triangle by rotating clockwise 
		 * around the triangle's center.
		 */
		CLOCKWISE,
		/**
		 * The counter-clockwise winding order will select the 3 vertices of the triangle by rotating 
		 * counter-clockwise around the triangle's center.
		 */
		COUNTER_CLOCKWISE;
	}
}
