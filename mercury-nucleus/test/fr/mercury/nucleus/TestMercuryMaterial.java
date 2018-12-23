package fr.mercury.nucleus;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.Texture2D;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;

public class TestMercuryMaterial extends MercuryApplication {
	
	public static void main(String[] args) {
		TestMercuryMaterial app = new TestMercuryMaterial();
		app.start();
	}

	@Override
	protected void initialize() {
		Texture2D texture = assetManager.loadTexture2D("/model/octostone.png")
				.setFilter(MinFilter.TRILINEAR, MagFilter.BILINEAR)
				.setWrapMode(WrapMode.REPEAT, WrapMode.REPEAT);
		texture.upload();
		
		PhysicaMundi cube = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube.setName("cube");
		cube.setTranslation(2.5f, 0, 0).setRotation(0.3f, 0, 0.3f).setScale(1f, 1f, 1f);
		
		Material[] materials = assetManager.loadMaterial("/shaders/unlit.json");
		for(int i = 0; i < materials.length; i++) {
			System.err.println(materials[i]);
		}
		
		cube.setMaterial(materials[0]);
		cube.getMesh().texture = texture;
		scene.attach(cube);
	}
	
	@Override
	protected void update(float tpf) {
		super.update(tpf);
		
		scene.rotate(0, 0.1f, 0);
	}
}
