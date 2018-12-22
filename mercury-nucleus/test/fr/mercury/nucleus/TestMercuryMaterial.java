package fr.mercury.nucleus;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.scenegraph.Material;

public class TestMercuryMaterial extends MercuryApplication {
	
	public static void main(String[] args) {
		TestMercuryMaterial app = new TestMercuryMaterial();
		app.start();
	}

	@Override
	protected void initialize() {
		Material[] materials = assetManager.loadMaterial("/shaders/unlit.json");
		for(int i = 0; i < materials.length; i++) {
			System.err.println(materials[i]);
		}
	}
}
