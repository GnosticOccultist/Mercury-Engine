package fr.mercury.nucleus.scenegraph;

import java.util.HashSet;
import java.util.Set;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;

public class Material {
	
	/**
	 * The name of the material, used for debugging.
	 */
	private String name;
	/**
	 * The description of the material, used for debugging.
	 */
	private String description;
	
	private final Set<ShaderSource> sources = new HashSet<>();
	
	public Material() {}
	
	public Material(String name, String description) {
		Validator.nonNull(name);
		
		this.name = name;
		this.description = description;
	}
	
	public void addShaderSource(ShaderSource source) {
		sources.add(source);
	}
	
	@Override
	public String toString() {
		String descr = description != null ? ": " + description : "";
		return "[" + name + "]" + descr; 
	}
}
