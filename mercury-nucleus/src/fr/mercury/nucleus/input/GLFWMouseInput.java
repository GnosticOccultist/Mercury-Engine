package fr.mercury.nucleus.input;

import static fr.mercury.nucleus.input.Input.Buttons.BUTTON_LEFT;
import static fr.mercury.nucleus.input.Input.Buttons.BUTTON_MIDDLE;
import static fr.mercury.nucleus.input.Input.Buttons.BUTTON_RIGHT;
import static fr.mercury.nucleus.input.Input.Buttons.BUTTON_UNDEFINED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetDropCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.event.EventType;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.MercuryContext;

public final class GLFWMouseInput {
	
	private static final Logger logger = FactoryLogger.getLogger("mercury.input");
	
	/**
	 * The context handling the mouse input.
	 */
	private final MercuryContext context;
	/**
	 * The input processor to dispatch the mouse events to.
	 */
	private InputProcessor processor;
	/**
	 * The queue of mouse events to be dispatched.
	 */
	private final Queue<MouseEvent> mouseEvents = new ArrayDeque<>();
    /**
     * Whether the cursor is visible.
     */
	private boolean cursorVisible = true;
	/**
	 * The table containing the click time of each mouse button.
	 */
	private final HashMap<Integer, Long> lastClickTime = new HashMap<>();
	/**
	 * The last event which was queued up.
	 */
	private MouseEvent lastEvent = null;
	
	private GLFWCursorPosCallback cursorPosCallback;
	private GLFWMouseButtonCallback mouseButtonCallback;
	private GLFWScrollCallback scrollCallback;
	private GLFWDropCallback dropCallback;
	
	/**
	 * Instantiates a new <code>GLFWMouseInput</code> for the provided {@link MercuryContext}.
	 * 
	 * @param context The context to create the mouse input for.
	 */
	public GLFWMouseInput(MercuryContext context) {
		Validator.nonNull(context);
		this.context = context;
		
		lastClickTime.put(BUTTON_LEFT, 0L);
		lastClickTime.put(BUTTON_RIGHT, 0L);
		lastClickTime.put(BUTTON_MIDDLE, 0L);
	}
	
	/**
	 * Initialize the <code>GLFWMouseInput</code> instance by creating the needed GLFW callback to
	 * keep track of mouse's informations such as button, cursor position, scroll.
	 */
	public void initialize() {
		
		final long window = context.getWindow();
		
		glfwSetCursorPosCallback(window, cursorPosCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xPos, double yPos) {
				onMouseMoved(window, xPos, yPos);
			}
		});
		
		glfwSetMouseButtonCallback(window, mouseButtonCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				onMouseButtonPressed(window, button, action, mods);
			}
		});

        glfwSetScrollCallback(window, scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(final long window, final double xOffset, final double yOffset) {
                onWheelScroll(window, xOffset, yOffset * 120);
            }
        });
        
		glfwSetDropCallback(window, dropCallback = new GLFWDropCallback() {
			@Override
			public void invoke(long window, int count, long names) {
				final String[] files = new String[count];
				for (int i = 0; i < count; i++) {
					files[i] = getName(names, i);
				}
				// Let the input processor handle the dropped files.
				processor.dropEvent(files);
			}
		});
		
		setCursorVisible(cursorVisible);
	}
	
	/**
	 * Update the <code>GLFWMouseInput</code> by dispatching the pending {@link MouseEvent}
	 * to the registered {@link InputProcessor}.
	 */
	public void dispatch() {
		
		if(processor == null) {
			logger.warning("No input processor assigned to the " + getClass().getSimpleName());
			return;
		}
		
		// Empty the queue of pending mouse events.
		while (!mouseEvents.isEmpty()) {
            processor.mouseEvent(mouseEvents.poll());
        }
	}
	
	private void onMouseMoved(long window, double xPos, double yPos) {
        final int x = (int) Math.round(xPos);
        final int y = context.getHeight() - (int) Math.round(yPos);
        
        final int dx = lastEvent != null ? x - lastEvent.getX() : 0;
        final int dy = lastEvent != null ? y - lastEvent.getY() : 0;
        
        // Keep track of the last pressed button to allow for dragging gesture.
        final int button = lastEvent != null && (lastEvent.getType().equals(MouseEvent.MOUSE_PRESSED) 
        		|| lastEvent.getType().equals(MouseEvent.MOUSE_MOVED)) ? lastEvent.getButton() : BUTTON_UNDEFINED;
        
        final MouseEvent event = new MouseEvent(MouseEvent.MOUSE_MOVED, button, 0, x, y, dx, dy, 0);
        event.setTime(inputTime());
        queueUpEvent(event);
	}
	
	private void onMouseButtonPressed(long window, int button, int action, int mods) {
		
		final int x = lastEvent != null ? lastEvent.getX() : 0;
		final int y = lastEvent != null ? lastEvent.getY() : 0;
		
		EventType<MouseEvent> type = action == GLFW_PRESS ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_RELEASED;
		final int buttonCode = button(button);
		
		// This looks like a clicked event so queue up a mouse event of this type in addition to the pressed/released event.
		if(type.equals(MouseEvent.MOUSE_RELEASED) && System.currentTimeMillis() - lastClickTime.get(buttonCode) < 500L) {
			final MouseEvent event = new MouseEvent(MouseEvent.MOUSE_CLICKED, button(button), x, y, 0, 0);
	        event.setTime((long) (glfwGetTime() * 1_000_000_000));
	        queueUpEvent(event);
		}
		
		if(type.equals(MouseEvent.MOUSE_PRESSED)) {
			lastClickTime.put(buttonCode, System.currentTimeMillis());
		}
		
		final MouseEvent event = new MouseEvent(type, button(button), mods, x, y, 0, 0, 0);
        event.setTime(inputTime());
        queueUpEvent(event);
	}
	
	private void onWheelScroll(long window, double xOffset, double yOffset) {
		double wheelAccum = 0.0;
		wheelAccum += yOffset;
        final int dw = (int) Math.floor(wheelAccum);
        if (dw == 0) {
            return;
        }
        wheelAccum -= dw;
		
		final int x = lastEvent != null ? lastEvent.getX() : 0;
		final int y = lastEvent != null ? lastEvent.getY() : 0;
	
		// Keep track of the last pressed button.
        final int button = lastEvent != null && (lastEvent.getType().equals(MouseEvent.MOUSE_PRESSED) || lastEvent.getType().equals(MouseEvent.MOUSE_MOVED) 
        		|| lastEvent.getType().equals(MouseEvent.MOUSE_SCROLL)) ? lastEvent.getButton() : BUTTON_UNDEFINED;
		
		final MouseEvent event = new MouseEvent(MouseEvent.MOUSE_SCROLL, button(button), 0, x, y, 0, 0, dw);
        event.setTime(inputTime());
        queueUpEvent(event);
	}
	
	/**
	 * Queue up the specified {@link MouseEvent} to be dispatched on the next updating call.
	 * 
	 * @param event The mouse event to add to the dispatching queue.
	 */
	private void queueUpEvent(MouseEvent event) {
		this.mouseEvents.add(event);
        this.lastEvent = event;
	}
	
	/**
	 * Converts the provided GLFW button code into a <code>MercuryEngine</code> readable one.
	 * 
	 * @param glfwButton The GLFW button to convert.
	 * @return			 The equivalent button of the provided GLFW button code.
	 */
	private int button(int glfwButton) {
		switch (glfwButton) {
			case GLFW_MOUSE_BUTTON_LEFT:
				return BUTTON_LEFT;
			case GLFW_MOUSE_BUTTON_RIGHT:
				return BUTTON_RIGHT;
			case GLFW_MOUSE_BUTTON_MIDDLE:
				return BUTTON_MIDDLE;
			default:
				return BUTTON_UNDEFINED;
		}
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
	 * Sets whether the cursor of the mouse should be visible.
	 * <ul>
     * <li><code>true</code> makes the cursor visible and behaving normally.</li>
     * <li><code>false</code> hides and grabs the cursor, providing virtual and unlimited cursor movement.</li>
     * </ul>
	 * 
	 * @param cursorVisible Whether the cursor is visible.
	 */
	public void setCursorVisible(boolean cursorVisible) {
		this.cursorVisible = cursorVisible;
		
		if(cursorVisible) {
			glfwSetInputMode(context.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		} else {
			glfwSetInputMode(context.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		}
	}
	
	/**
	 * Sets the {@link InputProcessor} to dispatch the {@link MouseEvent} of the
	 * <code>GLFWMouseInput</code> to.
	 * 
	 * @param processor The processor which will receive input events.
	 */
	public void setProcessor(InputProcessor processor) {
		this.processor = processor;
	}
	
	/**
	 * Destroy the <code>GLFWMouseInput</code> by closing the registered GLFW callback.
	 * This function is called by the {@link MercuryContext} when the context is being destroyed.
	 */
	public void destroy() {
		cursorPosCallback.close();
		mouseButtonCallback.close();
		scrollCallback.close();
		dropCallback.close();
	}
}
