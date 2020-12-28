package fr.mercury.nucleus.texture;

/**
 * <code>Texture2D</code> is an implementation of {@link Texture} to represents
 * a texture in 2D space in OpenGL.
 * 
 * @author GnosticOccultist
 */
public class Texture2D extends Texture {
	
	/**
	 * Instantiates a new <code>Texture2D</code> with no {@link Image} data defined.
	 * Use the {@link #setImage(Image)} to add an image data to the texture.
	 * <p>
	 * To be usable in an OpenGL context, you must call {@link #upload()} to upload 
	 * it to the GPU.
	 */
	public Texture2D() {}
	
	/**
	 * Creates and return a copy of the <code>Texture2D</code>'s implementation. Note that the 
	 * {@link Image} isn't copied an alias is being created.
	 * <p>
	 * To be usable in an OpenGL context, you must call {@link #upload()} to upload 
	 * it to the GPU.
	 * 
	 * @return A copy of the texture, not yet uploaded (not null).
	 */
	@Override
	public Texture2D copy() {
		var copy = new Texture2D();
		copy.setTextureState(currentState, toApply);
		copy.setImage(image);
		
		return copy;
	}
	
	/**
	 * Return the {@link TextureType type} of the <code>Texture2D</code>:
	 * {@link TextureType#TEXTURE_2D}.
	 * 
	 * @return The 2D texture type.
	 */
	@Override
	protected TextureType getType() {
		return TextureType.TEXTURE_2D;
	}
}
