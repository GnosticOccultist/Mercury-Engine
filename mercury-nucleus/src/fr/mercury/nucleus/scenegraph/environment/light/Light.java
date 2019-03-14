package fr.mercury.nucleus.scenegraph.environment.light;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.environment.EnvironmentElement;

/**
 * <code>Light</code> represents an abstraction layer for every light objects. It should be implemented
 * into specific type, which illuminates some part of the scene by modifying the visual properties of {@link AnimaMundi}.
 * <p>
 * Most of the light will affect the color values of an object depending on 3 components:
 * <ul>
 * <li>Ambient lighting defines the intensity or color if no lights is applied to an object. A dark gray tone
 * is ofently used for this effect to simulate obscurity.
 * <li>Diffuse lighting defines the light effect on rough surfaces, where most of the rays are diffused in all
 * direction.
 * <li>Specular lighting defines the light effect on shiny surface, whre the ray is mostly reflected to the 
 * opposite direction and suffers from lower diffusion than the <code>Diffuse lighting</code>. Its effect also
 * depends on the observer (camera) position, if the reflected ray is hitting its view.
 * </ul>
 * 
 * @author GnosticOccultist
 */
public abstract class Light implements EnvironmentElement {
	
	/**
	 * The lighting effect which affect every fragment the same way, mainly
	 * used to define the itensity factor if no lights is applied.
	 */
	private final Color ambient;
	/**
	 * The lighting effect color on rough surface. The ray is being mostly 
	 * diffused in all direction.
	 */
	private final Color diffuse;
	/**
	 * The lighting effect color on shiny surface. The ray is being mostly 
	 * reflected to the opposite direction and suffers from lower diffusion.
	 */
	private final Color specular;
	
	/**
	 * Instantiates a new <code>Light</code> with the default colors.
	 */
	protected Light() {
		this.ambient = new Color(0.2f, 0.2f, 0.2f, 1f);
		this.diffuse = new Color(1f, 1f, 1f, 1f);
		this.specular = new Color(1f, 1f, 1f, 1f);
	}
	
	/**
	 * Copy the component of this <code>Light</code> to the provided one.
	 * The provided light can't be null and must be of the same type as this one.
	 * 
	 * @param other The other light to copy components from.
	 */
	public void copy(Light other) {
		Validator.nonNull(other);
		if(other.getClass() != getClass()) {
			throw new IllegalArgumentException("The provided light to copy must be the same type!");
		}
		
		this.ambient.set(other.ambient);
		this.diffuse.set(other.diffuse);
		this.specular.set(other.specular);
	}
	
	/**
	 * Return the ambient color of the <code>Light</code>.
	 * 
	 * @return The ambient color of the light.
	 */
	public Color getAmbient() {
		return ambient;
	}
	
	/**
	 * Sets the ambient color of the <code>Light</code>.
	 * The provided color can't be null.
	 * 
	 * @param ambient The ambient color of the light.
	 */
	public void setAmbient(Color ambient) {
		Validator.nonNull(ambient);
		this.ambient.set(ambient);
	}
	
	/**
	 * Return the diffuse color of the <code>Light</code>.
	 * 
	 * @return The diffuse color of the light.
	 */
	public Color getDiffuse() {
		return diffuse;
	}
	
	/**
	 * Sets the diffuse color of the <code>Light</code>.
	 * The provided color can't be null.
	 * 
	 * @param diffuse The diffuse color of the light.
	 */
	public void setDiffuse(Color diffuse) {
		Validator.nonNull(diffuse);
		this.diffuse.set(diffuse);
	}
	
	/**
	 * Return the specular color of the <code>Light</code>.
	 * 
	 * @return The specular color of the light.
	 */
	public Color getSpecular() {
		return specular;
	}
	
	/**
	 * Sets the specular color of the <code>Light</code>.
	 * The provided color can't be null.
	 * 
	 * @param specular The specular color of the light.
	 */
	public void setSpecular(Color specular) {
		Validator.nonNull(specular);
		this.specular.set(specular);
	}
	
	/**
	 * Return <code>false</code> since multiple <code>Light</code> can be added on
	 * the scene-graph at a same level.
	 */
	@Override
	public boolean isSingleton() {
		return false;
	}
}
