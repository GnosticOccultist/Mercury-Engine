package fr.mercury.nucleus.input;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

import java.util.ArrayDeque;
import java.util.Queue;

import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.event.EventType;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.MercuryContext;

public final class GLFWKeyInput {

	private static final Logger logger = FactoryLogger.getLogger("mercury.input");
	
	/**
	 * The context handling the key input.
	 */
	private final MercuryContext context;
	/**
	 * The input processor to dispatch the key events to.
	 */
	private InputProcessor processor;
	/**
	 * The queue of key events to be dispatched.
	 */
	private final Queue<KeyEvent> keyEvents = new ArrayDeque<>();
	/**
	 * The repeat event type counter.
	 */
	private int repeatCount;
	
	private GLFWKeyCallback keyCallback;
	private GLFWCharCallback charCallback;

	/**
	 * Instantiates a new <code>GLFWKeyInput</code> for the provided {@link MercuryContext}.
	 * 
	 * @param context The context to create the key input for.
	 */
	public GLFWKeyInput(MercuryContext context) {
		Validator.nonNull(context);
		this.context = context;
	}

	/**
	 * Initialize the <code>GLFWKeyInput</code> instance by creating the needed GLFW callback to
	 * keep track of keyboard informations such as key's pressed or release.
	 */
	public void initialize() {
		
		final long window = context.getWindow();
		
		glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
			
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				Validator.inRange(key, "The input key is out of range " + key, 0, GLFW_KEY_LAST);
				
				EventType<KeyEvent> type = null;
				switch (action) {
					case GLFW_PRESS:
						type = KeyEvent.KEY_PRESSED;
						break;
					case GLFW_RELEASE:
						repeatCount = 0;
						type = KeyEvent.KEY_RELEASED;
						break;
					case GLFW_REPEAT:
						repeatCount++;
						type = KeyEvent.KEY_REPEATED;
						break;
				}
				
				final KeyEvent event = new KeyEvent(type, key, '\0', mods, repeatCount);
				event.setTime(inputTime());
				queueUpEvent(event);
			}
		});
		
		glfwSetCharCallback(window, charCallback = new GLFWCharCallback() {
			
			@Override
			public void invoke(long window, int codepoint) {
				
				final char keyChar = (char) codepoint;
				
				final KeyEvent event = new KeyEvent(KeyEvent.KEY_TYPED, 0x00, keyChar, 0);
				event.setTime(inputTime());
				queueUpEvent(event);
			}
		});
	}
	
	/**
	 * Update the <code>GLFWKeyInput</code> by dispatching the pending {@link KeyEvent}
	 * to the registered {@link InputProcessor}.
	 */
	public void dispatch() {
		
		if(processor == null) {
			logger.warning("No input processor assigned to the " + getClass().getSimpleName());
			return;
		}
		
		// Empty the queue of pending mouse events.
		while (!keyEvents.isEmpty()) {
            processor.keyEvent(keyEvents.poll());
        }
	}
	
	/**
	 * Queue up the specified {@link KeyEvent} to be dispatched on the next updating call.
	 * 
	 * @param event The key event to add to the dispatching queue.
	 */
	private void queueUpEvent(KeyEvent event) {
		this.keyEvents.add(event);
	}
	
	/**
	 * Return the current input time to set to a {@link MouseEvent} in nanoseconds.
	 * 
	 * @return The current input time in nanoseconds.
	 */
	private long inputTime() {
		return (long) (glfwGetTime() * 1_000_000_000);
	}
	
	/**
	 * Sets the {@link InputProcessor} to dispatch the {@link KeyEvent} of the
	 * <code>GLFWKeyInput</code> to.
	 * 
	 * @param processor The processor which will receive input events.
	 */
	public void setProcessor(InputProcessor processor) {
		this.processor = processor;
	}
	
	/**
	 * Destroy the <code>GLFWKeyInput</code> by closing the registered GLFW callback.
	 * This function is called by the {@link MercuryContext} when the context is being destroyed.
	 */
	public void destroy() {
		keyCallback.close();
		charCallback.close();
	}
}
