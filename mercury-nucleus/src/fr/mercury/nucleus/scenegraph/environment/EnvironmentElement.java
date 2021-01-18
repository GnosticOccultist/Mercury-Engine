package fr.mercury.nucleus.scenegraph.environment;

import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.UniformStructure;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.environment.light.Light;

/**
 * <code>EnvironmentElement</code> is an extension of {@link UniformStructure} which can be integrated in a scenegraph
 * to alter the visual aspect of subsequent or local {@link AnimaMundi}.
 * <p>
 * Any implementations of this class can be added to an existing scenegraph using {@link AnimaMundi#addEnvironmentElement(EnvironmentElement)},
 * and be used through a {@link ShaderProgram}.
 * Some implementations examples:
 * <ul>
 * <li>{@link Fog}: used to define and compute a fog effect on anima-mundi.</li>
 * <li>{@link Light}: used to define a lighting effect on anima-mundi.</li>
 * </ul>
 * 
 * @author GnosticOccultist
 */
public interface EnvironmentElement extends UniformStructure {

    /**
     * Return whether the <code>EnvironmentElement</code> can only be specified once per {@link AnimaMundi}.
     * 
     * @return Whether this element is a singleton, meaning it can only be specified once.
     */
    default boolean isSingleton() {
        return true;
    }
}
