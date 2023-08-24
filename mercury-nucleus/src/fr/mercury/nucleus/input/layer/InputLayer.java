package fr.mercury.nucleus.input.layer;

import java.util.Objects;

/**
 * <code>InputLayer</code> is used to represent input functions and input groups inside
 * the {@link LayeredInputProcessor}.
 * 
 * @author GnosticOccultist
 */
public final class InputLayer implements Comparable<InputLayer> {
    
    /**
     * A character to designate all functions inside a group.
     */
    public static final String ALL_FUNCTIONS = "*";
    /**
     * The default group of input layers.
     */
    public static final InputLayer DEFAULT = group("default");

    /**
     * The name of the group.
     */
    private final String group;
    /**
     * The name of the input function.
     */
    private final String function;

    /**
     * Create a new <code>InputLayer</code> describing an input function.
     * 
     * @param group    The name of the group.
     * @param function The name of the input function.
     * 
     * @return         A new input layer. 
     */
    public static InputLayer function(String group, String function) {
        return new InputLayer(group, function);
    }

    /**
     * Create a new <code>InputLayer</code> describing an entire group of functions.
     * 
     * @param group    The name of the group.
     * @param function The name of the input function.
     * 
     * @return         A new input layer. 
     */
    public static InputLayer group(String group) {
        return new InputLayer(group, ALL_FUNCTIONS);
    }

    /**
     * Internal use only. Please use {@link #function(String, String)} or {@link #group()}.
     * 
     * @param group    The name of the group.
     * @param function The name of the input function.
     */
    private InputLayer(String group, String function) {
        this.group = group;
        this.function = function;
    }

    /**
     * Return the group of the <code>InputLayer</code>.
     * 
     * @return The name of the group.
     */
    public String group() {
        return group;
    }

    /**
     * Return the function of the <code>InputLayer</code>.
     * 
     * @return The name of the function.
     */
    public String function() {
        return function;
    }

    /**
     * Return the name of the <code>InputLayer</code>.
     * 
     * @return The name of the layer.
     */
    public String name() {
        return group + ":" + function;
    }

    /**
     * Return whether the <code>InputLayer</code> designate an
     * entire group of inputs.
     *
     * @return Whether the layer designate a group.
     */
    public boolean designateGroup() {
        return group != null && !group.isEmpty() 
                && function.equals(ALL_FUNCTIONS);
    }
    
    @Override
    public int compareTo(InputLayer other) {
        var r = group.compareTo(other.group);
        if (r != 0) {
            return r;
        }
        
        return function.compareTo(other.function);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(group, function);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        var other = (InputLayer) obj;
        return group.equals(other.group) && function.equals(other.function);
    }
    
    @Override
    public String toString() {
        return "InputLayer[" + group + ":" + function + "]";
    }
}
