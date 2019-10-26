package fr.mercury.nucleus.renderer.opengl.vertex;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL30;

import fr.mercury.nucleus.renderer.opengl.GLObject;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>VertexArray</code> is a <code>GLObject</code>, which can contain a list of <code>VertexBuffer</code>
 * associated to an attribute.
 * The attribute can be asssigned to a location and later, be enabled to access the stored data of the <code>VertexBuffer</code>
 * and use it for the rendering process.
 * <p>
 * Before storing any <code>VertexBuffer</code> or creating the attributes, the <code>VertexArray</code> needs to be created
 * and bound to the OpenGL context, marking it as currently usable.
 * 
 * @author GnosticOccultist
 */
public final class VertexArray extends GLObject {
	
	/**
	 * Determines if the provided {@link VertexArray} correspond to an OpenGL 
	 * vertex array object.
	 * 
	 * @param vao The vertex array to validate.
	 * @return	  Whether the vertex array is valid.
	 */
	public static boolean valid(VertexArray vao) {
		return GL30.glIsVertexArray(vao.getID());
	}
	
	/**
	 * Binds the <code>VertexArray</code> to the OpenGL context, allowing it to 
	 * be used or updated. 
	 */
	@OpenGLCall
	public void bind() {
		if(getID() == INVALID_ID) {
			throw new GLException("The vertex array isn't created yet!");
		}
		
		GL30.glBindVertexArray(getID());
	}
	
	/**
	 * Unbinds the currently bound <code>VertexArray</code> from the OpenGL context.
	 * <p>
	 * This methods is mainly used for proper cleaning of the <code>OpenGL</code> context or to avoid 
	 * errors of misbindings, because it doesn't need to be called before binding a vertex array.
	 * <p>
	 * The method has been set static because it can be called from any <code>VertexArray</code> instance,
	 * and will only unbind the lastest bind on the <code>OpenGL</code> context.
	 */
	@OpenGLCall
	public static void unbind() {
		GL30.glBindVertexArray(0);
	}
	
	@Override
	@OpenGLCall
	public void upload() {
		create();
		
		bind();
	}
	
	@Override
	public void cleanup() {
		unbind();
		
		super.cleanup();
	}

	@Override
	@OpenGLCall
	protected Integer acquireID() {
		return GL30.glGenVertexArrays();
	}

	@Override
	@OpenGLCall
	protected Consumer<Integer> deleteAction() {
		return GL30::glDeleteVertexArrays;
	}
}
