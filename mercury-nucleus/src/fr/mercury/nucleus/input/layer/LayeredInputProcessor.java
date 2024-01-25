package fr.mercury.nucleus.input.layer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.alchemy.utilities.collections.array.Array;
import fr.mercury.nucleus.application.service.AbstractApplicationService;
import fr.mercury.nucleus.input.Axis;
import fr.mercury.nucleus.input.Button;
import fr.mercury.nucleus.input.InputProcessor;
import fr.mercury.nucleus.input.InputState;
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
     * The set of active input states.
     */
    private final Set<State> activeStates = new HashSet<>();
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
     * Instantiates a new <code>LayeredInputProcessor</code> with
     * {@link InputLayer#DEFAULT} active.
     */
    public LayeredInputProcessor() {
        // Activate the input default layer.
        activate(InputLayer.DEFAULT);
    }

    /**
     * Activate the provided {@link InputLayer} for the
     * <code>LayeredInputProcessor</code>.
     * 
     * @param layer The layer to activate (not null).
     * @return Whether the layer was inactive before.
     */
    public boolean activate(InputLayer layer) {
        return activeLayers.add(layer);
    }

    /**
     * Deactivate the provided {@link InputLayer} for the
     * <code>LayeredInputProcessor</code>.
     * 
     * @param layer The layer to deactivate (not null).
     * @return Whether the layer was active before.
     */
    public boolean deactivate(InputLayer layer) {
        return activeLayers.remove(layer);
    }

    /**
     * Return whether the provided {@link InputLayer} is active in the
     * <code>LayeredInputProcessor</code>.
     * 
     * @param layer The layer to check (not null).
     * @return Whether the layer is active.
     */
    public boolean isLayerActive(InputLayer layer) {
        if (activeLayers.contains(layer)) {
            return true;
        }

        return activeLayers.stream().filter(l -> l.designateGroup() && l.group().equals(layer.group())).findAny()
                .isPresent();
    }

    /**
     * Update the <code>LayeredInputProcessor</code> and the internal time per frame
     * using the specified {@link ReadableTimer}.
     * 
     * @param timer The readable only timer (not null).
     */
    @Override
    @OpenGLCall
    public void update(ReadableTimer timer) {
        super.update(timer);

        tpf = timer.getTimePerFrame();

        for (var state : activeStates) {
            var value = getStates(state.stateValue, false).lastValue;
            state.updateValue(value);
            notifyValue(state.layer, state.lastValue);
        }
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

        if (event.getType() == MouseEvent.MOUSE_PRESSED || event.getType() == MouseEvent.MOUSE_RELEASED) {
            var button = Button.fromButtonIndex(event.getButton());
            var states = getStates(button, false);
            if (states == null) {
                return;
            }

            var value = event.getType() == MouseEvent.MOUSE_PRESSED ? 1.0 : 0.0;
            states.updateDelayed(value);
        }
    }

    @Override
    public void keyEvent(KeyEvent event) {
        var states = getStates(event.getCode(), false);
        if (states == null) {
            return;
        }

        var value = (event.getType() == KeyEvent.KEY_PRESSED || 
                event.getType() == KeyEvent.KEY_REPEATED) ? 1.0 : 0.0;

        states.updateDelayed(value);
    }

    @Override
    public void dropEvent(String[] files) {

    }

    protected void notifyValue(InputLayer layer, double value) {
        var listeners = getListeners(layer, false);
        if (listeners == null) {
            return;
        }

        listeners.trigger(layer, value);
    }
    
    protected void notifyState(InputLayer layer, InputState state) {
        var listeners = getListeners(layer, false);
        if (listeners == null) {
            return;
        }

        listeners.trigger(layer, state);
    }

    public void listenValue(InputValueListener listener, InputLayer... layers) {
        for (var layer : layers) {
            var listeners = getListeners(layer, true);
            listeners.valueListeners.add(listener);
        }
    }
    
    public void listenState(InputStateListener listener, InputLayer... layers) {
        for (var layer : layers) {
            var listeners = getListeners(layer, true);
            listeners.stateListeners.add(listener);
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
        double lastValue;

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

                state.lastValue = value;
                var inputState = InputState.fromValue(lastValue);

                listeners.trigger(state.layer, value);
                listeners.trigger(state.layer, inputState);
                
                // Only send to the first found listener.
                break;
            }
        }

        void updateDelayed(double value) {
            if (value == lastValue) {
                return;
            }

            this.lastValue = value;

            // Current state is triggered, so make it active.
            if (lastValue != 0.0) {
                activeStates.addAll(states);
            } else {
                
                for (var state : states) {
                    // Reset to an inactive state.
                    state.lastValue = 0;
                    state.lastState = InputState.OFF;
                    notifyState(state.layer, state.lastState);
                }
                
                activeStates.removeAll(states);
            }
        }

        @Override
        public String toString() {
            return states.toString();
        }
    }

    private class State {

        InputLayer layer;
        double lastValue;
        InputState lastState;
        double factor;
        Object stateValue;

        public State(InputLayer layer, double factor, Object stateValue) {
            this.layer = layer;
            this.factor = factor;
            this.stateValue = stateValue;
        }

        void updateValue(double value) {
            var scaled = value * factor;
            if (lastValue == scaled) {
                return;
            }

            this.lastValue = scaled;
            var state = InputState.fromValue(lastValue);
            updateState(state);
        }
        
        void updateState(InputState state) {
            if (lastState == state) {
                return;
            }

            this.lastState = state;
            LayeredInputProcessor.this.notifyState(layer, lastState);
        }

        @Override
        public String toString() {
            return "State[layer= " + layer + ", state= " + stateValue + ", factor= " + factor + "]";
        }
    }

    private class Listeners {

        final Array<InputValueListener> valueListeners = Array.ofType(InputValueListener.class);
        final Array<InputStateListener> stateListeners = Array.ofType(InputStateListener.class);

        void trigger(InputLayer layer, double value) {
            for (var listener : valueListeners) {
                listener.trigger(layer, value);
            }
        }
        
        void trigger(InputLayer layer, InputState state) {
            for (var listener : stateListeners) {
                listener.trigger(layer, state);
            }
        }

        @Override
        public String toString() {
            return valueListeners.toString() + "\n " + stateListeners.toString();
        }
    }

    /**
     * <code>InputValueListener</code> is an interface to implement listener for input
     * mappings translated into a numeric value.
     * 
     * @author GnosticOccultist
     */
    @FunctionalInterface
    public interface InputValueListener {

        /**
         * Trigger the <code>InputValueListener</code> for the given {@link InputLayer}, with
         * the numeric value.
         * 
         * @param layer The input layer triggered (not null).
         * @param value The new value after the input was triggered.
         */
        void trigger(InputLayer layer, double value);
    }
    
    /**
     * <code>InputStateListener</code> is an interface to implement listener for input
     * mappings translated into a trinary value state.
     * 
     * @author GnosticOccultist
     */
    @FunctionalInterface
    public interface InputStateListener {

        /**
         * Trigger the <code>InputStateListener</code> for the given {@link InputLayer}, with
         * the new state of input.
         * 
         * @param layer The input layer triggered (not null).
         * @param state The new state after the input was triggered.
         */
        void trigger(InputLayer layer, InputState state);
    }
}
