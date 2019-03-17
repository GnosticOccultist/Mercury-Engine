package fr.mercury.nucleus;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.application.module.TaskExecutorModule;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.scenegraph.environment.Fog;
import fr.mercury.nucleus.texture.Texture2D;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;

public class TestMercuryMaterial extends MercuryApplication {
	
	public static void main(String[] args) {
		TestMercuryMaterial app = new TestMercuryMaterial();
		app.start();
	}

	private PhysicaMundi cube;

	@Override
	protected void initialize() {
		Texture2D texture = assetManager.loadTexture2D("/model/octostone.png")
				.setFilter(MinFilter.TRILINEAR, MagFilter.BILINEAR)
				.setWrapMode(WrapMode.REPEAT, WrapMode.REPEAT);
		texture.upload();
		
		cube = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube.setName("cube");
		cube.setTranslation(3, 0, 0).setRotation(0.3f, 0, 0.3f).setScale(1f, 1f, 1f);
		
		var translation = cube.getLocalTransform().getTranslation();
		camera.lookAt(translation.x(), translation.y(), translation.z(), Vector3f.UNIT_Y);
		
		Material[] materials = assetManager.loadMaterial("/shaders/unlit.json");
		for(int i = 0; i < materials.length; i++) {
			System.err.println(materials[i]);
		}
		
		var cube1 = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube1.setName("cube1");
		cube1.setTranslation(3, 0, -5).setRotation(0.3f, 0, 0.3f).setScale(1f, 1f, 1f);
		
		cube.setMaterial(materials[0]);
		//cube.setMaterial(materials[1]);
		cube.getMesh().texture = texture;
		cube1.setMaterial(materials[0]);
		//cube.setMaterial(materials[1]);
		cube1.getMesh().texture = texture;
		
		Fog fog = new Fog(new Color(0.4f, 0.4f, 0.5f, 1), 0.1f);
		scene.addEnvironmentElement(fog);
		scene.attach(cube);
		scene.attach(cube1);
		
		TaskExecutorModule module = new TaskExecutorModule();
		linkModule(module);
	}
	
	@Override
	protected void update(float tpf) {
		super.update(tpf);
		
		cube.rotate(0.0f, 0.03f, 0);
	}
}
