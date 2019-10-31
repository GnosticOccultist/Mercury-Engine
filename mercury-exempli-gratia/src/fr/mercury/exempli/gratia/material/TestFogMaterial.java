package fr.mercury.exempli.gratia.material;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.scenegraph.environment.Fog;
import fr.mercury.nucleus.texture.Texture2D;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
import fr.mercury.nucleus.utils.Timer;

/**
 * <code>TestMercuryMaterial</code> showcase the usage of {@link Material} to render a fog effect on a {@link PhysicaMundi}.
 * 
 * @author GnosticOccultist
 */
public class TestFogMaterial extends MercuryApplication {
	
	/**
	 * The physica-mundi to represent the first cube in the scene.
	 */
	private PhysicaMundi cube1;
	/**
	 * The physica-mundi to represent the second cube in the scene.
	 */
	private PhysicaMundi cube2;
	
	/**
	 * Launch method for the <code>TestMercuryMaterial</code>, no arguments required.
	 * 
	 * @param args The arguments to pass to the application.
	 */
	public static void main(String[] args) {
		TestFogMaterial app = new TestFogMaterial();
		app.start();
	}
	
	@Override
	protected void initialize() {
		// Load the 2D texture for the cube and upload it directly to the GPU.
		Texture2D texture = assetManager.loadTexture2D("/textures/octostone.png")
				.setFilter(MinFilter.TRILINEAR, MagFilter.BILINEAR)
				.setWrapMode(WrapMode.CLAMP_EDGES, WrapMode.CLAMP_EDGES);
		texture.upload();
		
		// Load and prepare both cubes in the scene.
		cube1 = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube1.setName("cube1");
		cube1.setTranslation(0, 0, 4F).setRotation(0f, 0, 0f).setScale(1f, 1f, 1f);
		
		cube2 = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube2.setName("cube2");
		cube2.setTranslation(0, 0, 2.5F).setRotation(0f, 0, 0f).setScale(1f, 1f, 1f);
		
		// Select the second material which is "Unlit" to render the cube using
		// a texture and a fog.
		Material[] materials = assetManager.loadMaterial("/materials/unlit.json");
		cube1.setMaterial(materials[1]);
		cube1.getMaterial().texture = texture;
		cube2.setMaterial(materials[1]);
		cube2.getMaterial().texture = texture;
		
		// Rotate the camera towards the first cube.
		var translation = cube1.getLocalTransform().getTranslation();
		camera.lookAt(translation.x(), translation.y(), translation.z(), Vector3f.UNIT_Y);
		
		// Attach a fog effect to the root-scene, so both cubes are affected.
		Fog fog = new Fog(new Color(0.4f, 0.4f, 0.5f, 1), 0.1f);
		scene.addEnvironmentElement(fog);
		scene.attach(cube1);
		scene.attach(cube2);
	}
	
	@Override
	protected void update(Timer timer) {
		super.update(timer);
		
		// Rotate and translate the first cube away from the camera.
		cube1.rotate(0.01f, 0.01f, 0.0f);
		cube1.translate(0, 0, -0.03f);
		
		// Just rotate the second cube.
		cube2.rotate(0.0f, 0.01f, 0.01f);
	}
}
