package fr.mercury.nucleus.renderer;

import java.util.Collection;
import java.util.EnumMap;

import fr.alchemy.utilities.collections.array.Array;
import fr.mercury.nucleus.renderer.logic.state.RenderState;

public class RenderStateMachine {

    /**
     * The renderer to apply the state.
     */
    private final AbstractRenderer renderer;
    /**
     * The map containing the render states.
     */
    protected final EnumMap<RenderState.Type, Array<RenderState>> states = new EnumMap<>(RenderState.Type.class);

    public RenderStateMachine(AbstractRenderer renderer) {
        this.renderer = renderer;
    }

    public RenderStateMachine(AbstractRenderer renderer, Array<RenderState> defaultStates) {
        this(renderer);
        pushAll(defaultStates);
    }

    public RenderStateMachine withDefaultStates(RenderState... defaultStates) {
        states.clear();
        pushAll(defaultStates);
        return this;
    }

    private void pushAll(Collection<RenderState> states) {
        for (var state : states) {
            push(state);
        }
    }

    private void pushAll(RenderState... states) {
        for (var state : states) {
            push(state);
        }
    }

    public boolean pushAndApply(RenderState state) {
        if (canApply(state)) {
            push(state);
            renderer.applyRenderState(state);
            state.setNeedsUpdate(false);
            return true;
        }

        return false;
    }

    private void push(RenderState state) {
        var result = states.getOrDefault(state.type(), Array.ofType(RenderState.class));
        result.add(state);

        states.put(state.type(), result);
    }

    public boolean applyDefault(RenderState.Type type) {
        var result = states.get(type);
        var state = result != null ? result.firstSafe().orElse(null) : null;
        if (state == null) {
            return false;
        }

        pushAndApply(state);
        return true;
    }

    public boolean restore(RenderState.Type type) {
        var result = states.get(type);
        var previous = result.pop();

        var state = result != null ? result.lastSafe().orElse(null) : null;
        if (canApply(previous, state)) {
            renderer.applyRenderState(state);
            state.setNeedsUpdate(false);
            return true;
        }

        return false;
    }

    private boolean canApply(RenderState state) {
        if (state == null) {
            return false;
        }

        var result = states.get(state.type());
        var current = result != null ? result.lastSafe().orElse(null) : null;

        if (current == null) {
            return false;
        }

        return canApply(current, state);
    }

    private boolean canApply(RenderState previous, RenderState state) {
        if (state == null) {
            return false;
        }

        if (state.needsUpdate()) {
            return true;
        }

        return state != previous;
    }
}
