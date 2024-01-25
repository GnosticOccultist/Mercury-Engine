package fr.mercury.nucleus.input;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.application.Application;
import fr.mercury.nucleus.application.MercuryContext;
import fr.mercury.nucleus.application.service.AbstractApplicationService;
import fr.mercury.nucleus.application.service.ApplicationService;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>DelegateInputProcessor</code> is an {@link InputProcessor} which extends {@link AbstractApplicationService}
 * to delegate input related events to another processor.
 * This processor is used by the {@link MercuryContext} by default.
 * 
 * @author GnosticOccultist
 */
public class DelegateInputProcessor extends AbstractApplicationService implements InputProcessor {

    /**
     * The mouse input used by the processor.
     */
    private final GLFWMouseInput mouseInput;
    /**
     * The mouse input used by the processor.
     */
    private final GLFWKeyInput keyInput;
    /**
     * The delegate input processor or null if none.
     */
    private volatile InputProcessor delegate;

    /**
     * Instantiates a new <code>DelegateInputProcessor</code> with the given mouse and key
     * input.
     * 
     * @param mouseInput The mouse input (not null).
     * @param keyInput   The key input (not null).
     */
    public DelegateInputProcessor(GLFWMouseInput mouseInput, GLFWKeyInput keyInput) {
        this(mouseInput, keyInput, null);
    }
    
    /**
     * Instantiates a new <code>DelegateInputProcessor</code> with the given mouse and key
     * input. The provided {@link InputProcessor} will receive input related events.
     * 
     * @param mouseInput The mouse input (not null).
     * @param keyInput   The key input (not null).
     * @param delegate   The input processor to delegate processing to, or null.
     */
    public DelegateInputProcessor(GLFWMouseInput mouseInput, GLFWKeyInput keyInput, InputProcessor delegate) {
        Validator.nonNull(mouseInput, "The provided mouse input can't be null!");
        Validator.nonNull(keyInput, "The provided key input can't be null!");

        this.mouseInput = mouseInput;
        this.mouseInput.setProcessor(this);

        this.keyInput = keyInput;
        this.keyInput.setProcessor(this);
        
        setDelegate(delegate);
    }

    /**
     * Update the <code>DelegateInputProcessor</code> by dispatching the pending 
     * input events.
     * 
     * @param timer The readable only timer (not null).
     */
    @Override
    @OpenGLCall
    public void update(ReadableTimer timer) {
        super.update(timer);

        // Dispatch the pending mouse events.
        mouseInput.dispatch();

        // Dispatch the pending key events.
        keyInput.dispatch();
    }

    @Override
    public void mouseEvent(MouseEvent event) {
        if (delegate != null) {
            delegate.mouseEvent(event);
        }
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (delegate != null) {
            delegate.keyEvent(event);
        }
    }

    @Override
    public void dropEvent(String[] files) {
        if (delegate != null) {
            delegate.dropEvent(files);
        }
    }
    
    /**
     * Center the current cursor position on the window. This function 
     * will only work if the cursor is visible.
     * 
     * <p><b>Do not use this function</b> to implement things like camera 
     * controls and mouse grabbing mechanics.
     * If the cursor {@link #isCursorVisible() isn't visible} then its position is automatically recentered.
     * 
     * @param x The x coordinate in screen coordinates.
     * @param y The y coordinate in screen coordinates.
     * 
     * @see #isCursorVisible()
     * @see #setCursorVisible(boolean)
     */
    @OpenGLCall
    public void centerCursor() {
        this.mouseInput.centerCursor();
    }
    
    /**
     * Return whether the cursor of the mouse should be visible.
     * <ul>
     * <li><code>true</code> makes the cursor visible and behaving normally.</li>
     * <li><code>false</code> hides and grabs the cursor, providing virtual and
     * unlimited cursor movement.</li>
     * </ul>
     * 
     * @return Whether the cursor is visible.
     */
    public boolean isCursorVisible() {
        return mouseInput.isCursorVisible();
    }
    
    /**
     * Sets whether the cursor of the mouse should be visible.
     * <ul>
     * <li><code>true</code> makes the cursor visible and behaving normally.</li>
     * <li><code>false</code> hides and grabs the cursor, providing virtual and
     * unlimited cursor movement.</li>
     * </ul>
     * 
     * @param visible Whether the cursor is visible.
     */
    @OpenGLCall
    public void setCursorVisible(boolean visible) {
        this.mouseInput.setCursorVisible(visible);
    }
    
    /**
     * Return the delegated {@link InputProcessor} used by the <code>DelegateInputProcessor</code>.
     * 
     * @param <I> The type of input processor.
     * 
     * @return The delegated input processor or null if none.
     */
    @SuppressWarnings("unchecked")
    public <I extends InputProcessor> I getDelegate() {
        return (I) delegate;
    }
    
    /**
     * Sets the delegated {@link InputProcessor} used by the <code>DelegateInputProcessor</code>.
     * Note that if the provided processor is an {@link ApplicationService} it will be linked to the
     * {@link Application} which uses this <code>DelegateInputProcessor</code>.
     * 
     * @param delegate The delegated input processor or null if none.
     */
    public void setDelegate(InputProcessor delegate) {
        if (delegate != null && delegate instanceof ApplicationService) {
            application.linkService(delegate);
        }
        
        if (delegate == null && this.delegate != null && 
                this.delegate instanceof ApplicationService) {
            application.unlinkService(this.delegate);
        }
        
        this.delegate = delegate;
    }

    /**
     * Destroy the <code>DelegateInputProcessor</code>.
     */
    @Override
    public void cleanup() {
        
        delegate = null;

        mouseInput.destroy();
        mouseInput.setProcessor(null);

        keyInput.destroy();
        keyInput.setProcessor(null);
        
        super.cleanup();
    }
}
