package fr.mercury.nucleus.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.scene.Mesh;
import fr.mercury.nucleus.utils.OpenGLCall;

public class Renderer {
	
	private final Camera camera;
	
	public Renderer(Camera camera) {
		Validator.nonNull(camera);
		
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
	 * Transfer the <code>VertexBuffer</code> to the bound <code>ShaderProgram</code> as attributes.
	 * The currently bound element array buffer (if any) will determine the amount of data to pass 
	 * through the <code>ShaderProgram</code>.
	 */
	@OpenGLCall
	public void drawRangeElements(Mesh mesh) {
		GL20.glDrawRangeElements(mesh.toOpenGLMode(), 0, mesh.getVertexCount(), mesh.getBuffer(VertexBufferType.INDEX).getData().limit(), GL11.GL_UNSIGNED_INT, 0);
	}
	
	/**
	 * Clears the color and depth buffer. The function should be called before every rendering process
	 * to clean these buffers before writing.
	 */
	@OpenGLCall
	public void clearBuffers() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
	}
	
	@OpenGLCall
	public void render(Mesh mesh) {
		clearBuffers();
		
		camera.getRotation().add(0.01f, 0.01f, 0.01f, 0);

		camera.updateViewMatrix();
		
		mesh.bind();
		
		mesh.texture.bindToUnit(0);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		drawElements(mesh);
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		
		mesh.texture.unbind();
		
		mesh.unbind();
	}

	@OpenGLCall
	public void resize(int width, int height) {
		if(camera != null) {
			camera.resize(width, height);
			GL11.glViewport(0, 0, camera.getWidth(), camera.getHeight());
		}
	}
}
