package fr.mercury.nucleus.input;

import fr.mercury.nucleus.application.Application;
import fr.mercury.nucleus.application.service.ApplicationService;

/**
 * <code>InputProcessor</code> is an interface allowing to receive low-level {@link InputEvent} from the current {@link Application}
 * context in order to process, manage and dispatch them in a more complex way suited to the <code>MercuryEngine</code>.
 * 
 * @author GnosticOccultist
 */
public interface InputProcessor extends ApplicationService {
	
	/**
	 * Called when the <code>InputProcessor</code> receives a new {@link MouseEvent} to be processed.
	 * 
	 * @param event The mouse event to be processed.
	 */
	void mouseEvent(MouseEvent event);
	
	/**
	 * Called when the <code>InputProcessor</code> receives a new {@link KeyEvent} to be processed.
	 * 
	 * @param event The key event to be processed.
	 */
	void keyEvent(KeyEvent event);
	
	/**
	 * Called when the <code>InputProcessor</code> receives event about dropped files on the context.
	 * 
	 * @param files The path of the files that were dropped.
	 */
	void dropEvent(String[] files);
}
