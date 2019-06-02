package fr.mercury.nucleus.renderer.queue;

import java.util.HashMap;
import java.util.Map;

import fr.alchemy.utilities.Validator;

public final class RenderLayer {
	
	/**
	 * The table mapping the rendering layer with its name.
	 */
	private static final Map<String, RenderLayer> RENDER_LAYERS = new HashMap<String, RenderLayer>();
	
	public static final RenderLayer LEGACY = get("Legacy", -1);
	
	public static final RenderLayer DEFAULT = get("Default", 0);
	
	public static final RenderLayer FRONT = get("Front", Integer.MAX_VALUE);
	
	public static RenderLayer get(String name, int index) {
		Validator.nonEmpty(name, "The name of the layer can't be null or empty!");
		
		RenderLayer layer = RENDER_LAYERS.get(name);
		if(layer == null) {
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
	public String toString() {
		return " [" + index + "] " + name;
	}
}
