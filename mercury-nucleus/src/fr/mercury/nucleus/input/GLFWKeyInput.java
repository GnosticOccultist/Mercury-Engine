package fr.mercury.nucleus.input;

import static org.lwjgl.glfw.GLFW.*;
import static fr.mercury.nucleus.input.Input.Keys.*;
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
				
				final KeyEvent event = new KeyEvent(type, toMercuryCode(key), '\0', mods, repeatCount);
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
	
	public int toMercuryCode(int glfwCode) {
		switch (glfwCode) {
			case GLFW_KEY_ESCAPE:
				return KEY_ESCAPE;
			case GLFW_KEY_1:
				return KEY_1;
			case GLFW_KEY_2:
				return KEY_2;
			case GLFW_KEY_3:
				return KEY_3;
			case GLFW_KEY_4:
				return KEY_4;
			case GLFW_KEY_5:
				return KEY_5;
			case GLFW_KEY_6:
				return KEY_6;
			case GLFW_KEY_7:
				return KEY_7;
			case GLFW_KEY_8:
				return KEY_8;
			case GLFW_KEY_9:
				return KEY_9;
			case GLFW_KEY_0:
				return KEY_0;
			case GLFW_KEY_MINUS:
				return KEY_MINUS;
			case GLFW_KEY_EQUAL:
				return KEY_EQUAL;
			case GLFW_KEY_BACKSPACE:
				return KEY_BACKSPACE;
			case GLFW_KEY_TAB:
				return KEY_TAB;
			case GLFW_KEY_Q:
				return KEY_Q;
			case GLFW_KEY_W:
				return KEY_W;
			case GLFW_KEY_E:
				return KEY_E;
			case GLFW_KEY_R:
				return KEY_R;
			case GLFW_KEY_T:
				return KEY_T;
			case GLFW_KEY_Y:
				return KEY_Y;
			case GLFW_KEY_U:
				return KEY_U;
			case GLFW_KEY_I:
				return KEY_I;
			case GLFW_KEY_O:
				return KEY_O;
			case GLFW_KEY_P:
				return KEY_P;
			case GLFW_KEY_LEFT_BRACKET:
				return KEY_LEFT_BRACKET;
			case GLFW_KEY_RIGHT_BRACKET:
				return KEY_RIGHT_BRACKET;
			case GLFW_KEY_ENTER:
				return KEY_ENTER;
			case GLFW_KEY_LEFT_CONTROL:
				return KEY_LEFT_CONTROL;
			case GLFW_KEY_A:
				return KEY_A;
			case GLFW_KEY_S:
				return KEY_S;
			case GLFW_KEY_D:
				return KEY_D;
			case GLFW_KEY_F:
				return KEY_F;
			case GLFW_KEY_G:
				return KEY_G;
			case GLFW_KEY_H:
				return KEY_H;
			case GLFW_KEY_J:
				return KEY_J;
			case GLFW_KEY_K:
				return KEY_K;
			case GLFW_KEY_L:
				return KEY_L;
			case GLFW_KEY_SEMICOLON:
				return KEY_SEMICOLON;
			case GLFW_KEY_APOSTROPHE:
				return KEY_APOSTROPHE;
			case GLFW_KEY_GRAVE_ACCENT:
				return KEY_GRAVE_ACCENT;
			case GLFW_KEY_LEFT_SHIFT:
				return KEY_LEFT_SHIFT;
			case GLFW_KEY_BACKSLASH:
				return KEY_BACKSLASH;
			case GLFW_KEY_Z:
				return KEY_Z;
			case GLFW_KEY_X:
				return KEY_X;
			case GLFW_KEY_C:
				return KEY_C;
			case GLFW_KEY_V:
				return KEY_V;
			case GLFW_KEY_B:
				return KEY_B;
			case GLFW_KEY_N:
				return KEY_N;
			case GLFW_KEY_M:
				return KEY_M;
			case GLFW_KEY_COMMA:
				return KEY_COMMA;
			case GLFW_KEY_PERIOD:
				return KEY_PERIOD;
			case GLFW_KEY_SLASH:
				return KEY_SLASH;
			case GLFW_KEY_RIGHT_SHIFT:
				return KEY_RIGHT_SHIFT;
			case GLFW_KEY_KP_MULTIPLY:
				return KEY_MULTIPLY;
			case GLFW_KEY_LEFT_ALT:
				return KEY_LEFT_ALT;
			case GLFW_KEY_SPACE:
				return KEY_SPACE;
			case GLFW_KEY_CAPS_LOCK:
				return KEY_CAPS_LOCK;
			case GLFW_KEY_F1:
				return KEY_F1;
			case GLFW_KEY_F2:
				return KEY_F2;
			case GLFW_KEY_F3:
				return KEY_F3;
			case GLFW_KEY_F4:
				return KEY_F4;
			case GLFW_KEY_F5:
				return KEY_F5;
			case GLFW_KEY_F6:
				return KEY_F6;
			case GLFW_KEY_F7:
				return KEY_F7;
			case GLFW_KEY_F8:
				return KEY_F8;
			case GLFW_KEY_F9:
				return KEY_F9;
			case GLFW_KEY_F10:
				return KEY_F10;
			case GLFW_KEY_NUM_LOCK:
				return KEY_NUM_LOCK;
			case GLFW_KEY_SCROLL_LOCK:
				return KEY_SCROLL_LOCK;
			case GLFW_KEY_KP_7:
				return KEY_NUMPAD_7;
			case GLFW_KEY_KP_8:
				return KEY_NUMPAD_8;
			case GLFW_KEY_KP_9:
				return KEY_NUMPAD_9;
			case GLFW_KEY_KP_SUBTRACT:
				return KEY_SUBTRACT;
			case GLFW_KEY_KP_4:
				return KEY_NUMPAD_4;
			case GLFW_KEY_KP_5:
				return KEY_NUMPAD_5;
			case GLFW_KEY_KP_6:
				return KEY_NUMPAD_6;
			case GLFW_KEY_KP_ADD:
				return KEY_ADD;
			case GLFW_KEY_KP_1:
				return KEY_NUMPAD_1;
			case GLFW_KEY_KP_2:
				return KEY_NUMPAD_2;
			case GLFW_KEY_KP_3:
				return KEY_NUMPAD_3;
			case GLFW_KEY_KP_0:
				return KEY_NUMPAD_0;
			case GLFW_KEY_KP_DECIMAL:
				return KEY_DECIMAL;
			case GLFW_KEY_F11:
				return KEY_F11;
			case GLFW_KEY_F12:
				return KEY_F12;
			case GLFW_KEY_F13:
				return KEY_F13;
			case GLFW_KEY_F14:
				return KEY_F14;
			case GLFW_KEY_F15:
				return KEY_F15;
			case GLFW_KEY_KP_ENTER:
				return KEY_NUMPAD_ENTER;
			case GLFW_KEY_RIGHT_CONTROL:
				return KEY_RIGHT_CONTROL;
			case GLFW_KEY_KP_DIVIDE:
				return KEY_NUMPAD_DIVIDE;
			case GLFW_KEY_PRINT_SCREEN:
				return KEY_PRINT_SCREEN;
			case GLFW_KEY_RIGHT_ALT:
				return KEY_RIGHT_ALT;
			case GLFW_KEY_PAUSE:
				return KEY_PAUSE;
			case GLFW_KEY_HOME:
				return KEY_HOME;
			case GLFW_KEY_UP:
				return KEY_UP;
			case GLFW_KEY_PAGE_UP:
				return KEY_PAGE_UP;
			case GLFW_KEY_LEFT:
				return KEY_LEFT;
			case GLFW_KEY_RIGHT:
				return KEY_RIGHT;
			case GLFW_KEY_END:
				return KEY_END;
			case GLFW_KEY_DOWN:
				return KEY_DOWN;
			case GLFW_KEY_PAGE_DOWN:
				return KEY_PAGE_DOWN;
			case GLFW_KEY_INSERT:
				return KEY_INSERT;
			case GLFW_KEY_DELETE:
				return KEY_DELETE;
			case GLFW_KEY_LEFT_SUPER:
				return KEY_LEFT_SUPER;
			case GLFW_KEY_RIGHT_SUPER:
				return KEY_RIGHT_SUPER;
				
			default:
				logger.warning("Unable to convert GLFW key code '" + glfwCode + 
						"' to Mercury equivalent! Returning KEY_UNKNOWN");
				return KEY_UNKNOWN;
		}
	}
}
