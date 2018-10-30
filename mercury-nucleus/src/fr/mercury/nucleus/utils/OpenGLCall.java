package fr.mercury.nucleus.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <code>OpenGLCall</code> means that the function contains OpenGL calls and 
 * its invokation should be limited to prevent GPU overload and OpenGL state changes.
 * <p>
 * It can also means that you can call OpenGL methods safely inside this function.
 * 
 * @author GnosticOccultist
 */
@Inherited
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface OpenGLCall {

}
