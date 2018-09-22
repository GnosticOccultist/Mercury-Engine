package fr.mercury.nucleus.renderer.opengl.mesh;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL30;

import fr.mercury.nucleus.renderer.opengl.GLObject;
import fr.mercury.nucleus.utils.OpenGLCall;

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
