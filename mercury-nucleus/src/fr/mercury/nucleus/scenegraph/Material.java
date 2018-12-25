package fr.mercury.nucleus.scenegraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
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
	 * The prefab uniforms needed by the material, set during material-file reading.
	 */
	private final List<String> prefabUniforms = new ArrayList<>();
	/**
	 * The set of the shader sources for the material.
	 */
	private final Map<String, List<ShaderSource>> sources = new HashMap<String, List<ShaderSource>>();
	/**
	 * The store for already loaded shaders.
	 */
	private final Map<String, ShaderProgram> shaders = new HashMap<String, ShaderProgram>();
	
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
	
	public ShaderProgram getShader(String name, AnimaMundi anima) {
		var shader = shaders.get(name);
		if(shader == null) {
			// Compute the new shader.
			shader = new ShaderProgram().attachSources(sources.get(name));
			shader.upload();
			shaders.put(name, shader);
		}
		addPrefabs(shader, anima);
		return shader;
	}
	
	public void addPrefabs(ShaderProgram shader, AnimaMundi anima) {
		for(int i = 0; i < prefabUniforms.size(); i++) {
			var prefabName = prefabUniforms.get(i);
			var property = anima.getEnvironmentProperty(prefabName);
			if(property != null) {
				property.uniforms(shader);
			}
		}
	}
	
	public List<ShaderSource> getSources(String name) {
		return sources.get(name);
	}
	
	public List<String> getPrefabUniforms() {
		return prefabUniforms;
	}
	
	public String getName() {
		return name;
	}
	
	public void addShaderSource(String name, ShaderSource source) {
		var list = sources.get(name);
		if(list == null) {
			list = new ArrayList<>();
			sources.put(name, list);
		}
		list.add(source);
	}
	
	public void cleanup() {
		shaders.values().forEach(ShaderProgram::cleanup);
	}
	
	@Override
	public String toString() {
		String descr = description != null ? ": " + description : "";
		return "[" + name + "]" + descr + " " + prefabUniforms; 
	}
}
