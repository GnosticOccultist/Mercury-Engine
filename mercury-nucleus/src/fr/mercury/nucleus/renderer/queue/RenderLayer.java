package fr.mercury.nucleus.renderer.queue;

import java.util.HashMap;
import java.util.Map;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>RenderLayer</code> describes a layer on which is situated an {@link AnimaMundi}. Each {@link Camera} 
 * can define their own set of layers which they are in charge of queuing and rendering.
 * <p>
 * For now, the render layers are only useful to discard some anima-mundis from the rendering and queuing process, 
 * and aren't yet ready to actually perform an ordering during the rendering mostly used in 2D scenes.
 * 
 * @author GnosticOccultist
 */
public final class RenderLayer implements Comparable<RenderLayer> {

    /**
     * The table mapping the rendering layer with its name.
     */
    private static final Map<String, RenderLayer> RENDER_LAYERS = new HashMap<String, RenderLayer>();

    /**
     * Use the {@link AnimaMundi}'s parent render layer, or default to
     * {@link #DEFAULT} if it is orphan.
     */
    public static final RenderLayer LEGACY = get("Legacy", -1);
    /**
     * The default render layer.
     */
    public static final RenderLayer DEFAULT = get("Default", 0);
    /**
     * The front render layer.
     */
    public static final RenderLayer FRONT = get("Front", Integer.MAX_VALUE);

    /**
     * Return the <code>RenderLayer</code> with the specified name.
     * <p>
     * If the render layer doesn't already exists it will instantiates a new one
     * with this name and finally return it.
     * 
     * @param name The name of the layer to retrieve or create.
     * @return     The render layer matching the name or a new one.
     */
    public static RenderLayer get(String name, int index) {
        Validator.nonEmpty(name, "The name of the layer can't be null or empty!");

        RenderLayer layer = RENDER_LAYERS.get(name);
        if (layer == null) {
            layer = new RenderLayer(name, index);
            RENDER_LAYERS.put(name, layer);
        }

        return layer;
    }

    /**
     * The name of the render layer.
     */
    private final String name;
    /**
     * The index of the render layer.
     */
    private final int index;

    private RenderLayer(String name, int index) {
        this.name = name;
        this.index = index;
    }

    @Override
    public int compareTo(RenderLayer other) {
        return Integer.compare(index, other.index);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(index);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (getClass().equals(obj.getClass())) {
            return compareTo((RenderLayer) obj) == 0;
        }

        return false;
    }

    @Override
    public String toString() {
        return name + " [" + index + "]";
    }
}
