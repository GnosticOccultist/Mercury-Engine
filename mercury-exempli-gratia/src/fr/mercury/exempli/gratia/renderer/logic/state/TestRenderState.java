package fr.mercury.exempli.gratia.renderer.logic.state;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState.Face;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState.WindingOrder;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
import fr.mercury.nucleus.utils.Timer;

public class TestRenderState extends MercuryApplication {
	
	/**
	 * The physica-mundi to represent the first cube in the scene.
	 */
	private PhysicaMundi cube1;
	
	/**
	 * Launch method for the <code>TestMercuryMaterial</code>, no arguments required.
	 * 
	 * @param args The arguments to pass to the application.
	 */
	public static void main(String[] args) {
		TestRenderState app = new TestRenderState();
		app.start();
	}
	
	@Override
	protected void initialize() {
		// Load the 2D texture for the cube and upload it directly to the GPU.
		var texture = assetManager.loadTexture2D("/textures/octostone.png")
				.setFilter(MinFilter.TRILINEAR, MagFilter.BILINEAR)
				.setWrapMode(WrapMode.REPEAT, WrapMode.REPEAT);
		texture.upload();
		
		// Load and prepare the cube in the scene.
		cube1 = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube1.setName("cube1");
		cube1.setTranslation(0, 0, 4F).setRotation(0f, 0, 0f).setScale(1f, 1f, 1f);
		
		// Apply the render state to render the cube.
		var cull = new FaceCullingState().setFace(Face.BACK)
				.setWindingOrder(WindingOrder.CLOCKWISE)
				.enable();
		cube1.setRenderState(cull);
		
		// Select the second material which is "Unlit" to render the cube using
		// a texture.
		Material[] materials = assetManager.loadMaterial("/materials/unlit.json");
		assert materials[1] != null;
		cube1.setMaterial(materials[1]);
		cube1.getMaterial().texture = texture;
		
		// Rotate the camera towards the cube.
		var translation = cube1.getLocalTransform().getTranslation();
		camera.lookAt(translation.x(), translation.y(), translation.z(), Vector3f.UNIT_Y);
		
		scene.attach(cube1);
	}
	
	@Override
	protected void update(Timer timer) {
		super.update(timer);
		
		cube1.rotate(0.01f, 0.01f, 0f);
	}
}
