package fr.mercury.nucleus.renderer.logic;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31C;

import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>RenderingTechnique</code> is an interface describing a rendering-logic for one
 * or multiple {@link Mesh} within an updating-cycle.
 * 
 * @author GnosticOccultist
 */
public interface RenderLogic {
	
	/**
	 * Begins the rendering process for the provided {@link PhysicaMundi}.
	 * It can be used to change the current state of the graphics API,
	 * allowing for specific rendering.
	 * 
	 * @param physica The physica-mundi to render on the screen.
	 */
	@OpenGLCall
	void begin(PhysicaMundi physica);
	
	/**
	 * Performs the rendering technique on the provided {@link PhysicaMundi}.
	 * 
	 * @param physica The physica-mundi to render on the screen.
	 */
	@OpenGLCall
	void render(PhysicaMundi physica);
	
	/**
	 * Ends the rendering process for the provided {@link PhysicaMundi}.
	 * It can be used to restore the state of the graphics API, 
	 * when the rendering is finished.
	 * 
	 * @param physica The physica-mundi to render on the screen.
	 */
	@OpenGLCall
	void end(PhysicaMundi physica);
	
	/**
	 * Transfer the <code>VertexBuffer</code> to the bound <code>ShaderProgram</code> as attributes.
	 * The currently bound element array buffer (if any) will determine the amount of data to pass 
	 * through the <code>ShaderProgram</code>.
	 */
	@OpenGLCall
	default void drawElements(Mesh mesh) {
		GL11.glDrawElements(mesh.toOpenGLMode(), mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
	}
	
	@OpenGLCall
	default void drawElementsInstanced(Mesh mesh, int instanceCount) {
		GL31C.glDrawElementsInstanced(mesh.toOpenGLMode(), mesh.getVertexCount(),
					GL11.GL_UNSIGNED_INT, 0, instanceCount);
	}
	
	
	@OpenGLCall
	default void drawTriangles(Mesh mesh) {
		GL11.glDrawArrays(mesh.toOpenGLMode(), 0, mesh.getVertexCount());
	}
	
	/**
	 * Transfer the <code>VertexBuffer</code> to the bound <code>ShaderProgram</code> as attributes.
	 * The currently bound element array buffer (if any) will determine the amount of data to pass 
	 * through the <code>ShaderProgram</code>.
	 */
	@OpenGLCall
	default void drawRangeElements(Mesh mesh) {
		GL20.glDrawRangeElements(mesh.toOpenGLMode(), 0, mesh.getVertexCount(), mesh.getBuffer(VertexBufferType.INDEX).getData().limit(), GL11.GL_UNSIGNED_INT, 0);
	}
}
