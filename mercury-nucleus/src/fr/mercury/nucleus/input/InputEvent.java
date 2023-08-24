package fr.mercury.nucleus.input;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.event.EventType;

/**
 * <code>InputEvent</code> is an abstract class for representing all input events.
 * 
 * @author GnosticOccultist
 */
public abstract class InputEvent {

    /**
     * The creation time of the event.
     */
    protected long time;
    /**
     * Whether the event has been consumed.
     */
    protected boolean consumed = false;
    /**
     * The type of event for the input.
     */
    protected final EventType<? extends InputEvent> type;

    /**
     * Instantiates a new <code>InputEvent</code> with the given {@link EventType}.
     * 
     * @param type The type describing the event (not null).
     */
    protected InputEvent(EventType<? extends InputEvent> type) {
        Validator.nonNull(type, "The provided input event type can't be null!");
        this.type = type;
    }

    /**
     * Return the type of the <code>InputEvent</code>.
     * 
     * @return The type of the input event.
     */
    public EventType<? extends InputEvent> getType() {
        return type;
    }

    /**
     * Return the time of occurence of this <code>InputEvent</code>.
     * 
     * @return The time of occurence of the event.
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the time of occurence of this <code>InputEvent</code>.
     * 
     * @param time The time of occurence of the event (&gt; 0).
     */
    public void setTime(long time) {
        Validator.positive(time, "The time of creation for the event must be greater than 0!");
        this.time = time;
    }

    /**
     * Return whether the <code>InputEvent</code> has been consumed meaning it's no longer valid 
     * and shouldn't be propagated anymore.
     * 
     * @return Whether the input event has been consumed.
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Consume the <code>InputEvent</code> meaning it's no longer valid and shouldn't be 
     * propagated anymore.
     */
    public void consume() {
        this.consumed = true;
    }
}
