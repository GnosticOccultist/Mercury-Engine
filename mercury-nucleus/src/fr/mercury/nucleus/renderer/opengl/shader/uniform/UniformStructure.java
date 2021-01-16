package fr.mercury.nucleus.renderer.opengl.shader.uniform;

import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;

/**
 * <code>UniformStructure</code> is an interface to implement a structure object, which can supply
 * multiple {@link Uniform} through {@link ShaderProgram} using a compact structure.
 * <p>
 * Such structure can be described as follow through GLSL shader code:
 * <code>
 * <ul>
 * struct SomeStruct<br>
 * { <ul>
 *  vec3 first;<br>
 *  vec4 second;<br>
 *  mat4 third;<br>
 * </ul>}
 * </ul>
 * </code>
 * And called as a generic uniform like this:
 * <p><code><ul>uniform SomeStruct uniform_structure;
 * <ul>...</ul>
 * vec3 store = uniform_structure.first;
 * </ul></code></p>
 * 
 * @author GnosticOccultist
 */
public interface UniformStructure {

    /**
     * Sets the uniforms of the <code>UniformStructure</code>, to the provided
     * {@link ShaderProgram}.
     * 
     * @param program The shader program in which the uniforms must be added.
     */
    void uniforms(ShaderProgram program);

    /**
     * Return the name of the <code>UniformStructure</code>.
     * 
     * @return The name of the uniform structure.
     */
    String name();
}
