package fr.mercury.nucleus.renderer.logic.state;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>DepthBufferState</code> is an implementation of {@link RenderState} to determine the usage of the depth buffer.
 * <p>
 * For example when drawing multiple shapes, the rasterizer tests the depth value (z-value) of a fragment against the content of the depth buffer using 
 * the depth test algorithm, in this case the {@link DepthFunction}, to know if the fragment should be drawn or discarded. The depth buffer can 
 * also be set as readable-only to prevent any new z-value to be written, even if the depth test passes, but to have an effect the depth-test must be enabled.
 * The depth buffer stores depth values as 16, 24 or 32 bit floats increasing the precision of the depth test and preventing Z-fighting issues. Nowadays, 
 * most systems uses a 24 bit floats depth buffer.
 * <p>
 * The depth test is by default disabled and its algorithm is set to {@link DepthFunction#LESS} but the depth buffer is writable.
 * <p>
 * More deep information on this subject can be found for <code>OpenGL</code> in: <a href="https://www.khronos.org/opengl/wiki/Depth_Test">Depth Test</a>
 * 
 * @author GnosticOccultist
 */
public final class DepthBufferState extends RenderState {
	
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
	
	/**
	 * Return whether the <code>DepthBufferState</code> is writable, meaning the fragments z values
	 * will be stored in the depth buffer if they pass the depth test exprimed with the {@link DepthFunction}.
	 * <p>
	 * If the <code>DepthBufferState</code> isn't writable then the depth buffer will be readable only but
	 * this has an effect only if {@link #enable()}.
	 * 
	 * @return Whether the depth buffer is writable.
	 */
	public boolean isWritable() {
		return writable;
	}
	
	/**
	 * Return the {@link DepthFunction} used by the <code>DepthBufferState</code> to determine if a fragment
	 * passes the depth test and can be written into the depth buffer.
	 * 
	 * @return The depth function used by the depth buffer state (not null).
	 */
	public DepthFunction function() {
		return function;
	}
	
	/**
	 * Set the <code>DepthBufferState</code> as being writable, meaning the fragments z values
	 * will be stored in the depth buffer if they pass the depth test exprimed with the {@link DepthFunction}.
	 * <p>
	 * If the <code>DepthBufferState</code> isn't writable then the depth buffer will be readable only but
	 * this has an effect only if {@link #enable()}.
	 * 
	 * @return The depth buffer state for chaining purposes (not null).
	 */
	public DepthBufferState write() {
		this.writable = true;
		
		setNeedsUpdate(true);
		return this;
	}
	
	/**
	 * Set the <code>DepthBufferState</code> as being masked or read-only, meaning the fragments z values
	 * will be tested and discarded accordingly to the {@link DepthFunction} but not written to the depth buffer.
	 * <p>
	 * If the <code>DepthBufferState</code> isn't writable then the depth buffer will be readable only but
	 * this has an effect only if {@link #enable()}.
	 * 
	 * @return The depth buffer state for chaining purposes (not null).
	 */
	public DepthBufferState mask() {
		this.writable = false;
		
		setNeedsUpdate(true);
		return this;
	}
	
	/**
	 * Sets the {@link DepthFunction} used by the <code>DepthBufferState</code> to determine if a fragment
	 * passes the depth test and can be written into the depth buffer.
	 * 
	 * @param function The depth function to use (not null).
	 * @return		   The depth buffer state for chaining purposes (not null).
	 */
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
		/**
		 * The depth test never passes.
		 */
		NEVER,
		/**
		 * The depth test always passes.
		 */
		ALWAYS,
		/**
		 * Passes if the fragment's depth value is equal to the stored depth value.
		 */
		EQUAL,
		/**
		 * Passes if the fragment's depth value isn't equal to the stored depth value.
		 */
		NOT_EQUAL,
		/**
		 * Passes if the fragment's depth value is less than the stored depth value.
		 */
		LESS,
		/**
		 * Passes if the fragment's depth value is less than or equal to the stored depth value.
		 */
		LESS_OR_EQUAL,
		/**
		 * Passes if the fragment's depth value is greater than the stored depth value.
		 */
		GREATER,
		/**
		 * Passes if the fragment's depth value is greater than or equal to the stored depth value.
		 */
		GREATER_OR_EQUAL;
	}
}
