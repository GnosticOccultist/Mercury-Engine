package fr.mercury.nucleus.renderer;

import org.lwjgl.opengl.GL11;

import fr.mercury.nucleus.scene.Mesh;
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
	
	/**
	 * Transfer the <code>VertexBuffer</code> to the bound <code>ShaderProgram</code> as attributes.
	 * The currently bound element array buffer (if any) will determine the amount of data to pass 
	 * through the <code>ShaderProgram</code>.
	 */
	@OpenGLCall
	public void drawElements(Mesh mesh) {
		GL11.glDrawElements(mesh.toOpenGLMode(), mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
	}
	
	/**
	 * Clears the color and depth buffer. The function should be called before every rendering process
	 * to clean these buffers before writing.
	 */
	@OpenGLCall
	public void clearBuffers() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}
	
	@OpenGLCall
	public void update(Mesh mesh) {
		clearBuffers();
		
		camera.getRotation().add(0.01f, 0.01f, 0.01f, 0);
	
		camera.updateViewMatrix();
		
		drawElements(mesh);
	}

	@OpenGLCall
	public void resize(int width, int height) {
		if(camera != null) {
			camera.resize(width, height);
			GL11.glViewport(0, 0, camera.getWidth(), camera.getHeight());
		}
	}
}
