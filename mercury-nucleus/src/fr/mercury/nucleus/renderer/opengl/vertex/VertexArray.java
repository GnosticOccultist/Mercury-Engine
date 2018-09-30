package fr.mercury.nucleus.renderer.opengl.vertex;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL30;

import fr.mercury.nucleus.renderer.opengl.GLObject;
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

	@OpenGLCall
	protected void bind() {
		GL30.glBindVertexArray(getID());
	}
	
	@OpenGLCall
	protected void unbind() {
		GL30.glBindVertexArray(0);
	}
	
	@Override
	@OpenGLCall
	public void upload() {
		create();
		
		bind();
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
