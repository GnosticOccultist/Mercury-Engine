package fr.mercury.nucleus;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.application.module.TaskExecutorModule;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.TextureAtlas;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>TestTextureAtlas</code> showcase the usage of {@link TextureAtlas} to save on <code>OpenGL</code> texture units
 * when applying a texture to a {@link Mesh}.
 * 
 * @author GnosticOccultist
 */
public class TestTextureAtlas extends MercuryApplication {
	
	/**
	 * The physica-mundi to represent a cube in the scene.
	 */
	private PhysicaMundi cube;
	/**
	 * The texture altas used for texturing the cube mesh.
	 */
	private TextureAtlas atlas;

	/**
	 * Launch method for the <code>TestTextureAtlas</code>, no arguments required.
	 * 
	 * @param args The arguments to pass to the application.
	 */
	public static void main(String[] args) {
		TestTextureAtlas app = new TestTextureAtlas();
		app.start();
	}
	
	@Override
	@OpenGLCall
	protected void initialize() {
		// Load the 2x2 texture atlas, make sure to apply nearest filtering 
		// to prevent adjacent texture borders.
		atlas = assetManager.loadTextureAtlas("/model/simple_texture_atlas.png", 2, 2)
				.setFilter(MinFilter.NEAREST, MagFilter.NEAREST)
				.setWrapMode(WrapMode.CLAMP_EDGES, WrapMode.CLAMP_EDGES);
		atlas.upload();
		
		// Load and prepare the cube in the scene.
		cube = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube.setName("cube");
		cube.setTranslation(0.0f, 0.0f, 0.0f).setRotation(0.0f, 0.0f, 0.0f).setScale(1f, 1f, 1f);
		
		// Select the third material which is "Unlit_atlas" to render the cube using
		// a texture atlas.
		Material[] materials = assetManager.loadMaterial("/shaders/unlit.json");
		assert materials[3] != null;
		cube.setMaterial(materials[3]);
		// Set the texture of the cube to the loaded texture atlas.
		cube.getMesh().texture = atlas;
		
		// Finally, attach the cube to the main scene.
		scene.attach(cube);
		
		// Add the uniforms for computing the texture coordinates based on the atlas index inside the shader.
		// TODO: This should be managed automatically by the material or renderer.
		var shader = cube.getMaterial().getShader("Unlit_atlas");
		shader.addUniform("rows", UniformType.FLOAT, atlas.getNumRows());
		shader.addUniform("cols", UniformType.FLOAT, atlas.getNumCols());
		
		// Link the task execution module to the application and schedule an updating index task each second.
		TaskExecutorModule module = new TaskExecutorModule();
		module.scheduleAtFixedRate(this::updateIndex, 1000);
		linkModule(module);
	}
	
	@Override
	@OpenGLCall
	protected void update(float tpf) {
		// Rotate slowly the cube.
		cube.rotate(0.01f, 0.01f, 0.0f);
		
		// Update the 'uvOffset' uniform value to the shader.
		var shader = cube.getMaterial().getShader("Unlit_atlas");
		shader.addUniform("uvOffset", UniformType.VECTOR2F, atlas.offset());
	}
	
	/**
	 * Update the {@link TextureAtlas} index by switching to the next one (in row order).
	 */
	private void updateIndex() {
		var nextIndex = atlas.getIndex() + 1;
		if(nextIndex > 3) {
			nextIndex = 0;
		}
	
		atlas.setIndex(nextIndex);
	}
}
