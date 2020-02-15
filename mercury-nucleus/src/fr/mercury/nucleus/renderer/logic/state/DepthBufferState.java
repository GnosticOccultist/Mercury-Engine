package fr.mercury.nucleus.renderer.logic.state;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.scenegraph.AnimaMundi;

public class DepthBufferState extends RenderState {
	
	/**
	 * Whether the depth buffer is writable.
	 */
	private boolean writable = true;
	/**
	 * The depth comparison function to use when rendering fragments.
	 */
	private DepthFunction function = DepthFunction.LESS;

	/**
	 * Instantiates a new disabled <code>DepthBufferState</code> with writable set to true and function
	 * to {@link DepthFunction#LESS}.
	 * 
	 * @see #reset()
	 */
	public DepthBufferState() {
		reset();
	}
	
	/**
	 * Enable the <code>DepthBufferState</code>. When applied as a render state for an {@link AnimaMundi},
	 * it will enable the depth testing used when rendering fragments using the set {@link DepthFunction}.
	 * 
	 * @see #setFunction(DepthFunction)
	 * @see #write()
	 * @see #mask()
	 */
	@Override
	public DepthBufferState enable() {
		super.enable();
		return this;
	}

	/**
	 * Disable the <code>DepthBufferState</code>. When applied as a render state for an {@link AnimaMundi},
	 * it will disable the depth testing used when rendering fragments.
	 * <p>
	 * Note that the depth buffer state is disabled by default.
	 * 
	 * @see #setFunction(DepthFunction)
	 * @see #write()
	 * @see #mask()
	 */
	@Override
	public DepthBufferState disable() {
		super.disable();
		return this;
	}
	
	public boolean isWritable() {
		return writable;
	}
	
	public DepthFunction function() {
		return function;
	}
	
	public DepthBufferState write() {
		this.writable = true;
		
		setNeedsUpdate(true);
		return this;
	}
	
	public DepthBufferState mask() {
		this.writable = false;
		
		setNeedsUpdate(true);
		return this;
	}
	
	public DepthBufferState setFunction(DepthFunction function) {
		Validator.nonNull(function, "The depth function can't be null!");
		this.function = function;
		
		setNeedsUpdate(true);
		return this;
	}
	
	/**
	 * Returns {@link Type#DEPTH_BUFFER}.
	 * 
	 * @return The depth buffer state type.
	 */
	@Override
	public Type type() {
		return Type.DEPTH_BUFFER;
	}
	
	@Override
	public void reset() {
		this.enabled = false;
		this.writable = true;
		this.function = DepthFunction.LESS;
		
		setNeedsUpdate(true);
	}
	
	/**
	 * <code>DepthFunction</code> is an enumeration of all possible depth comparison function used by the rasterizer.
	 * 
	 * @author GnosticOccultist
	 */
	public enum DepthFunction {
		NEVER,
		ALWAYS,
		EQUAL,
		NOT_EQUAL,
		LESS,
		LESS_OR_EQUAL,
		GREATER,
		GREATER_OR_EQUAL;
	}
}
