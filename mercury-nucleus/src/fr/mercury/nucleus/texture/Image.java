package fr.mercury.nucleus.texture;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.utils.data.BufferUtils;

/**
 * <code>Image</code> is a wrapper class containing the data of a graphical
 * image. It is define with a {@link Format}, the byte data as well as the width
 * and height of the image. This class is mainly used to upload data into a
 * {@link Texture} object which will actually be usable inside a
 * {@link ShaderProgram}.
 * 
 * @author GnosticOccultist
 */
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
     * The image format.
     */
    private Format format;
    /**
     * The data of the image.
     */
    private final ByteBuffer data;
    /**
     * Whether a texture which uses the image needs to be updated.
     */
    private boolean needUpdate;

    /**
     * Instantiates a new <code>Image</code> with the provided width and height.
     * <p>
     * The image format is set to {@link Format#RGBA8}.
     * 
     * @param width  The width of the image (&gt 0).
     * @param height The height of the image (&gt 0).
     */
    public Image(int width, int height) {
        this(width, height, Format.RGBA8, null);
    }

    /**
     * Instantiates a new <code>Image</code> with the provided width, height and
     * format.
     * 
     * @param width  The width of the image (&gt 0).
     * @param height The height of the image (&gt 0).
     * @param format The format of the image (not null).
     */
    public Image(int width, int height, Format format) {
        this(width, height, format, null);
    }

    /**
     * Instantiates a new <code>Image</code> with the provided width, height. The
     * internal buffer of the image copies the pixel data from the provided byte
     * buffer.
     * <p>
     * The image format is set to {@link Format#RGBA8}.
     * 
     * @param width  The width of the image (&gt 0).
     * @param height The height of the image (&gt 0).
     * @param buffer The byte buffer to copy the pixel values from.
     */
    public Image(int width, int height, ByteBuffer buffer) {
        this(width, height, Format.RGBA8, buffer);
    }

    /**
     * Instantiates a new <code>Image</code> with the provided width, height and
     * format. The internal buffer of the image copies the pixel data from the
     * provided byte buffer.
     * 
     * @param width  The width of the image (&gt 0).
     * @param height The height of the image (&gt 0).
     * @param format The format of the image (not null).
     * @param buffer The byte buffer to copy the pixel values from.
     */
    public Image(int width, int height, Format format, ByteBuffer buffer) {
        Validator.positive(width, "The image's width must be positive!");
        Validator.positive(height, "The image's height must be positive!");
        Validator.nonNull(format, "The image's format can't be null!");

        this.width = width;
        this.height = height;
        this.format = format;

        this.data = BufferUtils.createByteBuffer(buffer.capacity());
        if (buffer != null) {
            fromByteBuffer(buffer);
        }
    }

    /**
     * Transfers the byte data of the provided {@link ByteBuffer} to the
     * <code>Image</code> buffer data.
     * 
     * @param buffer The buffer to copy from (not null).
     * @return       The image with its updated data.
     */
    public Image fromByteBuffer(ByteBuffer buffer) {
        Validator.nonNull(buffer, "The buffer to copy from cannot be null!");
        this.data.put(buffer);
        return this;
    }

    /**
     * Transfers the byte of the <code>Image</code> data to the store
     * {@link ByteBuffer} and return it.
     * 
     * @param store The store buffer to fill (not null).
     * @return      The provided buffer filled with image data.
     */
    public ByteBuffer toByteBuffer(ByteBuffer store) {
        Validator.nonNull(store, "The buffer store cannot be null!");
        store.put(data);
        return store;
    }

    /**
     * Retrieves the pixel's color at the specified coordinates for this
     * <code>Image</code> and stores the value into the provided {@link Color}
     * instance.
     * 
     * @param x     The x coordinate of the pixel.
     * @param y     The y coordinate of the pixel.
     * @param store The store to put the pixel color into (not null).
     * @return      The filled store color.
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
     * Search the corresponding pixel of the <code>Image</code> according to the
     * provided coordinates and set its color to the given one.
     * 
     * @param x          The x coordinate of the pixel.
     * @param y          The y coordinate of the pixel.
     * @param pixelColor The color of the pixel (not null).
     * @return           The image with the updated pixel color.
     */
    public Image setPixel(int x, int y, Color pixelColor) {
        Validator.nonNull(pixelColor, "The pixel color cannot be null!");

        int start = 4 * (width * y + x);

        data.putFloat((start + 0) * Float.BYTES, pixelColor.r).putFloat((start + 1) * Float.BYTES, pixelColor.g)
                .putFloat((start + 2) * Float.BYTES, pixelColor.b).putFloat((start + 3) * Float.BYTES, pixelColor.a);

        this.needUpdate = true;

        return this;
    }

    /**
     * Return the size in pixels of the <code>Image</code>. (<i>&rarr; width *
     * height * 4</i>)
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
     * Sets the format of the <code>Image</code>.
     * 
     * @param format The image's format.
     */
    public void setFormat(Format format) {
        this.format = format;
        setNeedUpdate(true);
    }

    /**
     * Return whether the <code>Image</code> data's has been changed, and needs to
     * be updated if used in the OpenGL context.
     * 
     * @return Whether the image needs to be updated.
     */
    public boolean isNeedUpdate() {
        return needUpdate;
    }

    /**
     * Sets whether the <code>Image</code> data's has been changed, and needs to be
     * updated if used in the OpenGL context.
     * 
     * @param needUpdate Whether the image needs to be updated.
     */
    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    /**
     * <code>Format</code> represents the format use for the <code>Texture</code>
     * creation.
     * <p>
     * This can be used to describe the way the data are stored inside a
     * <code>Texture</code>.
     * <p>
     * There are three basic kinds of image formats : color, depth or depth/stencil
     * with different bits depth per component (8, 16, 24, 32, etc...).
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
     * Return the corresponding OpenGL texel data format of the <code>Image</code>
     * {@link Format}.
     * 
     * @return The OpenGL format of the image.
     */
    public int determineFormat() {
        switch (format) {
        case RGB8:
        case RGB16F:
        case RGB32F:
            return GL11.GL_RGB;
        case RGBA8:
        case RGBA16F:
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
     * Return the corresponding OpenGL internal format of the <code>Image</code>
     * {@link Format}.
     * 
     * @return The OpenGL format of the image.
     */
    public int determineInternalFormat() {
        switch (format) {
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
