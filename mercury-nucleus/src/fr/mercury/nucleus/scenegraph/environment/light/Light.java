package fr.mercury.nucleus.scenegraph.environment.light;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.readable.ReadableColor;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.environment.EnvironmentElement;

/**
 * <code>Light</code> represents an abstraction layer for every light objects.
 * It should be implemented into specific type, which illuminates some part of
 * the scene by modifying the visual properties of {@link AnimaMundi}.
 * <p>
 * Most of the light will affect the color values of an object depending on 3
 * components:
 * <ul>
 * <li>Ambient lighting defines the intensity or color if no lights is applied
 * to an object. A dark gray tone is oftently used for this effect to simulate
 * obscurity.
 * <li>Diffuse lighting defines the light effect on rough surfaces, where most
 * of the rays are diffused in all direction.
 * <li>Specular lighting defines the light effect on shiny surface, where the
 * ray is mostly reflected to the opposite direction and suffers from lower
 * diffusion than the <code>Diffuse lighting</code>. Its effect also depends on
 * the observer (camera) position, if the reflected ray is hitting its view.
 * </ul>
 * 
 * @author GnosticOccultist
 */
public abstract class Light implements EnvironmentElement {

    /**
     * The color of the lighting effect.
     */
    private final Color color;
    /**
     * The intensity of the light. An intensity of 0 means that the light is
     * disabled.
     */
    private float intensity = 1.0f;

    /**
     * Instantiates a new <code>Light</code> with a {@link Color#WHITE} color.
     */
    protected Light() {
        this.color = new Color(Color.WHITE);
    }

    protected abstract float computeInfluence(AnimaMundi animaMundi);

    /**
     * Copy the component of this <code>Light</code> to the provided one. The
     * provided light can't be null and must be of the same type as this one.
     * 
     * @param other The other light to copy components from (not null).
     */
    public void copy(Light other) {
        Validator.nonNull(other, "The light can't be null!");

        if (other.getClass() != getClass()) {
            throw new IllegalArgumentException("The provided light to copy must be the same type!");
        }

        setColor(other.color);
    }

    /**
     * Return the color of the <code>Light</code>.
     * 
     * @return The color of the light.
     */
    public ReadableColor getColor() {
        return color;
    }

    /**
     * Sets the color of the <code>Light</code>. The provided color can't be null.
     * 
     * @param ambient The color of the light (not null).
     */
    public void setColor(Color ambient) {
        Validator.nonNull(ambient, "The color of a light can't be null!");
        this.color.set(ambient);
    }

    /**
     * Return the intensity of the <code>Light</code>. An intensity of 0 means that
     * the light is disabled.
     * 
     * @return The intensity of the light (&ge;0).
     */
    public float getIntensity() {
        return intensity;
    }

    /**
     * Defines the intensity of the <code>Light</code>. An intensity of 0 means that
     * the light will be disabled.
     * 
     * @param intensity The intensity of the light (&ge;0).
     */
    public void setIntensity(float intensity) {
        Validator.nonNegative(intensity, "The intensity of a light can't be negative!");
        this.intensity = intensity;
    }

    @Override
    public String toString() {
        return "Light [color=" + color + ", intensity=" + intensity + "]";
    }
}
