package fr.mercury.nucleus.renderer.opengl.shader.uniform;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;

/**
 * <code>UniformField</code> marks a getter method that it can provide a {@link Uniform} to a {@link ShaderProgram}.
 * 
 * @see Uniform#register(Object, ShaderProgram)
 * 
 * @author GnosticOccultist
 */
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniformField {

    /**
     * Return the name of the described {@link Uniform}.
     * 
     * @return The name of the uniform (not null).
     */
    String name();

    /**
     * Return the {@link UniformType} of the described {@link Uniform}.
     * 
     * @return The type of the uniform (not null).
     */
    UniformType type();
}
