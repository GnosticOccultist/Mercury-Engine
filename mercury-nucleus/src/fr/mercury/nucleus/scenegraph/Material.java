package fr.mercury.nucleus.scenegraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexAttribute;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBuffer;
import fr.mercury.nucleus.texture.Texture;
import fr.mercury.nucleus.utils.MercuryException;

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
	 * The attributes used to pass vertex data through the shader.
	 */
	private final List<VertexAttribute> attributes = new ArrayList<>();
	/**
	 * The set of the shader sources for the material.
	 */
	private final Map<String, List<ShaderSource>> sources = new HashMap<String, List<ShaderSource>>();
	/**
	 * The store for already loaded shaders.
	 */
	private final Map<String, ShaderProgram> shaders = new HashMap<String, ShaderProgram>();
	
	// TODO: Privatize field.
	public Texture texture;
	
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
	 * @param name 		  The name of the material (not null).
	 * @param description The description of the material.
	 */
	public Material(String name, String description) {
		Validator.nonNull(name, "The material's name can't be null!");
		
		this.name = name;
		this.description = description;
	}
	
	public void setupUniforms(ShaderProgram program) {
		if(texture != null) {
			program.use();
			program.register(texture);
			texture.upload();
			texture.bindToUnit(0);
		}
	}
	
	/**
	 * Prepare and binds the {@link VertexAttribute} of the <code>Material</code> to the {@link VertexBuffer}
	 * defined in the {@link Mesh} of the provided {@link PhysicaMundi}.
	 * <p>
	 * The method will only setup the attributes if one of the VBO is dirty.
	 * 
	 * @param physica The physica-mundi to bind attributes to.
	 */
	public void bindAttributes(PhysicaMundi physica) {
		var mesh = physica.getMesh();
		
		// If one of the buffer is dirty re-bind all the attributes.
		if(mesh.isDirty()) {
			
			// Upload the VAO and VBOs of the mesh.
			mesh.upload();
			
			for(var attrib : attributes) {
				
				var type = attrib.getBufferType();
				var key = (type != null && !type.isEmpty()) ? type : attrib.getName();
				var buffer = mesh.getBuffer(key);
				
				if(buffer == null) {
					throw new MercuryException("No VertexBuffer setup in " + 
							physica + " for attribute '" + key + "'!");
				}
				
				attrib.bindAttribute(buffer);
			}
		}
	}
	
	public ShaderProgram getFirstShader() {
		// TODO: Use a default material.
		ShaderProgram shader = shaders.values().stream().findFirst().orElseGet(new Supplier<ShaderProgram>() {

			@Override
			public ShaderProgram get() {
				// Compute the new shader.
				var source = sources.values().stream().findFirst().orElseThrow();
				var newShader = new ShaderProgram().attachSources(source);
				newShader.upload();
				shaders.put(name, newShader);
				return newShader;
			}
		});
		
		return shader;
	}
	
	public ShaderProgram getShader(String name) {
		var shader = shaders.get(name);
		if(shader == null) {
			// Compute the new shader.
			shader = new ShaderProgram().attachSources(sources.get(name));
			shader.upload();
			shaders.put(name, shader);
		}
		return shader;
	}
	
	public List<ShaderSource> getSources(String name) {
		return sources.get(name);
	}
	
	public List<String> getPrefabUniforms() {
		return prefabUniforms;
	}
	
	public List<VertexAttribute> getAttributes() {
		return attributes;
	}
	
	public void addAttribute(VertexAttribute attribute) {
		Validator.nonNull(attribute, "The vertex attribute can't be null!");
		this.attributes.add(attribute);
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
		
		if(texture != null) {
			texture.cleanup();
		}
	}
	
	public Material copy(Texture texture) {
		Material copy = new Material(name, description);
		copy.prefabUniforms.addAll(prefabUniforms);
		copy.shaders.putAll(shaders);
		copy.attributes.addAll(attributes);
		
		copy.texture = texture;
		
		return copy;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		String descr = description != null ? ": " + description : "";
		return "[" + name + "]" + descr + " " + prefabUniforms; 
	}
}
