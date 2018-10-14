package fr.mercury.nucleus.texture;

public class TextureState implements Comparable<TextureState> {
	
	public Format format;
	public WrapMode sWrap, tWrap, rWrap;
	public MagFilter magFilter;
	public MinFilter minFilter;
	public CompareMode shadowCompareMode;
	
	public void reset() {
		format = null;
		sWrap = null;
		tWrap = null;
		rWrap = null;
	}
	
	@Override
	public int compareTo(TextureState other) {
		return -1;
	}
	
	/**
	 * <code>Format</code> represents the format use for the <code>Texture</code> creation.
	 * <p>
	 * This can be used to describe the way the data are stored inside a <code>Texture</code> or
	 * a <code>RenderBuffer</code>.
	 * <p>
	 * There are three basic kinds of image formats : color, depth or depth/stencil with different 
	 * bits depth per component (8, 16, 24, 32, etc...).
	 */
	public enum Format {
		/**
		 * 
		 */
		RGB8(24),
		/**
		 * 
		 */
		RGBA8(32),
		/**
		 * 
		 */
		RGB16F(48),
		/**
		 * 
		 */
		RGBA16F(64),
		/**
		 * 
		 */
		RGB32F(96),
		/**
		 * 
		 */
		RGBA32F(128),
		/**
		 * 
		 */
		DEPTH16(16, true),
		/**
		 * 
		 */
		DEPTH24(24, true),
		/**
		 * 
		 */
		DEPTH32(32, true),
		/**
		 * 
		 */
		DEPTH32F(32, true);
		
		private boolean isDepthFormat;
		private int bitsPerPixel;
		
		private Format(int bitsPerPixel, boolean isDepth) {
			this.bitsPerPixel = bitsPerPixel;
			this.isDepthFormat = isDepth;
		}
		
		private Format(int bitsPerPixel) {
			this.bitsPerPixel = bitsPerPixel;
			this.isDepthFormat = false;
		}
		
		/**
		 * @return Whether the <code>Format</code> is a depth format.
		 */
		public boolean isDepthFormat() {
			return isDepthFormat;
		}
		
		/**
		 * @return The number of bits per pixel for the <code>Format</code>.
		 */
		public int getBitsPerPixel() {
			return bitsPerPixel;
		}
	}
	
	/**
	 * <code>MinFilter</code> represents the minifying filter used when the pixel 
	 * being textured maps to an area greater to one texture element.
	 * <p>
	 * OpenGL Correspondance: <code>GL_TEXTURE_MIN_FILTER</code>.
	 */
	public enum MinFilter {
		/**
		 * Choose the value of the texture element that is nearest (in Manhattan distance) to the center of the pixel being textured. 
		 * <p>
		 * OpenGL Correspondance: <code>GL_NEAREST</code>.
		 */
		NEAREST,
		
		/**
		 * Choose the weighted average of the four texture elements that are closest to the center of the pixel being textured including
		 * the edges depending on the <code>WrapMode</code>.
		 * <p>
		 * OpenGL Correspondance: <code>GL_LINEAR</code>.
		 */
		BILINEAR,
		
		/**
		 * Choose between two mipmaps that most closely match the size of the pixel being textured using
		 * a weighted average of the four texture elements that are closest to the center of the pixel
		 * {@link MinFilter#BILINEAR}. Returns the weighted average of the two values.
		 * <p>
		 * OpenGL Correspondance: <code>GL_LINEAR_MIPMAP_LINEAR</code>.
		 */
		TRILINEAR;
	}
	
	/**
	 * <code>MagFilter</code> represents the magnification filter used when the pixel 
	 * being textured maps to an area less than or equal to one texture element.
	 * <p>
	 * OpenGL Correspondance: <code>GL_TEXTURE_MAG_FILTER</code>.
	 */
	public enum MagFilter  {
		/**
		 * Choose the value of the texture element that is nearest (in Manhattan distance) to the center of the pixel being textured. 
		 * <p>
		 * OpenGL Correspondance: <code>GL_NEAREST</code>.
		 */
		NEAREST,
		
		/**
		 * Choose the weighted average of the four texture elements that are closest to the center of the pixel being textured including
		 * the edges depending on the <code>WrapMode</code>.
		 * <p>
		 * OpenGL Correspondance: <code>GL_LINEAR</code>.
		 */
		BILINEAR;
	}
	
	/**
	 * <code>WrapMode</code> defines the wrap mode used for texture coordinates. This wrap mode is
	 * applied to the 'u' (horizontal) wrap and the 'v' (vertical) wrap independtly from each other.
	 * <p>
	 * OpenGL Correspondance: <code>GL_TEXTURE_WRAP_S</code> or <code>GL_TEXTURE_WRAP_T</code>.
	 * 
	 * @see WrapCoordinate
	 */
	public enum WrapMode {
		/**
		 * Wrap mode that repeat the texture once you go past the texture coordinates
		 * range.
		 * <p>
		 * OpenGL Correspondance: <code>GL_REPEAT</code>.
		 */
		REPEAT,
		
		/**
		 * Wrap mode that stops/clamp the texture to its last pixel when you fall
		 * off the edge of the texture coordinates.
		 * <p>
		 * OpenGL Correspondance: <code>GL_CLAMP_TO_EDGE</code>.
		 */
		CLAMP_EDGES,
		
		/**
		 * Wrap mode that stops/clamp the texture to its last pixel when you fall
		 * off the edge of the texture borders.
		 * <p>
		 * OpenGL Correspondance: <code>GL_CLAMP_TO_BORDER</code>.
		 */
		CLAMP_BORDER;
	}
	
	/**
	 * <code>WrapCoordinate</code> defines the 3 coordinates for the texture wrapping mode : 
	 * <li>'S' or 'u' (horizontal) wrap coordinate</li>
	 * <li>'T' or 'v' (vertical) wrap coordinate</li>
	 * <li>'R' or 'w' (depth) wrap coordinate</li>
	 * <p>
	 * OpenGL Correspondance: <code>GL_TEXTURE_WRAP_S</code>, <code>GL_TEXTURE_WRAP_T</code> or <code>GL_TEXTURE_WRAP_R</code>.
	 * 
	 * @see WrapMode
	 */
	public enum WrapCoordinate {
		
		/**
		 * <code>S</code> wrapping coordinate or <code>u</code> corresponding to horizontal.
		 */
		S,
		
		/**
		 * <code>T</code> wrapping coordinate or <code>v</code> corresponding to vertical.
		 */
		T,
		
		/**
		 * <code>R</code> wrapping coordinate or <code>w</code> corresponding to depth.
		 */
		R;
	}
	
	/**
	 * <code>CompareMode</code> specifies the texture comparison for
	 * a depth texture. For example compare the texture depth to the red
	 * texture component.
	 * <p>
	 * OpenGL Correspondance: <code>GL_TEXTURE_COMPARE_MODE</code>.
	 */
	public enum CompareMode {
		/**
		 * Comparison mode is disabled. The luminance, intensity
		 * or alpha is assigned to the appropriate value of the bound depth texture.
		 * <p>
		 * OpenGL Correspondance: <code>GL_NONE</code>.
		 */
		NONE,
		
		/**
		 * Compares the R coordinate to the value in the bound depth texture. If
		 * R is less or equal to the texture value then it returns 1.0 otherwise,
		 * it returns 0.0.
		 * <p>
		 * OpenGL Correspondance: <code>GL_COMPARE_R_TO_TEXTURE</code> | <code>GL_LEQUAL</code>.
		 */
		LESS_OR_EQUAL,
		
		/**
		 * Compares the R coordinate to the value in the bound depth texture. If
		 * R is greater or equal to the texture value then it returns 1.0 otherwise,
		 * it returns 0.0.
		 * <p>
		 * OpenGL Correspondance: <code>GL_COMPARE_R_TO_TEXTURE</code> | <code>GL_GEQUAL</code>.
		 */
		GREATER_OR_EQUAL;
	}
}
