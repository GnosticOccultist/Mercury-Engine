package fr.mercury.nucleus.texture;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.function.Consumer;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.renderer.opengl.GLObject;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>Texture</code> is an implementation of {@link GLObject} which
 * represents a texture in OpenGL.
 * <p>
 * It is defined by a width and a height and can be bound to a specific unit to
 * be used in a {@link ShaderProgram}.
 * 
 * @author GnosticOccultist
 */
public abstract class Texture extends GLObject {

    /**
     * The contained image of the texture.
     */
    protected Image image;
    /**
     * The currently used texture state.
     */
    protected TextureState currentState;
    /**
     * The state with changes to apply.
     */
    protected TextureState toApply;

    /**
     * Determines if the provided ID correspond to an OpenGL <code>Texture</code>.
     * 
     * @param id The ID of the texture to check.
     * @return Whether the ID correspond to a texture.
     */
    public static boolean valid(int id) {
        return GL30.glIsTexture(id);
    }

    /**
     * Unbinds the currently bound <code>Texture</code> from the OpenGL context.
     * <p>
     * The methods is mainly used for proper cleaning of the OpenGL context or to
     * avoid errors of misbindings, because it doesn't need to be called before
     * binding a new texture.
     * <p>
     * The method has been set static because it can be called from any
     * <code>Texture</code> instance, and will only unbind the lastest bind on the
     * <code>OpenGL</code> context matching the provided {@link TextureType}.
     * 
     * @param type The texture type to unbind from the context (not null).
     */
    public static void unbind(TextureType type) {
        Validator.nonNull(type, "The texture type can't be null!");
        GL15.glBindTexture(Texture.getOpenGLType(type), 0);
    }

    /**
     * Instantiates a new <code>Texture</code> with no {@link Image} data defined.
     * Use the {@link #setImage(Image)} to add an image data to the texture.
     */
    protected Texture() {
        this.currentState = new TextureState();
        this.toApply = new TextureState();
    }

    /**
     * Binds the <code>Texture</code> to the OpenGL context, allowing it to be used
     * or updated.
     * <p>
     * Note that there is only one bound buffer per OpenGL {@link TextureType}.
     */
    @OpenGLCall
    protected void bind() {
        if (getID() == INVALID_ID) {
            throw new GLException("The " + getClass().getSimpleName() + " isn't created yet!");
        }

        GL11.glBindTexture(getOpenGLType(), getID());
    }

    @Override
    @OpenGLCall
    public void upload() {
        create();

        bind();

        uploadImage();

        applyParameters();
    }

    /**
     * <code>bindToUnit</code> binds this specific <code>Texture</code> to the
     * specified OpenGL Texture Unit.
     * 
     * @param unit The unit to bind this texture to.
     */
    @OpenGLCall
    public void bindToUnit(int unit) {
        bind();
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
    }

    /**
     * Unbinds the currently bound <code>Texture</code> from the OpenGL context.
     * <p>
     * This methods is mainly used for proper cleaning of the OpenGL context or to
     * avoid errors of misbindings, because it doesn't need to be called before
     * binding a new texture. Note that it works even if the currently bound texture
     * isn't the one invoking this method.
     */
    @OpenGLCall
    public void unbind() {
        GL11.glBindTexture(getOpenGLType(), 0);
    }

    /**
     * Uploads the {@link Image} of the <code>Texture</code> to be usable by the
     * OpenGL context.
     */
    @OpenGLCall
    protected void uploadImage() {
        if (!image.isNeedUpdate()) {
            return;
        }

        // Don't know if it's really useful...
        GL11C.glPixelStorei(GL11C.GL_UNPACK_ALIGNMENT, 1);

        // Prepare image buffer for reading.
        ByteBuffer buffer = image.getData();
        buffer.rewind();

        if (image.hasMipmaps()) {

            var pos = 0;
            for (var level = 0; level < image.mipmapsCount(); ++level) {
                var width = Math.max(1, image.getWidth() >> level);
                var height = Math.max(1, image.getHeight() >> level);
                var size = image.getMipmapSize(level);

                buffer.position(pos);
                buffer.limit(pos + size);

                GL11C.glTexImage2D(getOpenGLType(), level, image.determineInternalFormat(), width, height, 0,
                        image.determineFormat(), image.determineDataType(), buffer);

                pos += size;
            }

        } else {
            GL11C.glTexImage2D(getOpenGLType(), 0, image.determineInternalFormat(), image.getWidth(), image.getHeight(),
                    0, image.determineFormat(), image.determineDataType(), buffer);
        }

        image.setNeedUpdate(false);
    }

    /**
     * Applies the changed parameters for the <code>Texture</code>.
     */
    @OpenGLCall
    protected void applyParameters() {

        if (currentState.minFilter != toApply.minFilter) {
            GL11C.glTexParameteri(getOpenGLType(), GL11.GL_TEXTURE_MIN_FILTER, toApply.determineMinFilter());
            currentState.minFilter = toApply.minFilter;
        }

        if (currentState.magFilter != toApply.magFilter) {
            GL11C.glTexParameteri(getOpenGLType(), GL11.GL_TEXTURE_MAG_FILTER, toApply.determineMagFilter());
            currentState.magFilter = toApply.magFilter;
        }

        if (currentState.sWrap != toApply.sWrap) {
            GL11C.glTexParameteri(getOpenGLType(), GL11.GL_TEXTURE_WRAP_S, toApply.determineSWrapMode());
            currentState.sWrap = toApply.sWrap;
        }

        if (currentState.tWrap != toApply.tWrap) {
            GL11C.glTexParameteri(getOpenGLType(), GL11.GL_TEXTURE_WRAP_T, toApply.determineTWrapMode());
            currentState.tWrap = toApply.tWrap;
        }

        if (!currentState.borderColor.equals(toApply.borderColor)
                && (currentState.sWrap == WrapMode.CLAMP_BORDER || currentState.tWrap == WrapMode.CLAMP_BORDER)) {
            FloatBuffer buffer = MemoryUtil.memAllocFloat(4);
            buffer.put(toApply.borderColor.r).put(toApply.borderColor.g).put(toApply.borderColor.b)
                    .put(toApply.borderColor.a);
            buffer.rewind();

            GL11C.glTexParameterfv(getOpenGLType(), GL11C.GL_TEXTURE_BORDER_COLOR, buffer);

            MemoryUtil.memFree(buffer);
            currentState.borderColor = toApply.borderColor;
        }

        if (currentState.anisotropicFilter != toApply.anisotropicFilter) {
            GL11C.glTexParameterf(getOpenGLType(), EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                    toApply.anisotropicFilter);
            currentState.setAnisotropicFilter(toApply.anisotropicFilter);
        }

        if (!toApply.isGeneratedMipMaps() && toApply.isNeedMipmaps() && !image.hasMipmaps()) {
            GL30C.glGenerateMipmap(getOpenGLType());
            currentState.setGeneratedMipMaps(true);
            currentState.setNeedMipmaps(true);
        }

        if (image.hasMipmaps() || currentState.isGeneratedMipMaps()) {
            if (currentState.maxLevel != toApply.maxLevel) {
                var maxLevel = toApply.maxLevel == -1 ? image.mipmapsCount() - 1 : toApply.maxLevel;
                GL11C.glTexParameteri(getOpenGLType(), GL12C.GL_TEXTURE_MAX_LEVEL, maxLevel);
                currentState.maxLevel = toApply.maxLevel;
            }

            if (currentState.baseLevel != toApply.baseLevel) {
                GL11C.glTexParameteri(getOpenGLType(), GL12C.GL_TEXTURE_BASE_LEVEL, toApply.baseLevel);
                currentState.baseLevel = toApply.baseLevel;
            }
        }
    }

    /**
     * Sets the {@link WrapMode} for the S et T axis of the <code>Texture</code>.
     * <p>
     * For the changes to occur, {@link #upload()} needs to be invoked.
     * 
     * @param sWrap The wrap mode for the S-axis.
     * @param tWrap The wrap mode for the T-axis.
     */
    @SuppressWarnings("unchecked")
    public <T extends Texture> T setWrapMode(WrapMode sWrap, WrapMode tWrap) {
        if (sWrap != currentState.sWrap) {
            toApply.sWrap = sWrap;
        }

        if (tWrap != currentState.tWrap) {
            toApply.tWrap = tWrap;
        }

        return (T) this;
    }

    /**
     * Sets the {@link MinFilter} and {@link MagFilter} for the
     * <code>Texture</code>.
     * <p>
     * For the changes to occur, {@link #upload()} needs to be invoked.
     * 
     * @param minFilter The minifying filter to apply to the texture.
     * @param magFilter The magnification filter to apply to the texture.
     */
    @SuppressWarnings("unchecked")
    public <T extends Texture> T setFilter(MinFilter minFilter, MagFilter magFilter) {
        if (minFilter != currentState.minFilter) {

            toApply.minFilter = minFilter;
            // Force applying mipmapping for trilinear.
            if (minFilter.equals(MinFilter.TRILINEAR)) {
                setNeedMipMaps(true);
            }
        }

        if (magFilter != currentState.magFilter) {
            toApply.magFilter = magFilter;
        }

        return (T) this;
    }

    /**
     * Notify that the <code>Texture</code> needs to generate mipmaps.
     * <p>
     * The generation of the mipmaps will occur when invoking {@link #upload()}.
     * 
     * @param mipmaps Whether the texture needs mipmapping.
     * @return The texture with the new mipmapping parameter.
     */
    @SuppressWarnings("unchecked")
    public <T extends Texture> T setNeedMipMaps(boolean mipmaps) {
        if (currentState.isNeedMipmaps() != mipmaps) {
            toApply.setNeedMipmaps(mipmaps);
            if (mipmaps) {
                toApply.setGeneratedMipMaps(false);
            }
        }
        return (T) this;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Texture> T setMaxLevel(int maxLevel) {
        Validator.inRange(maxLevel, -1, image.mipmapsCount() - 1);
        toApply.maxLevel = maxLevel;

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Texture> T setBaseLevel(int baseLevel) {
        Validator.inRange(baseLevel, 0, image.mipmapsCount() - 1);
        toApply.baseLevel = baseLevel;

        return (T) this;
    }

    /**
     * Sets the provided {@link Color} to be used for the border of the
     * <code>Texture</code> when the {@link WrapMode#CLAMP_BORDER} is set as the
     * wrapping mode. The function allow also to clamp the borders and to actually
     * display the color.
     * 
     * @param color       The color to set for the border (not null).
     * @param clampBorder Whether to clamp the border for the texture's wrap mode.
     * @return The texture with the new border color.
     */
    @SuppressWarnings("unchecked")
    public <T extends Texture> T setBorderColor(Color color, boolean clampBorder) {
        Validator.nonNull(color, "The border color can't be null!");
        if (clampBorder) {
            setWrapMode(WrapMode.CLAMP_BORDER, WrapMode.CLAMP_BORDER);
        }

        toApply.borderColor.set(color);
        return (T) this;
    }

    /**
     * Changes the {@link Image} of the <code>Texture</code> to the specified
     * {@link Color}. It can also adjust the size of the applied color with the
     * given width and height.
     * 
     * @param color  The color to apply inside the image.
     * @param width  The width of the modification.
     * @param height The height of the modification.
     * @return The colored texture.
     */
    @SuppressWarnings("unchecked")
    public <T extends Texture> T color(Color color, int width, int height) {

        if (image == null) {
            image = new Image(width, height);
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setPixel(x, y, color);
            }
        }

        return (T) this;
    }

    /**
     * Cleanup the object once it isn't needed anymore from the GPU and the OpenGL
     * context. It also {@link TextureState#reset() reset} the state of the
     * <code>Texture</code> for later utilization.
     */
    @Override
    @OpenGLCall
    public void cleanup() {
        super.cleanup();

        // Reset the state of the texture.
        currentState.reset();
    }

    @Override
    protected void restart() {
        this.image.setNeedUpdate(true);

        super.restart();
    }

    /**
     * Returns the {@link Image} contained in the <code>Texture</code>.
     * 
     * @param image The image contained by the texture.
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the {@link Image} contained in the <code>Texture</code>.
     * 
     * @param image The image contained by the texture.
     */
    public void setImage(Image image) {
        this.image = image;
        this.image.setNeedUpdate(true);
    }

    /**
     * Sets the current {@link TextureState} and the one to be applied to the
     * <code>Texture</code>. This method should only be used for copying purposes.
     * 
     * @param current The current texture state (not null).
     * @param toApply The texture state to be applied on next upload call (not
     *                null).
     */
    protected void setTextureState(TextureState current, TextureState toApply) {
        this.currentState = new TextureState(current);
        this.toApply = new TextureState(toApply);
    }

    /**
     * Creates and return a copy of the <code>Texture</code>'s implementation. Note
     * that the {@link Image} isn't copied an alias is being created.
     * <p>
     * To be usable in an OpenGL context, you must call {@link #upload()} to upload
     * it to the GPU.
     * 
     * @return A copy of the texture, not yet uploaded (not null).
     */
    public abstract Texture copy();

    /**
     * Return the {@link TextureType type} of the <code>Texture</code>.
     * 
     * @return The nature of this texture.
     * @see TextureType
     */
    protected abstract TextureType getType();

    /**
     * Return the OpenGL type corresponding to the {@link TextureType} of this
     * <code>Texture</code>.
     * 
     * @return The OpenGL type of texture.
     */
    public int getOpenGLType() {
        return getType().getOpenGLType();
    }

    @Override
    @OpenGLCall
    protected Integer acquireID() {
        return GL11.glGenTextures();
    }

    @Override
    @OpenGLCall
    protected Consumer<Integer> deleteAction() {
        return GL11::glDeleteTextures;
    }

    @Override
    @OpenGLCall
    public Runnable onDestroy(int id) {
        return () -> GL11.glDeleteTextures(id);
    }

    /**
     * Return the <code>Texture</code> equivalent texture type as an int
     * corresponding to the provided enum value of the {@link TextureType}.
     * 
     * @return The type of texture (not null).
     */
    public static int getOpenGLType(TextureType type) {
        Validator.nonNull(type, "The texture type to convert can't be null!");

        switch (type) {
        case TEXTURE_2D:
            return GL11.GL_TEXTURE_2D;
        case TEXTURE_MULTISAMPLE:
            return GL32.GL_TEXTURE_2D_MULTISAMPLE;
        case TEXTURE_3D:
            return GL11.GL_TEXTURE_2D;
        case TEXTURE_CUBE_MAP:
            return GL13.GL_TEXTURE_CUBE_MAP;
        default:
            throw new UnsupportedOperationException(
                    "Cannot convert the texture type: " + type + " to an OpenGL equivalent!");
        }
    }
}
