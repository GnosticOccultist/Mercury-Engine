package fr.mercury.nucleus.input;

import fr.alchemy.utilities.Validator;

public class BaseInputProcessor implements InputProcessor {

	/**
	 * The mouse input used by the processor.
	 */
	private final GLFWMouseInput mouseInput;
	/**
	 * The mouse input used by the processor.
	 */
	private final GLFWKeyInput keyInput;
	
	public BaseInputProcessor(GLFWMouseInput mouseInput, GLFWKeyInput keyInput) {
		Validator.nonNull(mouseInput, "The provided mouse input can't be null!");
		Validator.nonNull(keyInput, "The provided key input can't be null!");
		
		this.mouseInput = mouseInput;
		this.mouseInput.setProcessor(this);
		
		this.keyInput = keyInput;
		this.keyInput.setProcessor(this);
	}
	
	public void update() {
		
		// Dispatch the pending mouse events. 
		mouseInput.dispatch();
		
		// Dispatch the pending key events.
		keyInput.dispatch();
	}
	
	@Override
	public void mouseEvent(MouseEvent event) {
		
	}
	
	@Override
	public void keyEvent(KeyEvent event) {
		
	}

	@Override
	public void dropEvent(String[] files) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
	/**
	 * Destroy the <code>BaseInputProcessor</code>.
	 */
	public void destroy() {
		mouseInput.destroy();
		mouseInput.setProcessor(null);
	}
}
