package fr.mercury.nucleus.renderer.opengl;

import java.util.function.Consumer;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.alchemy.utilities.logging.LoggerLevel;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexArray;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.gc.NativeObject;
import fr.mercury.nucleus.utils.gc.NativeObjectCleaner;

/**
 * <code>GLObject</code> represents an abstraction layer for every OpenGL objects such as {@link ShaderProgram}, {@link ShaderSource},
 * {@link GLBuffer}, {@link VertexArray}...
 * <p>
 * It contains the ID of the object attributed by the OpenGL context, since it's a common point to every <code>GLObjects</code>, 
 * it just defines the function to create and destroy the object.
 * <p>
 * Once every parameters for an object are set and you want to use it, you can simply call {@link #upload()} to send the data to the 
 * GPU. The renderer will also lazily upload the data to the GPU when it does need it.
 * 
 * @author GnosticOccultist
 */
public abstract class GLObject extends NativeObject implements Comparable<GLObject> {

    /**
     * The logger of the OpenGL context.
     */
    protected static final Logger logger = FactoryLogger.getLogger("mercury.opengl");

    static {
        logger.setActive(LoggerLevel.DEBUG, true);
    }

    /*
     * The invalid ID for an object, usually if the object hasn't been registered by
     * the OpenGL context.
     */
    public static final int INVALID_ID = -1;

    /**
     * The ID assigned to the object by the OpenGL context. If the object isn't
     * created yet it will be {@value #INVALID_ID}.
     */
    protected int id = INVALID_ID;

    /**
     * Upload the object to the GPU using the OpenGL context.
     */
    @OpenGLCall
    protected abstract void upload();

    /**
     * Create the <code>GLObject</code> by assigning it the ID of a native reference
     * using the OpenGL context.
     * <p>
     * Should be leaved untouched, use {@link #acquireID()} to return the
     * appropriate ID from the OpenGL context.
     * <p>
     * The method is also registering the object to the {@link NativeObjectCleaner}
     * to be later destroyed when no longer needed.
     */
    @OpenGLCall
    protected boolean create() {
        var newNative = id == INVALID_ID;

        if (newNative) {
            var id = acquireID();
            if (id == 0) {
                throw new GLException("Failed to create " + getClass().getSimpleName() + "!");
            }

            setID(id);
            onAssigned(id);
        }

        return newNative;
    }

    /**
     * Acquire an ID for the object using the OpenGL context.
     * 
     * @return A new unique identifier for the object.
     */
    @OpenGLCall
    protected abstract Integer acquireID();

    /**
     * Cleanup the object once it isn't needed anymore from the GPU and the OpenGL
     * context.
     * <p>
     * Should be leaved untouched, use {@link #deleteAction()} to return the
     * appropriate action for destroying the object from the OpenGL context.
     */
    @Override
    @OpenGLCall
    public void cleanup() {
        if (id == INVALID_ID) {
            logger.error(getClass().getSimpleName() + " not yet uploaded to GPU, cannot delete.");
            return;
        }

        deleteAction().accept(id);

        logger.debug("Cleanup " + this + ".");

        setID(INVALID_ID);
    }

    @Override
    @OpenGLCall
    protected void restart() {
        super.restart();

        create();
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
     * Set the ID of the object, usually assigned by the OpenGL context so you won't
     * need to call this method.
     * 
     * @param id The object's ID.
     */
    protected void setID(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(GLObject other) {
        return Integer.compare(id, other.id);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj != null && getClass().equals(obj.getClass())) {
            return compareTo((GLObject) obj) == 0;
        }

        return false;
    }
}
