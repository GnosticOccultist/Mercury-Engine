package fr.mercury.nucleus.utils;

/**
 * <code>GLException</code> is throw when an OpenGL context error has occured in
 * the <code>MercuryApplication</code>.
 * 
 * @author GnosticOccultist
 */
public class GLException extends MercuryException {

	private static final long serialVersionUID = 5843394016727903288L;

	public GLException(String message) {
		super(message);
	}

}
