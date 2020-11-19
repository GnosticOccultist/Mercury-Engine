package fr.mercury.exempli.gratia.asset;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.asset.AssimpLoader;
import fr.mercury.nucleus.asset.AssimpLoader.ConfigFlag;
import fr.mercury.nucleus.renderer.logic.state.BlendState;
import fr.mercury.nucleus.renderer.logic.state.DepthBufferState;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Face;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>TestAssimpLoader</code> showcase the usage of an {@link AssimpLoader} to load a {@link PhysicaMundi} in Mercury
 * using the <code>Assimp</code> libraries via <code>LWJGL3</code> bindings.
 * 
 * @author GnosticOccultist
 */
public class TestAssimpLoader extends MercuryApplication {

	/**
	 * The physica-mundi to represent the sponza model in the scene.
	 */
	private AnimaMundi sponza;

	/**
	 * Launch method for the <code>TestAssimpLoader</code>, no arguments required.
	 * 
	 * @param args The arguments to pass to the application.
	 */
	public static void main(String[] args) {
		TestAssimpLoader app = new TestAssimpLoader();
		app.start();
	}
	
	@Override
	@OpenGLCall
	protected void initialize() {
		// Load and prepare the cube in the scene.
		sponza = assetManager.loadAssimp("resources/model/sponza/sponza.gltf", ConfigFlag.IGNORE_ROOT_NODE);
		sponza.setName("sponza");
		sponza.setTranslation(0.0f, 0.3F, 0.0F).setScale(1f, 1f, 1f);
		
		camera.setLocation(5.0f, 2.0f, 0.0f);
		
		scene.setRenderStates(new DepthBufferState().enable(), new BlendState().enable(), new FaceCullingState().setFace(Face.BACK).enable());
		
		// Finally, attach the sponza to the main scene.
		scene.attachAll(sponza);
	}
	
	@Override
	@OpenGLCall
	protected void update(ReadableTimer timer) {
		
	}
}
