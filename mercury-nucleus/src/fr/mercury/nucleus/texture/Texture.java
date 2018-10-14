package fr.mercury.nucleus.texture;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import fr.mercury.nucleus.renderer.opengl.GLObject;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
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
	 * The width of the texture.
	 */
	protected int width;
	/**
	 * The height of the texture.
	 */
	protected int height;
	
	/**
	 * Constructor instantiates a new <code>Texture</code>.
	 * This constructor is used by its sub-classes or by the 
	 * <code>TextureBuilder</code>.
	 * 
	 * @param id	The id of the texture.
	 * @param size  The size of the texture, same for the width and height.
	 */
	protected Texture() {}
	
	@OpenGLCall
	public void upload() {
		create();
		
		bind();
	}
	
	@OpenGLCall
	protected void bind() {
		if(getID() == INVALID_ID) {
			throw new GLException("The " + getClass().getSimpleName() + " isn't created yet!");
		}
		
		GL11.glBindTexture(getOpenGLType(), getID());
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
}
