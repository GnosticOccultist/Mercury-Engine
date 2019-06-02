package fr.mercury.nucleus.texture;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
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
 * <code>Texture</code> is an implementation of {@link GLObject} which represents a 
 * texture in OpenGL. 
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
	 * Constructor instantiates a new <code>Texture</code>.
	 * This constructor is used by its sub-classes or by the 
	 * <code>TextureBuilder</code>.
	 * 
	 * @param id	The id of the texture.
	 * @param size  The size of the texture, same for the width and height.
	 */
	protected Texture() {
		this.currentState = new TextureState();
		this.toApply = new TextureState();
	}
	
	@OpenGLCall
	protected void bind() {
		if(getID() == INVALID_ID) {
			throw new GLException("The " + getClass().getSimpleName() + " isn't created yet!");
		}
		
		GL11.glBindTexture(getOpenGLType(), getID());
	}
	
	@OpenGLCall
	public void upload() {
		create();
		
		bind();
		
		uploadImage();
		
		applyParameters();
	}

	/**
	 * <code>bindToUnit</code> binds this specific <code>Texture</code> to
	 * the specified OpenGL Texture Unit.
	 * 
	 * @param unit The unit to bind this texture to.
	 */
	@OpenGLCall
	public void bindToUnit(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		bind();
	}
	
	/**
	 * <code>unbind</code> unbinds this specific <code>Texture</code> from
	 * the currently binded OpenGL Texture Unit.
	 */
	@OpenGLCall
	public void unbind() {
		GL11.glBindTexture(getOpenGLType(), 0);
	}
	
	/**
	 * Uploads the {@link Image} of the <code>Texture</code> to be usable by
	 * the OpenGL context.
	 */
	@OpenGLCall
	protected void uploadImage() {
		if(!image.isNeedUpdate()) {
			return;
		}
		
		// Don't know if it's really useful...
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
			
		ByteBuffer buffer = MemoryUtil.memAlloc(image.sizeInPixel() * Float.BYTES);
			
		GL11.glTexImage2D(getOpenGLType(), 0, image.determineInternalFormat(), image.getWidth(), 
				image.getHeight(), 0, image.determineFormat(), GL11.GL_UNSIGNED_BYTE, image.toByteBuffer(buffer));

		MemoryUtil.memFree(buffer);
			
		// FIXME: If the image is used by multiple textures, it can conflict.
		// Maybe add an atomic counter which decrements for each texture, until it reaches 0?
		image.setNeedUpdate(false);
	}
	
	/**
	 * Applies the changed parameters for the <code>Texture</code>.
	 */
	@OpenGLCall
	protected void applyParameters() {
		
		if(currentState.minFilter != toApply.minFilter) {
			GL11.glTexParameteri(getOpenGLType(), GL11.GL_TEXTURE_MIN_FILTER, toApply.determineMinFilter());
			currentState.minFilter = toApply.minFilter;
		}
		
		if(currentState.magFilter != toApply.magFilter) {
			GL11.glTexParameteri(getOpenGLType(), GL11.GL_TEXTURE_MAG_FILTER, toApply.determineMagFilter());
			currentState.magFilter = toApply.magFilter;
		}
		
		if(currentState.sWrap != toApply.sWrap) {
			GL11.glTexParameteri(getOpenGLType(), GL11.GL_TEXTURE_WRAP_S, toApply.determineSWrapMode());
			currentState.sWrap = toApply.sWrap;
		}
		
		if(currentState.tWrap != toApply.tWrap) {
			GL11.glTexParameteri(getOpenGLType(), GL11.GL_TEXTURE_WRAP_T, toApply.determineTWrapMode());
			currentState.tWrap = toApply.tWrap;
		}
		
		if(!currentState.borderColor.equals(toApply.borderColor) && (currentState.sWrap == WrapMode.CLAMP_BORDER 
				|| currentState.tWrap == WrapMode.CLAMP_BORDER)) {
			FloatBuffer buffer = MemoryUtil.memAllocFloat(4);
			buffer.put(toApply.borderColor.r).put(toApply.borderColor.g).put(toApply.borderColor.b).put(toApply.borderColor.a);
			buffer.rewind();
			
			GL11C.glTexParameterfv(getOpenGLType(), GL11C.GL_TEXTURE_BORDER_COLOR, buffer);
			
			MemoryUtil.memFree(buffer);
			currentState.borderColor = toApply.borderColor;
		}
		
		if(!toApply.isGeneratedMipMaps() && toApply.isNeedMipmaps()) {
			GL30.glGenerateMipmap(getOpenGLType());
			currentState.setGeneratedMipMaps(true);
			currentState.setNeedMipmaps(true);
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
		if(sWrap != currentState.sWrap) {
			toApply.sWrap = sWrap;
		}
		
		if(tWrap != currentState.tWrap) {
			toApply.tWrap = tWrap;
		}
		
		return (T) this;
	}
	
	/**
	 * Sets the {@link MinFilter} and {@link MagFilter} for the <code>Texture</code>.
	 * <p>
	 * For the changes to occur, {@link #upload()} needs to be invoked.
	 * 
	 * @param minFilter The minifying filter to apply to the texture.
	 * @param magFilter The magnification filter to apply to the texture.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Texture> T setFilter(MinFilter minFilter, MagFilter magFilter) {
		if(minFilter != currentState.minFilter) {
			
			toApply.minFilter = minFilter;
			// Force applying mipmapping for trilinear.
			if(minFilter.equals(MinFilter.TRILINEAR)) {
				setNeedMipMaps(true);
			}
		}
		
		if(magFilter != currentState.magFilter) {
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
	 * 
	 * @return The texture with the new mipmapping parameter.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Texture> T setNeedMipMaps(boolean mipmaps) {
		if(currentState.isNeedMipmaps() != mipmaps) {
			toApply.setNeedMipmaps(mipmaps);
			if(mipmaps) {
				toApply.setGeneratedMipMaps(false);
			}
		}
		return (T) this;
	}
	
	/**
	 * Sets the provided {@link Color} to be used for the border of the <code>Texture</code> when 
	 * the {@link WrapMode#CLAMP_BORDER} is set as the wrapping mode.
	 * The function allow also to clamp the borders and to actually display the color.
	 * 
	 * @param color		  The color to set for the border (not null).
	 * @param clampBorder Whether to clamp the border for the texture's wrap mode.
	 * 
	 * @return The texture with the new border color.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Texture> T setBorderColor(Color color, boolean clampBorder) {
		Validator.nonNull(color, "The border color can't be null!");
		if(clampBorder) {
			setWrapMode(WrapMode.CLAMP_BORDER, WrapMode.CLAMP_BORDER);
		}
		
		toApply.borderColor.set(color);
		return (T) this;
	}
	
	/**
	 * Changes the {@link Image} of the <code>Texture</code> to the specified {@link Color}.
	 * It can also adjust the size of the applied color with the given width and height.
	 * 
	 * @param color	 The color to apply inside the image.
	 * @param width	 The width of the modification.
	 * @param height The height of the modification.
	 * 
	 * @return The colored texture.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Texture> T color(Color color, int width, int height) {

		if(image == null) {
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
	 * Cleanup the object once it isn't needed anymore from the GPUand the OpenGL context.
	 * It also {@link TextureState#reset() reset} the state of the <code>Texture</code> for later utilization.
	 */
	@Override
	public void cleanup() {
		super.cleanup();
		
		// Reset the state of the texture.
		currentState.reset();
		toApply.reset();
	}
	
	/**
	 * Sets the {@link Image} contained in the <code>Texture</code>.
	 * 
	 * @param image The image contained by the texture.
	 */
	public void setImage(Image image) {
		this.image = image;
	}
	
	/**
	 * Return the {@link TextureType type} of the <code>Texture</code>.
	 * 
	 * @return The nature of this texture.
	 * @see TextureType
	 */
	protected abstract TextureType getType();
	
	/**
	 * Return the OpenGL type corresponding to the {@link TextureType} 
	 * of this <code>Texture</code>.
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
}
