package fr.mercury.nucleus.renderer.opengl.vertex;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL30;

import fr.mercury.nucleus.renderer.opengl.GLObject;
import fr.mercury.nucleus.utils.GLException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>VertexArray</code> (VAO) is an implementation of {@link GLObject}, which can contain a list of {@link VertexBuffer} 
 * associated to an attribute.  The attribute can be asssigned to a location and later, be enabled to access the stored data 
 * of said buffer and use it for the rendering process.
 * <p>
 * Before storing any vertex buffer or creating the attributes, the vertex array  needs to be {@link #upload() created and bound} 
 * to the OpenGL context, marking it as currently usable.
 * 
 * @author GnosticOccultist
 */
public final class VertexArray extends GLObject {

    /**
     * The currently bound VAO on the OpenGL context.
     */
    private static VertexArray CURRENT = null;

    /**
     * Determines if the provided {@link VertexArray} correspond to an OpenGL vertex
     * array object.
     * 
     * @param vao The vertex array to validate.
     * @return    Whether the vertex array is valid.
     */
    public static boolean valid(VertexArray vao) {
        return valid(vao.id);
    }

    /**
     * Determines if the provided {@link VertexArray} correspond to an OpenGL vertex
     * array object.
     * 
     * @param id The identifier of the native vertex array to validate.
     * @return   Whether the native vertex array is valid.
     */
    public static boolean valid(int id) {
        return GL30.glIsVertexArray(id);
    }

    /**
     * Binds the <code>VertexArray</code> to the OpenGL context, allowing it to be
     * used or updated.
     */
    @OpenGLCall
    public void bind() {
        if (CURRENT == this) {
            return;
        }

        if (getID() == INVALID_ID) {
            throw new GLException("The vertex array isn't created yet!");
        }

        GL30.glBindVertexArray(getID());
        CURRENT = this;
    }

    /**
     * Unbinds the currently bound <code>VertexArray</code> from the OpenGL context.
     * <p>
     * This methods is mainly used for proper cleaning of the <code>OpenGL</code>
     * context or to avoid errors of misbindings, because it doesn't need to be
     * called before binding a vertex array.
     * <p>
     * The method has been set static because it can be called from any
     * <code>VertexArray</code> instance, and will only unbind the lastest bind on
     * the <code>OpenGL</code> context.
     */
    @OpenGLCall
    public static void unbind() {
        GL30.glBindVertexArray(0);

        CURRENT = null;
    }

    @Override
    @OpenGLCall
    public void upload() {
        create();

        bind();
    }

    @Override
    @OpenGLCall
    public void cleanup() {
        unbind();

        super.cleanup();
    }

    @Override
    @OpenGLCall
    protected Integer acquireID() {
        return GL30.glGenVertexArrays();
    }

    @Override
    @OpenGLCall
    protected Consumer<Integer> deleteAction() {
        return GL30::glDeleteVertexArrays;
    }

    @Override
    @OpenGLCall
    public Runnable onDestroy(int id) {
        return () -> GL30.glDeleteVertexArrays(id);
    }
}
