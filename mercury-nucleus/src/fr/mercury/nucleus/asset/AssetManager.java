package fr.mercury.nucleus.asset;

import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;

// TODO: Redo the entire class.
public class AssetManager {
	
	private final GLSLLoader glslLoader;
	
	public AssetManager() {
		this.glslLoader = new GLSLLoader();
	}
	
	
	public ShaderSource loadShaderSource(String path) {
		return glslLoader.load(path);
	}
}
