package fr.mercury.nucleus.renderer;

import org.lwjgl.opengl.GL11;

public class Renderer {
	
	private final Camera camera;
	
	public Renderer(Camera camera) {
		this.camera = camera;
	}
	
	public void drawTriangles(int vertices) {
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertices);
	}
	
	public void update() {
		camera.updateViewMatrix();
	}
}
