package fr.mercury.nucleus.renderer.opengl;

import java.util.function.Consumer;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexArray;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>GLObject</code> represents an abstraction layer for every OpenGL objects 
 * such as {@link ShaderProgram}, {@link ShaderSource}, {@link GLBuffer}, {@link VertexArray}...
 * <p>
 * It contains the ID of the object attributed by the OpenGL context, since it's 
 * a common point to every <code>GLObjects</code>, it just defines the function 
 * to create and destroy the object.
 * <p>
 * Once every parameters for an object are set and you want to use it, you can simply
 * call {@link #upload()} to send the data to the GPU.
 * 
 * @author GnosticOccultist
 */
public abstract class GLObject {
	
	/**
	 * The logger of the OpenGL context.
	 */
	protected static final Logger logger = FactoryLogger.getLogger("mercury.opengl");
	
	/*
	 * The invalid ID for an object, usually if the object
	 * hasn't been registered by the OpenGL context.
	 */
	public static final int INVALID_ID = -1;
	
	/**
	 * The ID assigned to the object by the OpenGL context.
	 * If the object isn't created yet it will be {@value #INVALID_ID}. 
	 */
	protected int id = INVALID_ID;
	
	/**
	 * Upload the object to the GPU using the OpenGL context.
	 */
	@OpenGLCall
	protected abstract void upload();
	
	/**
	 * Create the object by assigning it an ID using the
	 * OpenGL context.
	 * <p>
	 * Should be leaved untouched, use {@link #acquireID()} to return 
	 * the appropriate ID from the OpenGL context.
	 */
	@OpenGLCall
	protected void create() {
		if(id == INVALID_ID) {
			var id = acquireID();
			if(id == 0) {
				throw new GLException("Failed to create " + 
						getClass().getSimpleName() + "!");
			}
			
			setID(id);
		}
	}
	
	/**
	 * Acquire an ID for the object using the OpenGL context.
	 * 
	 * @return
	 */
	@OpenGLCall
	protected abstract Integer acquireID();
	
	/**
	 * Cleanup the object once it isn't needed anymore from the GPU
	 * and the OpenGL context.
	 * <p>
	 * Should be leaved untouched, use {@link #deleteAction()} to return 
	 * the appropriate action for destroying the object from the OpenGL context.
	 */
	@OpenGLCall
	public void cleanup() {
		if(getID() == INVALID_ID) {
			logger.error(getClass().getSimpleName() + 
					" not yet uploaded to GPU, cannot delete.");
			return;
		}
		
		deleteAction().accept(getID());
		
		setID(-1);
	}
	
	/**
	 * Return the deleting action for the object.
	 * 
	 * @return The deleting action.
	 */
	@OpenGLCall
	protected abstract Consumer<Integer> deleteAction();
	
	/**
	 * Return the ID of the object assigned by the OpenGL context.
	 * 
	 * @return The object's ID.
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Set the ID of the object, usually assigned by the OpenGL contex
	 * so you won't need to call this method.
	 * 
	 * @param id The object's ID.
	 */
	public void setID(int id) {
		this.id = id;
	}
}
