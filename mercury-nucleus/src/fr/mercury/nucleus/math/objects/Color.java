package fr.mercury.nucleus.math.objects;

import fr.alchemy.utilities.Validator;

/**
 * <code>Color</code> is a mathematical object defined of a red, green, blue and alpha component
 * exprimed by floating point numbers.
 * <p>
 * The first three components caracterize the percentage of each in the total color, the alpha
 * caracterize the opacity of the color (0 &rarr; invisible, 1 &rarr; opaque).
 * <p>
 * The components are stored in linear color space.
 * 
 * @author GnosticOccultist
 */
public final class Color {
	
	/**
	 * The red component.
	 */
	public float r;
	/**
	 * The green component.
	 */
	public float g;
	/**
	 * The blue component.
	 */
	public float b;
	/**
	 * The alpha component.
	 */
	public float a;
	
	/**
	 * Instantiates a new <code>Color</code> with all the components
	 * set to 1.
	 * It is the equivalent to the white color.
	 */
	public Color() {
		this.r = g = b = a = 1.0f;
	}
	
	/**
	 * Instantiates a new <code>Color</code> with the provided
	 * components.
	 * 
	 * @param r The red component to copy from.
	 * @param g The green component to copy from.
	 * @param b The blue component to copy from.
	 * @param a The alpha component to copy from.
	 */
	public Color(float r, float g, float b, float a) {
		set(r, g, b, a);
	}
	
	/**
	 * Instantiates a new <code>Color</code> with the provided
	 * components.
	 * The alpha component is set to 1.
	 * 
	 * @param r The red component to copy from.
	 * @param g The green component to copy from.
	 * @param b The blue component to copy from.
	 */
	public Color(float r, float g, float b) {
		set(r, g, b, 1.0f);
	}
	
	/**
	 * Instantiates a new <code>Color</code> with the provided
	 * color's components.
	 * 
	 * @param other The other color to get the components.
	 */
	public Color(Color other) {
		set(other);
	}
	
	/**
	 * Set the provided components values to this <code>Color</code> 
	 * components.
	 * 
	 * @param r The red component to copy from.
	 * @param g The green component to copy from.
	 * @param b The blue component to copy from.
	 * @param a The alpha component to copy from.
	 */
	public Color set(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		return this;
	}
	
	/**
	 * Set the provided components values to this <code>Color</code> 
	 * components.
	 * 
	 * @param r The red component to copy from.
	 * @param g The green component to copy from.
	 * @param b The blue component to copy from.
	 */
	public Color set(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
		return this;
	}
	
	/**
	 * Set the components values of the provided color to this 
	 * <code>Color</code> components.
	 * <p>
	 * The provided color cannot be null.
	 * 
	 * @param other The other color to copy from.
	 */
	public Color set(Color other) {
		Validator.nonNull(other, "The color cannot be null!");
		
		this.r = other.r;
		this.g = other.g;
		this.b = other.b;
		this.a = other.a;
		return this;
	}
	
	/**
     * Transform this <code>Color</code> to a <code>Vector3f</code> using
     * the red, green and blue components, the alpha one isn't incorporated.
     *
     * @return A vector containing the RGB value of this color.
     */
	public Vector3f asVector3f() {
		return new Vector3f(r, g, b);
	}
	
	/**
     * Transform this <code>Color</code> to a <code>Vector4f</code> using
     * the red, green, blue and alpha components.
     *
     * @return A vector containing the RGBA value of this color.
     */
	public Vector4f asVector4f() {
		return new Vector4f(r, g, b, a);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Color)) {
			return false;
		}

		if (this == o) {
			return true;
		}

		Color other = (Color) o;
		if (Float.compare(r, other.r) != 0) {
			return false;
		}
		if (Float.compare(g, other.g) != 0) {
			return false;
		}
		if (Float.compare(b, other.b) != 0) {
			return false;
		}
		
		return Float.compare(a, other.a) == 0;
	}
}
