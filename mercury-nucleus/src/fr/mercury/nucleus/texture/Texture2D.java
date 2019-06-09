package fr.mercury.nucleus.texture;

/**
 * <code>Texture2D</code> is an implementation of {@link Texture} to represents
 * a texture in 2D space in OpenGL.
 * 
 * @author GnosticOccultist
 */
public class Texture2D extends Texture {
	
	/**
	 * Instantiates a new <code>Texture2D</code>.
	 * <p>
	 * To be usable in an OpenGL context, you must call {@link #upload()} to upload 
	 * it to the GPU.
	 */
	public Texture2D() {}
	
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
