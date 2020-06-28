package fr.mercury.nucleus.application;

import java.util.HashMap;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.application.MercuryContext.Type;
import fr.mercury.nucleus.application.service.Window;

/**
 * <code>MercurySettings</code> stores the settings to be used by the {@link Application} for creating an appropriate 
 * {@link MercuryContext}. The settings can be loaded from default, changed and new ones can be added.
 * <p>
 * If the application settings are changed, it needs to be {@link Application#restart()} in order to apply changes and 
 * rebuild the context appropriately.
 * 
 * @author GnosticOccultist
 */
public final class MercurySettings extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	/**
	 * The default table with settings.
	 */
	private static final MercurySettings DEFAULT = new MercurySettings(false);

	static {
		
		DEFAULT.put("ContextType", Type.WINDOW);
		DEFAULT.put("Width", 1280);
		DEFAULT.put("Height", 768);
		DEFAULT.put("Title", "Mercury-Engine");
		DEFAULT.put("Icons", "/icons/mercury-logo-x32.png");
		DEFAULT.put("Fullscreen", false);
		DEFAULT.put("Resizable", true);
		DEFAULT.put("VSync", true);
		DEFAULT.put("Frequency", 60);
		DEFAULT.put("FrameRate", -1);
		DEFAULT.put("ShowFPS", true);
		DEFAULT.put("Samples", 1);
		DEFAULT.put("RequiredExtensions", new String[0]);
	}

	/**
	 * Create a new instance of <code>MercurySettings</code>.
	 * <p>
	 * If <code>loadDefaults</code> is true, then the {@link #DEFAULT} settings will
	 * be set on the instance.
	 * <p>
	 * You can change the settings safely.
	 * 
	 * @param loadDefault Whether to load default settings on the new instance.
	 */
	public MercurySettings(boolean loadDefault) {
		if (loadDefault) {
			putAll(DEFAULT);
		}
	}

	/**
	 * Copies all the settings from the provided <code>MercurySettings</code> to this one.
	 * <p>
	 * Any settings specified on the instance will be overwritten if contained in the other.
	 * 
	 * @param other The settings to copy from (not null).
	 */
	public void copyFrom(MercurySettings other) {
		Validator.nonNull(other, "The settings to copy from can't be null!");
		putAll(other);
	}

	/**
	 * Return the {@link Type} to use for creating the {@link MercuryContext}.
	 * 
	 * @return The context type used by the application (not null).
	 */
	public Type getContextType() {
		var result = (Type) get("ContextType");
		
		assert result != null;
		return result;
	}

	/**
	 * Set the {@link Type} to use for creating the {@link MercuryContext}.
	 * 
	 * @param type The context type to use with the application (not null, default&rarr;WINDOW).
	 */
	public void setContextType(Type type) {
		Validator.nonNull(type, "The context type can't be null!");
		put("ContextType", type);
	}
	
	/**
	 * Return the {@link Window} width in screen coordinates.
	 * 
	 * @return The window's width (&gt;0).
	 */
	public int getWidth() {
		var result = getInteger("Width");
		
		assert result > 0;
		return result;
	}

	/**
	 * Set the with of the {@link Window} in screen coordinates.
	 * 
	 * @param width The width of the window (&gt;0, default&rarr;1280).
	 */
	public void setWidth(int width) {
		Validator.positive(width, "The width of the window must be strictly positive!");
		addInteger("Width", width);
	}

	/**
	 * Return the window {@link Window} in screen coordinates.
	 * 
	 * @return The window's height (&gt;0).
	 */
	public int getHeight() {
		var result = getInteger("Height");
		
		assert result > 0;
		return result;
	}

	/**
	 * Set the height of the {@link Window} in screen coordinates.
	 * 
	 * @param height The height of the window (&gt;0, default&rarr;768).
	 */
	public void setHeight(int height) {
		Validator.positive(height, "The height of the window must be strictly positive!");
		addInteger("Height", height);
	}

	/**
	 * Set the resolution of the {@link Window} in screen coordinates.
	 * 
	 * @param width  The width of the window (&gt;0, default&rarr;1280).
	 * @param height The height of the window (&gt;0, default&rarr;768).
	 */
	public void setResolution(int width, int height) {
		setWidth(width);
		setHeight(height);
	}
	
	/**
	 * Return the title to use for the {@link Window}.
	 * 
	 * @return The window's title (not null).
	 */
	public String getTitle() {
		var result = getString("Title");
		
		assert result != null;
		return result;
	}

	/**
	 * Set the title to use for the {@link Window}.
	 * 
	 * @param title The title of the window (not null, default&rarr;"Mercury-Engine").
	 */
	public void setTitle(String title) {
		Validator.nonNull(title, "The title can't be null!");
		addString("Title", title);
	}
	
	/**
	 * Return whether the {@link Window} needs to be in fullscreen mode.
	 * 
	 * @return Whether the window is in fullscreen mode.
	 */
	public boolean isFullscreen() {
		return getBoolean("Fullscreen");
	}
	
	/**
	 * Return whether the {@link Window} needs to be resizable.
	 * 
	 * @return Whether the window is resizable.
	 */
	public boolean isResizable() {
		return getBoolean("Resizable");
	}
	
	/**
	 * Return whether to enable vertical-synchronization, limiting and synchronizing
	 * every frame rendered to the monitor's refresh rate.
	 * 
	 * @return Whether to enable vertical-synchronization.
	 * 
	 * @see #getFrequency()
	 */
	public boolean isVSync() {
		return getBoolean("VSync");
	}

	/**
	 * Set whether to enable vertical-synchronization, limiting and synchronizing
	 * every frame rendered to the monitor's refresh rate.
	 * 
	 * @param value Whether to enable V-Sync (default&rarr;false).
	 * 
	 * @see #setFrequency(int)
	 */
	public void setVSync(boolean value) {
		addBoolean("VSync", value);
	}
	
	/**
	 * Return the frequency or refresh rate of the {@link Window}. This is only
	 * used if V-Sync is enabled.
	 * 
	 * @return The frequency of the monitor (&gt;0).
	 * 
	 * @see #isVSync()
	 */
	public int getFrequency() {
		var result = getInteger("Frequency");
		
		assert result > 0;
		return result;
	}
	
	/**
	 * Set the frequency or refresh rate of the {@link Window}. This is only
	 * used if V-Sync is enabled.
	 * 
	 * @return The frequency of the monitor (&gt;0, default&rarr;60).
	 * 
	 * @see #setVSync(boolean)
	 */
	public void setFrequency(int frequency) {
		Validator.positive(frequency, "The frequency must be strictly positive!");
		addInteger("Frequency", frequency);
	}
	
	/**
	 * Return the upper limit for FPS of the {@link Application}.
	 * 
	 * @return The frame-rate upper limit, or -1 to disable it (&gt;0).
	 */
	public int getFrameRate() {
		var result = getInteger("FrameRate");
		return result;
	}

	/**
	 * Set the upper limit for FPS of the {@link Application}.
	 * 
	 * @param frameRate The frame-rate, or -1 to disable it (default&rarr;-1).
	 */
	public void setFrameRate(int frameRate) {
		addInteger("FrameRate", frameRate);
	}

	/**
	 * Return the number of samples per pixel. If the value is &gt;1, the rendered
	 * pixels will be multi-sampled.
	 * 
	 * @return The number of samples used for pixels (&ge;1).
	 */
	public int getSamples() {
		var result = getInteger("Samples");
		
		assert result >= 1;
		return result;
	}

	/**
	 * Set the number of samples per pixel. If the value is &gt;1, the rendered
	 * pixels will be multi-sampled.
	 * 
	 * @param frameRate The number of samples to use for pixels (default&rarr;1).
	 */
	public void setSamples(int samples) {
		Validator.inRange(samples, 1, Integer.MAX_VALUE);
		addInteger("Samples", samples);
	}

	/**
	 * Return the array of required extensions in order for the {@link Application}
	 * to render correctly. The required extensions will be checked during the
	 * creation of the {@link MercuryContext}.
	 * 
	 * @return The array of required graphics API extensions (not null).
	 */
	public String[] getRequiredExtensions() {
		String[] result = (String[]) get("RequiredExtensions");
		
		assert result != null;
		return result;
	}

	/**
	 * Set the array of required extensions in order for the {@link Application} to
	 * render correctly. The required extensions will be checked during the creation
	 * of the {@link MercuryContext}.
	 * 
	 * @param requiredExtensions The array of required graphics API extensions
	 *                           (default&rarr;empty, not null).
	 */
	public void setRequiredExtensions(String... requiredExtensions) {
		Validator.nonNull(requiredExtensions, "The required extensions can't be null!");
		put("RequiredExtensions", requiredExtensions);
	}

	/**
	 * Return an integer value from the <code>MercurySettings</code>.
	 * 
	 * @param key The key of the setting.
	 * @return The value or 0 if not set.
	 */
	public int getInteger(String key) {
		Integer i = (Integer) get(key);
		if (i == null) {
			return 0;
		}
		return i.intValue();
	}

	/**
	 * Add an integer to the <code>MercurySettings</code>.
	 * 
	 * @param key   The key of the setting.
	 * @param value The setting value.
	 */
	public void addInteger(String key, int value) {
		put(key, Integer.valueOf(value));
	}

	/**
	 * Return a float value from the <code>MercurySettings</code>.
	 * 
	 * @param key The key of the setting.
	 * @return The value or 0f if not set.
	 */
	public float getFloat(String key) {
		Float f = (Float) get(key);
		if (f == null) {
			return 0f;
		}
		return f.floatValue();
	}

	/**
	 * Add a float to the <code>MercurySettings</code>.
	 * 
	 * @param key   The key of the setting.
	 * @param value The setting value.
	 */
	public void addFloat(String key, float value) {
		put(key, Float.valueOf(value));
	}

	/**
	 * Return a boolean value from the <code>MercurySettings</code>.
	 * 
	 * @param key The key of the setting.
	 * @return The value or false if not set.
	 */
	public boolean getBoolean(String key) {
		Boolean b = (Boolean) get(key);
		if (b == null) {
			return false;
		}
		return b.booleanValue();
	}

	/**
	 * Add a boolean to the <code>MercurySettings</code>.
	 * 
	 * @param key   The key of the setting.
	 * @param value The setting value.
	 */
	public void addBoolean(String key, boolean value) {
		put(key, Boolean.valueOf(value));
	}

	/**
	 * Return a string value from the <code>MercurySettings</code>.
	 * 
	 * @param key The key of the setting.
	 * @return The value or null if not set.
	 */
	public String getString(String key) {
		String s = (String) get(key);
		if (s == null) {
			return null;
		}
		return s;
	}

	/**
	 * Add a string to the <code>MercurySettings</code>.
	 * 
	 * @param key   The key of the setting.
	 * @param value The setting value.
	 */
	public void addString(String key, String value) {
		put(key, value);
	}
}
