package fr.mercury.nucleus.renderer.opengl;

import fr.mercury.nucleus.utils.OpenGLCall;

public abstract class GLObject {
	
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
	 * Register the object inside the OpenGL context by assigning 
	 * it an ID.
	 */
	@OpenGLCall
	protected abstract void upload();
	
	/**
	 * Cleanup the object once it isn't need anymore.
	 */
	@OpenGLCall
	protected abstract void cleanup();
	
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
