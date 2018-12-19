package fr.mercury.nucleus.renderer.logic;

import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;

/**
 * <code>DefaultRenderingTechnique</code> is an implementation of {@link RenderLogic},
 * describing the default rendering-logic used by the <code>Mercury-Engine</code>.
 * 
 * @author GnosticOccultist
 */
public class DefaultRenderLogic implements RenderLogic {

	@Override
	public void begin(PhysicaMundi physica) {
		
		Mesh mesh = physica.getMesh();
		
		mesh.bindBeforeRender();
	}

	@Override
	public void render(PhysicaMundi physica) {
		
		Mesh mesh = physica.getMesh();
		
		drawElements(mesh);
	}

	@Override
	public void end(PhysicaMundi physica) {
		
		Mesh mesh = physica.getMesh();
		
		mesh.unbindAfterRender();
	}
}
