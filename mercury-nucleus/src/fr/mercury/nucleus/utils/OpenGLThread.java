package fr.mercury.nucleus.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <code>OpenGLThread</code> mark a method which is called in the OpenGL-Thread.
 * So that you can call <b>safely</b> any OpenGL methods.
 * 
 * @author GnosticOccultist
 */
@Inherited
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface OpenGLThread {

}
