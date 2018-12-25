package fr.mercury.nucleus.scenegraph.environment;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.UniformStructure;

/**
 * <code>Fog</code> represents the ambient fog present on the scene. The fog object is
 * defined by a {@link Color} and a density, and can be applied to any rendered {@link AnimaMundi} 
 * if specified so in the shaders.
 * 
 * @author GnosticOccultist
 */
public final class Fog implements UniformStructure {

	/**
	 * The name of the structure to access the uniforms.
	 */
	public static final String STRUCTURE_NAME = "fog";
	/**
	 * A fog with a density of 0, meaning it has no effects on the scene.
	 */
	public static final Fog NO_FOG = new Fog();
	
	/**
	 * The fog density.
	 */
	private float density;
	/**
	 * The fog color.
	 */
	private final Color color;
	
	/**
	 * Instantiates a new <code>Fog</code> with a {@link Color#WHITE}.
	 * The density is set to 0, meaning the fog effect won't be visible on the scene.
	 */
	public Fog() {
		this(new Color(), 0f);
	}
	
	/**
	 * Instantiates a new <code>Fog</code> with the provided color and
	 * density.
	 * 
	 * @param color	  The color of the fog (not null).
	 * @param density The density of the fog (&ge;0).
	 */
	public Fog(Color color, float density) {
		Validator.nonNull(color);
		Validator.nonNegative(density);
		
		this.color = color;
		this.density = density;
	}
	
	/**
	 * Return the color of the <code>Fog</code>.
	 * 
	 * @return The fog's color.
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * Return the density of the <code>Fog</code>.
	 * 
	 * @return The fog's density.
	 */
	public float getDensity() {
		return density;
	}
	
	@Override
	public void uniforms(ShaderProgram program) {
		var color = program.getUniform(STRUCTURE_NAME + ".color");
		if(color == null) {
			program.addUniform(STRUCTURE_NAME + ".color", UniformType.VECTOR4F, this.color);
			program.getUniform(STRUCTURE_NAME + ".color").upload(program);
		} else {
			color.setValue(UniformType.VECTOR4F, this.color);
			color.upload(program);
		}
		
		var density = program.getUniform(STRUCTURE_NAME + ".density");
		if(density == null) {
			program.addUniform(STRUCTURE_NAME + ".density", UniformType.FLOAT, this.density);
			program.getUniform(STRUCTURE_NAME + ".density").upload(program);
		} else {
			density.setValue(UniformType.FLOAT, this.density);
			density.upload(program);
		}
	}
	
	@Override
	public String name() {
		return STRUCTURE_NAME;
	}
	
	@Override
	public String toString() {
		return "Fog: " + color + " [" + density + "]";
	}
}
