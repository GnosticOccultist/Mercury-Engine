package fr.mercury.nucleus.scenegraph.environment;

import fr.mercury.nucleus.renderer.opengl.shader.uniform.UniformStructure;

public interface EnvironmentElement extends UniformStructure {
	
	default boolean isSingleton() {
		return true;
	}
}
