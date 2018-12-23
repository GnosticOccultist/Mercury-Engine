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
	/**
	 * The set of the shader sources for the material.
	 */
	private final Set<ShaderSource> sources = new HashSet<>();
	
	/**
	 * Instantiates a new empty <code>Material</code>.
	 */
	public Material() {}
	
	/**
	 * Instantiates a new <code>Material</code> with the provided name
	 * and description. Do note that the name can't be null.
	 * <p>
	 * In order for the material to be correctly used, {@link #addShaderSource(ShaderSource) add}
	 * {@link ShaderSource} to it.
	 * 
	 * @param name 		  The name of the material.
	 * @param description The description of the material.
	 */
	public Material(String name, String description) {
		Validator.nonNull(name);
		
		this.name = name;
		this.description = description;
	}
	
	public Set<ShaderSource> getSources() {
		return sources;
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
