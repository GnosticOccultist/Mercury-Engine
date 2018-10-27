package fr.mercury.nucleus.texture;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.math.objects.Color;

public class Image {
	
	/**
	 * The width of the image.
	 */
	private int width; 
	/**
	 * The height of the image.
	 */
	private int height;
	/**
	 * The data of the image.
	 */
	private ByteBuffer data;
	/**
	 * The image format.
	 */
	private Format format;
	
	/**
	 * Instantiates a new <code>Image</code> with the provided 
	 * width and height.
	 * <p>
	 * The image format is set to {@link Format#RGB8}.
	 * 
	 * @param width  The width of the image (&gt 0).
	 * @param height The height of the image (&gt 0).
	 */
	public Image(int width, int height) {
		this(width, height, Format.RGBA8, null);
	}
	
	/**
	 * Instantiates a new <code>Image</code> with the provided 
	 * width, height and format.
	 * <p>
	 * The image format cannot be null.
	 * 
	 * @param width  The width of the image (&gt 0).
	 * @param height The height of the image (&gt 0).
	 * @param format The format of the image.
	 */
	public Image(int width, int height, Format format) {
		this(width, height, format, null);
	}
	
	/**
	 * Instantiates a new <code>Image</code> with the provided 
	 * width, height. 
	 * The internal buffer of the image copies the pixel data from the provided
	 * byte buffer. 
	 * <p>
	 * The image format is set to {@link Format#RGB8}.
	 * 
	 * @param width  The width of the image (&gt 0).
	 * @param height The height of the image (&gt 0).
	 * @param buffer The byte buffer to copy the pixel values from.
	 */
	public Image(int width, int height, ByteBuffer buffer) {
		this(width, height, Format.RGBA8, buffer);
	}
	
	/**
	 * Instantiates a new <code>Image</code> with the provided 
	 * width, height and format.
	 * The internal buffer of the image copies the pixel data from the provided
	 * byte buffer. 
	 * <p>
	 * The image format cannot be null.
	 * 
	 * @param width  The width of the image (&gt 0).
	 * @param height The height of the image (&gt 0).
	 * @param format The format of the image.
	 * @param buffer The byte buffer to copy the pixel values from.
	 */
	public Image(int width, int height, Format format, ByteBuffer buffer) {
		Validator.positive(width);
		Validator.positive(height);
		Validator.nonNull(format);
		
		this.width = width;
		this.height = height;
		this.format = format;
		
		this.data = BufferUtils.createByteBuffer(sizeInPixel() * Float.BYTES);
		if(buffer != null) {
			fromByteBuffer(buffer);
		}
	}
	
	/**
	 * Copy the data from the provided byte buffer and set it as the
	 * <code>Image</code> data.
	 * <p>
	 * The buffer to copy from cannot be null.
	 * 
	 * @param buffer The buffer to copy from.
	 * @return		 The image with its updated data.
	 */
	public Image fromByteBuffer(ByteBuffer buffer) {
		Validator.nonNull(buffer, "The buffer to copy from cannot be null!");
		
		for (int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				
				int start = 4 * (y * width + x);

                float r = (buffer.get(start) & 0xff) / 255f;
                float g = (buffer.get(start + 1) & 0xff) / 255f;
                float b = (buffer.get(start + 2) & 0xff) / 255f;
                float a = (buffer.get(start + 3) & 0xff) / 255f;
                
                setPixel(x, y, new Color(r, g, b, a));
			}
		}
		return this;
	}
	
	/**
	 * Retrieves the <code>Image</code> data and fill the store 
	 * {@link ByteBuffer} with it.
	 * 
	 * @param store The store buffer to fill.
	 * @return		The buffer filled with pixel data.
	 */
	public ByteBuffer toByteBuffer(ByteBuffer store) {
		Validator.nonNull(store, "The buffer store cannot be null!");
		
		Color color = MercuryMath.LOCAL_VARS.acquireNext(Color.class);
		int index = 0;

		for (int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
           
				getPixel(x, y, color);

                int r = (int) (color.r * 255f);
                int g = (int) (color.g * 255f);
                int b = (int) (color.b * 255f);
                int a = (int) (color.a * 255f);

                store.put(index++, (byte) r)
                	 .put(index++, (byte) g)
                	 .put(index++, (byte) b)
                	 .put(index++, (byte) a);
            }
        }
		return store;
	}
	
	/**
	 * Retrieves the pixel's color at the specified coordinates for this 
	 * <code>Image</code> and stores the value into the provided {@link Color} instance.
	 * 
	 * @param x 	The x coordinate of the pixel.
	 * @param y		The y coordinate of the pixel.
	 * @param store The store to put the pixel color into.
	 * @return		The filled store color.
	 */
	public Color getPixel(int x, int y, Color store) {
		Validator.nonNull(store, "The pixel color store cannot be null!");
  
		int start = 4 * (width * y + x);
	        
		store.r = data.getFloat(start * Float.BYTES);
		store.g = data.getFloat((start + 1) * Float.BYTES);
		store.b = data.getFloat((start + 2) * Float.BYTES);		
		store.a = data.getFloat((start + 3) * Float.BYTES);

		return store;
	}
	
	/**
	 * Search the corresponding pixel of the <code>Image</code> according 
	 * to the provided coordinates and set its color to the given one.
	 * 
	 * @param x 		 The x coordinate of the pixel.
	 * @param y			 The y coordinate of the pixel.
	 * @param pixelColor The color of the pixel.
	 * @return			 The image with the updated pixel color.
	 */
	public Image setPixel(int x, int y, Color pixelColor) {
		Validator.nonNull(pixelColor, "The pixel color cannot be null!");
		
		int start = 4 * (width * y + x);

        data.putFloat((start + 0) * Float.BYTES, pixelColor.r)
        	.putFloat((start + 1) * Float.BYTES, pixelColor.g)
        	.putFloat((start + 2) * Float.BYTES, pixelColor.b)
        	.putFloat((start + 3) * Float.BYTES, pixelColor.a);
        
        return this;
	}
	
	/**
	 * Return the size in pixels of the <code>Image</code>.
	 * (<i>&rarr; width * height * 4</i>)
	 * 
	 * @return The pixel size.
	 */
	public int sizeInPixel() {
		return width * height * 4;
	}
	
	/**
	 * Return the width of the <code>Image</code>.
	 * 
	 * @return The width of the image.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Return the height of the <code>Image</code>.
	 * 
	 * @return The height of the image.
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Return the data of the <code>Image</code>.
	 * 
	 * @return The image data as a byte buffer.
	 */
	public ByteBuffer getData() {
		return data;
	}
	
	/**
	 * Return the format of the <code>Image</code>.
	 * 
	 * @return The format of the image.
	 */
	public Format getFormat() {
		return format;
	}
	
	/**
	 * <code>Format</code> represents the format use for the <code>Texture</code> creation.
	 * <p>
	 * This can be used to describe the way the data are stored inside a <code>Texture</code>.
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
		 * Return whether the <code>Format</code> is a depth format.
		 * 
		 * @return Whether the format is a depth format.
		 */
		public boolean isDepthFormat() {
			return isDepthFormat;
		}
		
		/**
		 * Return the number of bits per pixel for the <code>Format</code>.
		 * 
		 * @return The number of bits per pixel.
		 */
		public int getBitsPerPixel() {
			return bitsPerPixel;
		}
	}
	
	/**
	 * Return the corresponding OpenGL texel data format of the 
	 * <code>Image</code> {@link Format}.
	 * 
	 * @return The OpenGL format of the image.
	 */
	public int determineFormat() {
		switch(format) {
			case RGB8:
				return GL11.GL_RGB;
			case RGBA8:
				return GL11.GL_RGBA;
			case RGB16F:
				return GL11.GL_RGB;
			case RGBA16F:
				return GL11.GL_RGBA;
			case RGB32F:
				return GL11.GL_RGB;
			case RGBA32F:
				return GL11.GL_RGBA;
			case DEPTH16:
			case DEPTH24:
			case DEPTH32:
			case DEPTH32F:
				return GL11.GL_DEPTH_COMPONENT;
			default:
				throw new UnsupportedOperationException("Unknown image format: " + format);
		}	
	}
	
	/**
	 * Return the corresponding OpenGL internal format of the 
	 * <code>Image</code> {@link Format}.
	 * 
	 * @return The OpenGL format of the image.
	 */
	public int determineInternalFormat() {
		switch(format) {
			case RGB8:
				return GL11.GL_RGB8;
			case RGBA8:
				return GL11.GL_RGBA8;
			case RGB16F:
				return GL30.GL_RGB16F;
			case RGBA16F:
				return GL30.GL_RGBA16F;
			case RGB32F:
				return GL30.GL_RGB32F;
			case RGBA32F:
				return GL30.GL_RGBA32F;
			case DEPTH16:
				return GL14.GL_DEPTH_COMPONENT16;
			case DEPTH24:
				return GL14.GL_DEPTH_COMPONENT24;
			case DEPTH32:
				return GL14.GL_DEPTH_COMPONENT32;
			case DEPTH32F:
				return GL30.GL_DEPTH_COMPONENT32F;
			default:
				throw new UnsupportedOperationException("Unknown image format: " + format);
		}	
	}
}	
