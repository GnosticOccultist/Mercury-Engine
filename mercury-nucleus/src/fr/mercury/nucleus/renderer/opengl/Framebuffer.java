package fr.mercury.nucleus.renderer.opengl;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL30;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.OpenGLCall;

public class Framebuffer extends GLObject {

	/**
	 * The framebuffer target to use in the OpenGL context.
	 */
	private FramebufferTarget target;
	
	/**
	 * Instantiates a new <code>Framebuffer</code> to use for the specified {@link FramebufferTarget}
	 * in the OpenGL context.
	 * 
	 * @param target The target of the framebuffer in the context (not null).
	 */
	public Framebuffer(FramebufferTarget target) {
		Validator.nonNull(target, "The framebuffer target can't be null!");
		this.target = target;
	}
	
	/**
	 * Binds the <code>Framebuffer</code> to the OpenGL context, allowing it to 
	 * be used or updated. 
	 */
	@OpenGLCall
	public void bind() {
		if(getID() == INVALID_ID) {
			throw new GLException("The framebuffer isn't created yet!");
		}
		
		GL30.glBindFramebuffer(target.getOpenGLType(), id);
	}
	
	@Override
	@OpenGLCall
	protected void upload() {
		create();
		
		bind();
	}
	
	/**
	 * Return the {@link FramebufferTarget} to use for the <code>Framebuffer</code> in the OpenGL context.
	 * 
	 * @return The target of the framebuffer in the context (not null).
	 */
	public FramebufferTarget getTarget() {
		return target;
	}
	
	/**
	 * Unbinds the currently bound <code>Framebuffer</code> using its defined target from the OpenGL context.
	 * <p>
	 * This methods is mainly used for proper cleaning of the <code>OpenGL</code> context or to avoid 
	 * errors of misbindings, because it doesn't need to be called before binding a framebuffer.
	 * <p>
	 * If you want to specify which target type to unbound from the context use {@link #unbind(FramebufferTarget)} instead.
	 * 
	 * @see #unbind(FramebufferTarget)
	 */
	@OpenGLCall
	public void unbind() {
		unbind(target);
	}
	
	/**
	 * Unbinds the currently bound <code>Framebuffer</code> on the specified {@link FramebufferTarget} from the OpenGL context.
	 * <p>
	 * This methods is mainly used for proper cleaning of the <code>OpenGL</code> context or to avoid 
	 * errors of misbindings, because it doesn't need to be called before binding a framebuffer.
	 * <p>
	 * The method has been set static because it can be called from any <code>Framebuffer</code> instance,
	 * and will only unbind the lastest bind on the <code>OpenGL</code> context.
	 * 
	 * @param target The framebuffer target to unbind (either read, draw or both framebuffer target).
	 * 
	 * @see #unbind()
	 */
	@OpenGLCall
	public static void unbind(FramebufferTarget target) {
		GL30.glBindFramebuffer(target.getOpenGLType(), 0);
	}

	/**
	 * Acquire an ID for the <code>Framebuffer</code> using the OpenGL context.
	 * 
	 * @return A new unique identifier for the framebuffer object (&gt;0).
	 */
	@Override
	@OpenGLCall
	protected Integer acquireID() {
		return GL30.glGenFramebuffers();
	}

	/**
	 * Return the deleting action for the <code>Framebuffer</code>.
	 * 
	 * @return The deleting action to delete the framebuffer from the OpenGL context.
	 */
	@Override
	@OpenGLCall
	protected Consumer<Integer> deleteAction() {
		return GL30::glDeleteFramebuffers;
	}
	
	@Override
	@OpenGLCall
	public Runnable onDestroy(int id) {
		return () -> GL30.glDeleteFramebuffers(id);
	}
	
	/**
	 * <code>FramebufferTarget</code> enumerates all target parameter available when creating, binding/unbinding, or other 
	 * commands with a {@link Framebuffer} inside the OpenGL context. 
	 * It means that a context can have one framebuffer used only for reading purposes and another one for writing bound at the same time.
	 * 
	 * @author GnosticOccultist
	 */
	public enum FramebufferTarget {
		/**
		 * Only perform reading commands to the framebuffer such as reading pixels.
		 */
		READ,
		/**
		 * Only perform writing commands to the framebuffer such as all rendering commands.
		 */
		DRAW,
		/**
		 * Perform both reading and writing commands to the framebuffer.
		 */
		READ_AND_DRAW;
		
		/**
		 * Return the equivalent OpenGL framebuffer target. Either {@link GL30#GL_FRAMEBUFFER}, {@link GL30#GL_READ_FRAMEBUFFER}
		 * or {@link GL30#GL_DRAW_FRAMEBUFFER}.
		 * 
		 * @return The equivalent OpenGL framebuffer target.
		 */
		public int getOpenGLType() {
			switch (this) {
				case READ:
					return GL30.GL_READ_FRAMEBUFFER;
				case DRAW:
					return GL30.GL_DRAW_FRAMEBUFFER;
				case READ_AND_DRAW:
					return GL30.GL_FRAMEBUFFER;
				default:
					throw new IllegalStateException("Unknown OpenGL framebuffer target " + name());
			}
		}
	}
}
