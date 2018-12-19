package fr.mercury.nucleus;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.LoggerLevel;
import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.Texture2D;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;

public class TestApp extends MercuryApplication {
	
	private PhysicaMundi cube;
	
	private NucleusMundi nucleus;
	
	public static void main(String[] args) {
		TestApp app = new TestApp();
		app.start();
	}
	
	@Override
	protected void initialize() {
		Texture2D texture = assetManager.loadTexture2D("/model/octostone.png")
				.setFilter(MinFilter.TRILINEAR, MagFilter.BILINEAR)
				.setWrapMode(WrapMode.REPEAT, WrapMode.REPEAT);
		texture.upload();
		
		cube = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube.setName("cube-1");
		cube.getLocalTransform().setRotation(0.3f, 0, 0.3f).setScale(1f, 1f, 1f);
		
		PhysicaMundi cube1 = assetManager.loadPhysicaMundi("/model/cube.obj");
		cube1.setName("cube-2");
		cube1.getLocalTransform().setTranslation(2.5f, 0, 0).setRotation(0.3f, 0, 0.3f).setScale(1f, 1f, 1f);
		
		camera.lookAt(cube.getLocalTransform().getTranslation(), Vector3f.UNIT_Y);
		
		cube.getMesh().texture = texture;
		
		Texture2D texture2 = new Texture2D().color(new Color(0, 0, 1), 2048, 2048)
				.setFilter(MinFilter.BILINEAR, MagFilter.BILINEAR)
				.setWrapMode(WrapMode.REPEAT, WrapMode.REPEAT);
		texture2.upload();
		
		cube1.getMesh().texture = texture2;
		
		FactoryLogger.getLogger("mercury.scenegraph").setActive(LoggerLevel.DEBUG, true);
		
		nucleus = new NucleusMundi("sub-nucleus");
		
		nucleus.attach(cube);
		nucleus.attach(cube1);
		scene.attach(nucleus);
	}
	
	@Override
	protected void update(float tpf) {
		scene.rotate(.007f, 0.007f, 0);
		cube.rotate(0.0f, 0.03f, 0);
	}
}
