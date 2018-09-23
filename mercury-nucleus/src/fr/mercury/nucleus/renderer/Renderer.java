package fr.mercury.nucleus.renderer;

import org.lwjgl.opengl.GL11;

import fr.mercury.nucleus.utils.OpenGLCall;

public class Renderer {
	
	private final Camera camera;
	
	public Renderer(Camera camera) {
		this.camera = camera;
	}
	
	@OpenGLCall
	public void drawTriangles(int vertices) {
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertices);
	}
	
	@OpenGLCall
	public void drawElements(int vertices) {
		GL11.glDrawElements(GL11.GL_TRIANGLES, vertices, GL11.GL_UNSIGNED_INT, 0);
	}
	
	public void update() {
		camera.updateViewMatrix();
	}
}
