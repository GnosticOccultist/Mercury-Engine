package fr.mercury.nucleus.scenegraph.environment.light;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.UniformField;
import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>DirectionalLight</code> is an implementation of {@link Light}, to describe a light which
 * is assumed to be infinitely far away and will lit up the entire scene from a given direction.
 * A great example for comparison with the real-life, would be the sun or the moon which illuminates an entire
 * face of the Earth.
 * <p>
 * In addition to colors, this light's type only needs a direction vector to be correctly used in shaders.
 * 
 * @author GnosticOccultist
 */
public class DirectionalLight extends Light {
	
	/**
	 * The direction in which the directional light's rays are propagating.
	 */
	private final Vector3f direction;
	
	public DirectionalLight() {
		super();
		this.direction = new Vector3f(Vector3f.UNIT_Z);
	}
	
	@Override
	protected float computeInfluence(AnimaMundi animaMundi) {
		/*
		 * The distance isn't affecting the influence of the light, only its intensity.
		 */
		return getColor().strengthSqr() * getIntensity();
	}
	
	/**
	 * Return the direction in which the <code>DirectionalLight</code>'s rays are propagating.
	 * 
	 * @return The direction in which the light is emitting.
	 */
	@UniformField(name="direction", type=UniformType.VECTOR3F)
	public Vector3f getDirection() {
		return direction;
	}
	
	/**
	 * Sets the direction in which the <code>DirectionalLight</code>'s rays are propagating.
	 * The direction vector can't be null.
	 * 
	 * @param direction The direction in which the light is emitting.
	 */
	public void setDirection(Vector3f direction) {
		Validator.nonNull(direction);
		direction.set(direction);
	}

    @Override
    public void uniforms(ShaderProgram program) {
        
    }

    @Override
    public String name() {
        return null;
    }
}
