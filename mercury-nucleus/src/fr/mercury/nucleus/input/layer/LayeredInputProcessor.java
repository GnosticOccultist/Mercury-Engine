package fr.mercury.nucleus.input.layer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.alchemy.utilities.collections.array.Array;
import fr.mercury.nucleus.application.AbstractApplicationService;
import fr.mercury.nucleus.input.Axis;
import fr.mercury.nucleus.input.Button;
import fr.mercury.nucleus.input.InputProcessor;
import fr.mercury.nucleus.input.KeyEvent;
import fr.mercury.nucleus.input.MouseEvent;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

public class LayeredInputProcessor extends AbstractApplicationService implements InputProcessor {

    /**
     * The set of currently active layers.
     */
    private final Set<InputLayer> activeLayers = new HashSet<>();
    /**
     * The table containing listeners for each layer.
     */
    private final Map<InputLayer, Listeners> listenerMap = new HashMap<>();
    /**
     * The table containing input states.
     */
    private final Map<Object, States> statesMap = new HashMap<>();
    /**
     * The time per frame.
     */
    private volatile float tpf = 0;
    
    /**
     * Instantiates a new <code>LayeredInputProcessor</code> with {@link InputLayer#DEFAULT} active.
     */
    public LayeredInputProcessor() {
        // Activate the input default layer.
        activate(InputLayer.DEFAULT);
    }
    
    /**
     * Activate the provided {@link InputLayer} for the <code>LayeredInputProcessor</code>.
     * 
     * @param layer The layer to activate (not null).
     * @return      Whether the layer was inactive before.
     */
    public boolean activate(InputLayer layer) {
        return activeLayers.add(layer);
    }
    
    /**
     * Deactivate the provided {@link InputLayer} for the <code>LayeredInputProcessor</code>.
     * 
     * @param layer The layer to deactivate (not null).
     * @return      Whether the layer was active before.
     */
    public boolean deactivate(InputLayer layer) {
        return activeLayers.remove(layer);
    }
    
    /**
     * Return whether the provided {@link InputLayer} is active in the <code>LayeredInputProcessor</code>.
     * 
     * @param layer The layer to check (not null).
     * @return      Whether the layer is active.
     */
    public boolean isLayerActive(InputLayer layer) {
        if (activeLayers.contains(layer)) {
            return true;
        }
        
        return activeLayers.stream()
                .filter(l -> l.designateGroup() && l.group().equals(layer.group()))
                .findAny().isPresent();
    }
    
    /**
     * Update the <code>LayeredInputProcessor</code> and the internal time per frame using 
     * the specified {@link ReadableTimer}. 
     * 
     * @param timer The readable only timer (not null).
     */
    @Override
    @OpenGLCall
    public void update(ReadableTimer timer) {
        super.update(timer);
        
        tpf = timer.getTimePerFrame();
    }
    
    @Override
    public void mouseEvent(MouseEvent event) {
        if (event.getType() == MouseEvent.MOUSE_MOVED || event.getType() == MouseEvent.MOUSE_DRAGGED) {
            if (event.getDX() != 0) {
                // Notify about a X-axis mouse movement.
                update(Axis.MOUSE_X, -event.getDX() / (1024.0 * tpf));
            }
            
            if (event.getDY() != 0) {
                // Notify about a Y-axis mouse movement.
                update(Axis.MOUSE_Y, -event.getDY() / (1024.0 * tpf));
            }
        }
        
        if (event.getType() == MouseEvent.MOUSE_PRESSED || 
                event.getType() == MouseEvent.MOUSE_RELEASED) {
            var button = Button.fromButtonIndex(event.getButton());
            var states = getStates(button, false);
            if (states == null) {
                return;
            }
            
            var value = event.getType() == MouseEvent.MOUSE_PRESSED ? 1.0 : 0.0;
            states.updateValue(value);
        }
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (event.getType() == KeyEvent.KEY_PRESSED || 
                event.getType() == KeyEvent.KEY_RELEASED) {
            var states = getStates(event.getCode(), false);
            if (states == null) {
                return;
            }
            
            var value = event.getType() == KeyEvent.KEY_PRESSED ? 1.0 : 0.0;
            states.updateValue(value);
        }
    }

    @Override
    public void dropEvent(String[] files) {
        
    }
    
    protected void notify(InputLayer layer, double value) {
        var listeners = getListeners(layer, false);
        if (listeners == null) {
            return;
        }
        
        listeners.trigger(layer, value);
    }
    
    public void listen(InputListener listener, InputLayer... layers) {
        for (var layer : layers) {
            var listeners = getListeners(layer, true);
            listeners.listeners.add(listener);
        }
    }
    
    public void map(InputLayer layer, Axis axis) {
        addMapping(layer, 1.0, axis);
    }
    
    public void map(InputLayer layer, Button button) {
        addMapping(layer, 1.0, button);
    }
    
    public void map(InputLayer layer, int keyCode) {
        map(layer, 1.0, keyCode);
    }
    
    public void map(InputLayer layer, double factor, int keyCode) {
        addMapping(layer, factor, keyCode);
    }
    
    private void addMapping(InputLayer layer, double factor, Object stateValue) {
        var state = new State(layer, factor, stateValue);
        var states = getStates(stateValue, true);
        states.states.add(state);
    }
    
    private Listeners getListeners(InputLayer layer, boolean add) {
        var listeners = listenerMap.get(layer);
        if (listeners == null && add) {
            listeners = new Listeners();
            listenerMap.put(layer, listeners);
        }
        
        return listeners;
    }
    
    private States getStates(Object state, boolean add) {
        var states = statesMap.get(state);
        if (states == null && add) {
            states = new States();
            statesMap.put(state, states);
        }
        
        return states;
    }
    
    protected boolean update(Axis axis, double value) {
        var states = getStates(axis, false);
        if (states == null) {
            return false;
        }
        
        states.updateValue(value);
        return true;
    }
    
    private class States {
        
        final Array<State> states = Array.ofType(State.class);

        void updateValue(double value) {
            for (var state : states) {
                
                var layer = state.layer;
                if (!isLayerActive(layer)) {
                    continue;
                }
                
                var listeners = getListeners(state.layer, false);
                if (listeners == null) {
                    continue;
                }
                
                // Apply the factor before triggering the value.
                value *= state.factor;
                
                listeners.trigger(state.layer, value);
            }
        }
        
        @Override
        public String toString() {
            return states.toString();
        }
    }
    
    private class State {
        
        InputLayer layer;
        double factor;
        Object stateValue;
        
        public State(InputLayer layer, double factor, Object stateValue) {
            this.layer = layer;
            this.factor = factor;
            this.stateValue = stateValue;
        }
        
        @Override
        public String toString() {
            return "State[layer= " + layer + ", state= " + stateValue + ", factor= " + factor + "]";
        }
    }

    private class Listeners {
        
        final Array<InputListener> listeners = Array.ofType(InputListener.class);
        
        void trigger(InputLayer layer, double value) {
            for (var listener : listeners) {
                listener.trigger(layer, value);
            }
        }
        
        @Override
        public String toString() {
            return listeners.toString();
        }
    }
    
    /**
     * <code>InputListener</code> is an interface to implement listener for input mappings.
     * 
     * @author GnosticOccultist
     */
    @FunctionalInterface
    public interface InputListener {
        
        /**
         * Trigger the <code>InputListener</code> for the given {@link InputLayer}.
         * 
         * @param layer The input layer triggered (not null).
         * @param value The new value after the input was triggered.
         */
        void trigger(InputLayer layer, double value);
    }
}
