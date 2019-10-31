package fr.mercury.nucleus.renderer.logic;

import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>DefaultRenderingTechnique</code> is an implementation of {@link RenderLogic}, describing the default 
 * rendering-logic used by the <code>Mercury-Engine</code>. It is performing as follows:
 * <p>
 * <li>Bind the vertex array and its buffers and enable all corresponding vertex attributes.</li>
 * <li>Render the mesh by either using the drawElements() or drawArrays() depending if indices were defined for the mesh.</li>
 * <li>Unbind the vertex array and its buffers and disable all corresponding vertex attributes.</li>
 * </p>
 * Note that for the rendering to occur a {@link ShaderProgram} should have been previously marked as used and if uniforms are defined
 * in said shaders, they should be provided. 
 * <br>
 * Also, an optional render state can be defined for example to render transparent materials, or draw only the lines composing the mesh, 
 * but this currently isn't integrated within the engine, you will have to handle it yourself using the current OpenGL context.
 * 
 * @author GnosticOccultist
 */
public class DefaultRenderLogic implements RenderLogic {

	@Override
	@OpenGLCall
	public void begin(PhysicaMundi physica) {
		
		var mesh = physica.getMesh();
		
		mesh.bindBeforeRender();
	}

	@Override
	@OpenGLCall
	public void render(PhysicaMundi physica) {
		
		var mesh = physica.getMesh();
		
		// Check that our mesh as an indices buffer setup to draw elements, otherwise draw arrays.
		if(mesh.hasIndices()) {
			drawElements(mesh);
		} else {
			drawArrays(mesh);
		}
	}

	@Override
	@OpenGLCall
	public void end(PhysicaMundi physica) {
		
		var mesh = physica.getMesh();
		
		mesh.unbindAfterRender();
	}
}
