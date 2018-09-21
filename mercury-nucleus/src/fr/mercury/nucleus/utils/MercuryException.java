package fr.mercury.nucleus.utils;

/**
 * <code>MercuryException</code> is thrown when a generic fatal error has occured
 * in the <code>MercuryApplication</code>.
 * 
 * @author GnosticOccultist
 */
public class MercuryException extends RuntimeException {
	
	private static final long serialVersionUID = 6013755710892891259L;

	/**
	 * Instantiates a new <code>MercuryException</code> with the
	 * specified message to be thrown.
	 * 
	 * @param message The message to be thrown.
	 */
	public MercuryException(String message) {
		super(message);
	}
}
