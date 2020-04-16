package fr.mercury.exempli.gratia.asset;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.asset.AssimpLoader;
import fr.mercury.nucleus.asset.AssimpLoader.ConfigFlag;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.TextureAtlas;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
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
	 * The physica-mundi to represent a cube in the scene.
	 */
	private PhysicaMundi cube;
	/**
	 * The physica-mundi to represent a teapot in the scene.
	 */
	private PhysicaMundi teapot;
	/**
	 * The physica-mundi to represent a capricorn in the scene.
	 */
	private PhysicaMundi capricorn;

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
		// Load the 2x2 texture atlas, make sure to apply nearest filtering 
		// to prevent adjacent texture borders.
		TextureAtlas atlas = assetManager.loadTextureAtlas("/textures/simple_texture_atlas.png", 2, 2)
				.setFilter(MinFilter.NEAREST, MagFilter.NEAREST)
				.setWrapMode(WrapMode.CLAMP_EDGES, WrapMode.CLAMP_EDGES);
		atlas.upload();
		
		// Select the first index of the atlas.
		atlas.setIndex(0);
		
		// Load and prepare the cube in the scene.
		cube = (PhysicaMundi) assetManager.loadAssimp("/model/cube.obj", ConfigFlag.IGNORE_ROOT_NODE);
		cube.setName("cube");
		cube.setTranslation(0.0f, 0.0f, 2f).setRotation(0.0f, 0.0f, 0.0f).setScale(1f, 1f, 1f);
		
		// Load and prepare the teapot in the scene.
		teapot = (PhysicaMundi) assetManager.loadAssimp("/model/teapot.obj", ConfigFlag.IGNORE_ROOT_NODE);
		teapot.setName("teapot");
		teapot.setTranslation(-5.0f, -0.5f, 2f).setRotation(0.0f, 0.0f, 0.0f).setScale(1f, 1f, 1f);
		
		// Load and prepare the capricorn in the scene.
		capricorn = (PhysicaMundi) assetManager.loadAssimp("/model/capricorn.obj", ConfigFlag.IGNORE_ROOT_NODE);
		capricorn.setName("capricorn");
		capricorn.setTranslation(5.0f, -1.0f, 2f).setRotation(0.0f, 0.0f, 0.0f).setScale(0.05f, 0.05f, 0.05f);
		
		// Select the fourth material which is "Unlit_atlas" to render the cube using
		// a texture atlas.
		Material[] materials = assetManager.loadMaterial("/materials/unlit.json");
		assert materials[3] != null;
		cube.setMaterial(materials[3]);
		// Set the texture of the cube to the loaded texture atlas.
		cube.getMaterial().texture = atlas;
		
		teapot.setMaterial(materials[3]);
		// Set the texture of the teapot to the loaded texture atlas.
		teapot.getMaterial().texture = atlas;
		
		capricorn.setMaterial(materials[3]);
		// Set the texture of the capricorn to the loaded texture atlas.
		capricorn.getMaterial().texture = atlas;
		
		// Finally, attach the cube, teapot and capricorn to the main scene.
		scene.attachAll(cube, teapot, capricorn);
	}
	
	@Override
	@OpenGLCall
	protected void update(ReadableTimer timer) {
		// Rotate slowly the cube, teapot and capricorn.
		cube.rotate(0, 0.01f, 0);
		teapot.rotate(0, 0.01f, 0);
		capricorn.rotate(0, 0.01f, 0);
	}
}
