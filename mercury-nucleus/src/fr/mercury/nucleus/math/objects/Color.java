package fr.mercury.nucleus.math.objects;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.pool.Reusable;
import fr.mercury.nucleus.math.readable.ReadableColor;

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
public final class Color implements ReadableColor, Comparable<Color>, Reusable {
	
	/**
	 * The white color (1,1,1,1).
	 */
	public static final ReadableColor WHITE = new Color();
	/**
	 * The black color (0,0,0,1).
	 */
	public static final ReadableColor BLACK = new Color();
	
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
	public Color(ReadableColor other) {
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
	public Color set(ReadableColor other) {
		Validator.nonNull(other, "The color cannot be null!");
		
		this.r = other.r();
		this.g = other.g();
		this.b = other.b();
		this.a = other.a();
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
	
	/**
	 * Return the red component of the <code>Color</code>, as a single-precision float.
	 * 
	 * @return The red component value of the color.
	 */
	@Override
	public float r() {
		return r;
	}
	
	/**
	 * Return the green component of the <code>Color</code>, as a single-precision float.
	 * 
	 * @return The green component value of the color.
	 */
	@Override
	public float g() {
		return g;
	}
	
	/**
	 * Return the blue component of the <code>Color</code>, as a single-precision float.
	 * 
	 * @return The blue component value of the color.
	 */
	@Override
	public float b() {
		return b;
	}
	
	/**
	 * Return the alpha component of the <code>Color</code>, as a single-precision float.
	 * 
	 * @return The alpha component value of the color.
	 */
	@Override
	public float a() {
		return a;
	}
	
	/**
   	 * Sets all the components of the <code>Color</code> to {@link #WHITE},
   	 * before retrieving it from a pool.
   	 */
   	@Override
   	public void reuse() {
   		set(1, 1, 1, 1);
   	}
   	
   	/**
   	 * Sets all the components of the <code>Color</code> to {@link #WHITE},
   	 * before storing it into a pool.
   	 */
   	@Override
   	public void free() {
   		set(1, 1, 1, 1);
   	}

	/**
	 * Compare this color with the provided <code>Color</code>. It will first
	 * compare the R-component, then the G-component and so on.
	 * 
	 * @param  The other color to compare with (not null).
	 * @return 0 &rarr; the 2 colors are equal, negative &rarr; this color comes before 
	 * 		   the other, negative &rarr; this color comes after the other.
	 */
	@Override
	public int compareTo(Color other) {
		int result = Float.compare(r, other.r);
		if(result == 0) {
			result = Float.compare(g, other.g);
		}
        if(result == 0) {
        	result = Float.compare(b, other.b);
        }
        if(result == 0) {
        	result = Float.compare(a, other.a);
        }
        
        return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if (!(o instanceof ReadableColor)) {
			return false;
		}

		var other = (ReadableColor) o;
		if (Float.compare(r, other.r()) != 0) {
			return false;
		}
		if (Float.compare(g, other.g()) != 0) {
			return false;
		}
		if (Float.compare(b, other.b()) != 0) {
			return false;
		}
		
		return Float.compare(a, other.a()) == 0;
	}
	
	@Override
	public String toString() {
		return "Color [ r= " + r + ", g= " + g + ", b= " + b + ", a= " + a + " ]";
	}
}
