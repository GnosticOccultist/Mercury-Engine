package fr.mercury.nucleus.renderer.logic.state;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.Material;

/**
 * <code>PolygonModeState</code> is an implementation of {@link RenderState} to provide information of how a source pixel is blended to 
 * a destination pixel during the fragment pass. This state is particularly used to achieve transparent or translucent {@link Material},
 * where the tint of the transparent/translucent surface is applied with some weight to the surface of an object situated behind. 
 * 
 * @author GnosticOccultist
 */
public final class BlendState extends RenderState {

	/**
	 * The blend function used for the source weighting factor of the blending.
	 */
	private BlendFunction srcFactor = BlendFunction.ONE;
	/**
	 * The blend function used for the destination weighting factor of the blending.
	 */
	private BlendFunction dstFactor = BlendFunction.ZERO;
	
	/**
	 * Instantiates a new disabled <code>BlendState</code> with the source factor being {@link BlendFunction#ONE}
	 * and the destination factor being {@link BlendFunction#ZERO}.
	 * 
	 * @see #reset()
	 */
	public BlendState() {
		reset();
	}
	
	/**
	 * Enable the <code>BlendState</code>. When applied as a render state for an {@link AnimaMundi},
	 * it will enable blending between new and back fragment using the set {@link BlendFunction}.
	 * 
	 * @see #setSRCFactor(BlendFunction)
	 * @see #setDSTFactor(BlendFunction)
	 */
	@Override
	public BlendState enable() {
		super.enable();
		return this;
	}

	/**
	 * Disable the <code>BlendState</code>. When applied as a render state for an {@link AnimaMundi},
	 * it will disable the blending between new and back fragment and will just override the previously set pixel.
	 * <p>
	 * Note that the depth buffer state is disabled by default.
	 * 
	 * @see #setSRCFactor(BlendFunction)
	 * @see #setDSTFactor(BlendFunction)
	 */
	@Override
	public BlendState disable() {
		super.disable();
		return this;
	}
	
	/**
	 * Return the {@link BlendFunction} to use for the source weighting factor of the blending.
	 * 
	 * @return The source blending factor (not null).
	 */
	public BlendFunction srcFactor() {
		return srcFactor;
	}
	
	/**
	 * Return the {@link BlendFunction} to use for the destination weighting factor of the blending.
	 * 
	 * @return The destination blending factor (not null).
	 */
	public BlendFunction dstFactor() {
		return dstFactor;
	}
	
	/**
	 * Sets the {@link BlendFunction} to use for the source weighting factor of the blending.
	 * 
	 * @param srcFactor The source factor to use when blending pixels (not null).
	 * @return			The blend state for chaining purposes (not null).
	 */
	public BlendState setSRCFactor(BlendFunction srcFactor) {
		Validator.nonNull(srcFactor, "The source factor can't be null!");
		this.srcFactor = srcFactor;
		
		setNeedsUpdate(true);
		return this;
	}
	
	/**
	 * Sets the {@link BlendFunction} to use for the destination weighting factor of the blending.
	 * 
	 * @param dstFactor The destination factor to use when blending pixels (not null).
	 * @return			The blend state for chaining purposes (not null).
	 */
	public BlendState setDSTFactor(BlendFunction dstFactor) {
		Validator.nonNull(dstFactor, "The destination factor can't be null!");
		this.dstFactor = dstFactor;
		
		setNeedsUpdate(true);
		return this;
	}
	
	/**
	 * Returns {@link Type#BLEND_STATE}.
	 * 
	 * @return The blend state state type.
	 */
	@Override
	public Type type() {
		return Type.BLEND_STATE;
	}
	
	/**
	 * Resets the <code>BlendState</code> to its default state, meaning its disabled with the source
	 * factor set to {@link BlendFunction#ONE} and the destination factor set to {@link BlendFunction#ZERO}.
	 */
	@Override
	public void reset() {
		this.enabled = false;
		this.srcFactor = BlendFunction.ONE;
		this.dstFactor = BlendFunction.ZERO;
		
		setNeedsUpdate(true);
	}
	
	/**
	 * <code>BlendFunction</code> is an enumeration of all possible weighting factors to use in the blending 
	 * function used by the rasterizer.
	 * 
	 * @author GnosticOccultist
	 */
	public enum BlendFunction {
		/**
		 * The value of the blend function is one.
		 */
		ONE,
		/**
		 * The value of the blend function is zero.
		 */
		ZERO,
		/**
		 * The value of the blend function is set to the source color.
		 */
		SOURCE_COLOR,
		/**
		 * The value of the blend function is set to 1 - source color.
		 */
		ONE_MINUS_SOURCE_COLOR,
		/**
		 * The value of the blend function is set to the source alpha.
		 */
		SOURCE_ALPHA,
		/**
		 * The value of the blend function is set to 1 - source alpha.
		 */
		ONE_MINUS_SOURCE_ALPHA;
	}
}
