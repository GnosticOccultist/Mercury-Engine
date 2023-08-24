package fr.mercury.nucleus.renderer.logic;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL31C;

import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexAttribute;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBuffer;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>RenderingTechnique</code> is an interface describing a rendering-logic for one or multiple {@link Mesh} 
 * within an updating-cycle.
 * 
 * @author GnosticOccultist
 */
public interface RenderLogic {

    /**
     * Begins the rendering process for the provided {@link PhysicaMundi}. It can be used to change 
     * the current state of the graphics API, allowing for specific rendering.
     * 
     * @param physica The physica-mundi to render on the screen.
     */
    @OpenGLCall
    void begin(PhysicaMundi physica);

    /**
     * Performs the rendering technique on the provided {@link PhysicaMundi}.
     * 
     * @param physica The physica-mundi to render on the screen.
     */
    @OpenGLCall
    void render(PhysicaMundi physica);

    /**
     * Ends the rendering process for the provided {@link PhysicaMundi}. It can be used to restore 
     * the state of the graphics API, when the rendering is finished.
     * 
     * @param physica The physica-mundi to render on the screen.
     */
    @OpenGLCall
    void end(PhysicaMundi physica);

    /**
     * Transfer the {@link VertexBuffer} to the bound {@link ShaderProgram} as {@link VertexAttribute}. 
     * The currently bound element array buffer (if any) will determine the amount of data to pass through the
     * shader program.
     * 
     * @param mesh The mesh containing the vertex data to pass (not null).
     * 
     * @see Mesh#getElementCount()
     */
    @OpenGLCall
    default void drawElements(Mesh mesh) {
        assert mesh != null;
        assert mesh.hasIndices();
        
        GL11.glDrawElements(mesh.toOpenGLMode(), mesh.getElementCount(),
                VertexBufferType.getOpenGLFormat(mesh.getIndicesFormat()), 0);
    }

    /**
     * Transfer the {@link VertexBuffer} to the bound {@link ShaderProgram} as {@link VertexAttribute}. 
     * The currently bound element array buffer (if any) will determine the amount of data to pass through the
     * shader program.
     * The mesh instance count will determine the number of instances to draw with the set of elements.
     * 
     * @param mesh The mesh containing the vertex data to pass, with an instance count greater than one (not null).
     * 
     * @see Mesh#getElementCount()
     * @see Mesh#getInstanceCount()
     */
    @OpenGLCall
    default void drawElementsInstanced(Mesh mesh) {
        assert mesh != null;
        assert mesh.hasIndices();
        assert mesh.getInstanceCount() > 1;

        GL31C.glDrawElementsInstanced(mesh.toOpenGLMode(), mesh.getElementCount(),
                VertexBufferType.getOpenGLFormat(mesh.getIndicesFormat()), 0, mesh.getInstanceCount());
    }

    /**
     * Transfer the {@link VertexBuffer} to the bound {@link ShaderProgram} as {@link VertexAttribute}. 
     * The count of vertice will determine the amount of data to pass through the shader program.
     * 
     * @param mesh The mesh containing the vertex data to pass (not null).
     * 
     * @see Mesh#getVertexCount()
     */
    @OpenGLCall
    default void drawArrays(Mesh mesh) {
        assert mesh != null;
        GL11.glDrawArrays(mesh.toOpenGLMode(), 0, mesh.getVertexCount());
    }
    
    /**
     * Transfer the {@link VertexBuffer} to the bound {@link ShaderProgram} as {@link VertexAttribute}. 
     * The count of vertice will determine the amount of data to pass through the shader program.
     * The mesh instance count will determine the number of instances to draw with the set of elements.
     * 
     * @param mesh The mesh containing the vertex data to pass, with an instance count greater than one (not null).
     * 
     * @see Mesh#getVertexCount()
     * @see Mesh#getInstanceCount()
     */
    @OpenGLCall
    default void drawArraysInstanced(Mesh mesh) {
        assert mesh != null;
        assert mesh.getInstanceCount() > 1;

        GL31C.glDrawArraysInstanced(mesh.toOpenGLMode(), 0, mesh.getVertexCount(), mesh.getInstanceCount());
    }

    /**
     * Transfer the {@link VertexBuffer} to the bound {@link ShaderProgram} as {@link VertexAttribute}. 
     * The currently bound element array buffer (if any) will determine the amount of data to pass through the
     * shader program.
     * 
     * @param mesh The mesh containing the vertex data to pass (not null).
     * 
     * @see Mesh#getElementCount()
     */
    @OpenGLCall
    default void drawRangeElements(Mesh mesh) {
        assert mesh != null;
        assert mesh.hasIndices();
        
        GL20C.glDrawRangeElements(mesh.toOpenGLMode(), 0, mesh.getElementCount(),
                mesh.getBuffer(VertexBufferType.INDEX).getData().limit(),
                VertexBufferType.getOpenGLFormat(mesh.getIndicesFormat()), 0);
    }
}
