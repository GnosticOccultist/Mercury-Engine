package fr.mercury.nucleus.texture;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;

/**
 * <code>TextureType</code> represents the nature of a <code>Texture</code>, followed
 * by this OpenGL Type equivalent.
 * 
 * @author GnosticOccultist
 */
public enum TextureType {
	/**
	 * Represents a 2D <code>Texture</code>.
	 */
	TEXTURE_2D(GL_TEXTURE_2D),
	/**
	 * Represents a 2D <code>Texture</code> with multiple samples.
	 */
	TEXTURE_MULTISAMPLE(GL_TEXTURE_2D_MULTISAMPLE),
	/**
	 * Represents a 3D <code>Texture</code>.
	 */
	TEXTURE_3D(GL_TEXTURE_3D),
	/**
	 * Represents a Cube Map <code>Texture</code>.
	 */
	TEXTURE_CUBE_MAP(GL_TEXTURE_CUBE_MAP);
	
	private final int openGLType;
	
	TextureType(int openGLType) {
		this.openGLType = openGLType;
	}
	
	/**
	 * Return the OpenGL type equivalent of this <code>TextureType</code>.
	 * 
	 * @return The type of the texture in OpenGL.
	 * @see TextureType
	 */
	public int getOpenGLType() {
		return openGLType;
	}
}
