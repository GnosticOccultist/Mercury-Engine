package fr.mercury.nucleus.application;

import java.util.HashMap;

/**
 * <code>MercurySettings</code> stores the settings to be used by the {@link Application}.
 * <p>
 * The settings can be loaded from default, changed or added.
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
		DEFAULT.put("Width", 1280);
		DEFAULT.put("Height", 768);
		DEFAULT.put("Title", "Mercury-Engine");
		DEFAULT.put("Fullscreen", false);
		DEFAULT.put("Resizable", true);
		DEFAULT.put("VSync", true);
		DEFAULT.put("FrameRate", -1);
		DEFAULT.put("ShowFPS", true);
		DEFAULT.put("Samples", 0);
	}
	
	/**
	 * Create a new instance of <code>MercurySettings</code>.
	 * <p>
	 * If <code>loadDefaults</code> is true, then the {@link #DEFAULT} 
	 * settings will be set on the instance.
	 * <p>
	 * You can change the settings safely.
	 * 
	 * @param loadDefault Whether to load default settings are to be loaded.
	 */
	public MercurySettings(boolean loadDefault) {
		if(loadDefault) {
			putAll(DEFAULT);
		}
	}
	
	/**
	 * Copies all the settings from the provided one to this one.
	 * <p>
	 * Any settings which are specified in other will overwrite these ones.
	 * 
	 * @param other The settings to copy from.
 	 */
	public void copyFrom(MercurySettings other) {
		putAll(other);
	}
	
	/**
	 * Return whether to enable vertical-synchronization.
	 * 
	 * @return Whether to enable vertical-synchronization.
	 */
	public boolean isVSync() {
		return getBoolean("VSync");
	}
	
	/**
     * Set whether to enable vertical-synchronization, limiting and synchronizing
     * every frame rendered to the monitor's refresh rate.
     * 
     * @param value Whether to enable vertical-synchronization.(Default: false)
     */
    public void setVSync(boolean value) {
    	addBoolean("VSync", value);
    }
	
	/**
	 * Return whether the window is resizable.
	 * 
	 * @return Whether the window is resizable.
	 */
	public boolean isResizable() {
		return getBoolean("Resizable");
	}
	
	/**
	 * Return whether the window is in fullscreen mode.
	 * 
	 * @return Whether the window is in fullscreen mode.
	 */
	public boolean isFullscreen() {
		return getBoolean("Fullscreen");
	}

	/**
	 * Return the window width in screen coordinates.
	 * 
	 * @return The window width.
	 */
	public int getWidth() {
		return getInteger("Width");
	}
	
	/**
	 * Set the with of the window in screen coordinates.
	 * 
	 * @param width The width of the window (default: 1280).
	 */
	public void setWidth(int width) {
		addInteger("Width", width);
	}

	/**
	 * Return the window height in screen coordinates.
	 * 
	 * @return The window height.
	 */
	public int getHeight() {
		return getInteger("Height");
	}
	
	/**
	 * Set the height of the window in screen coordinates.
	 * 
	 * @param height The height of the window (default: 768).
	 */
	public void setHeight(int height) {
		addInteger("Height", height);
	}
	
	/**
	 * Set the resolution of the window in screen coordinates.
	 * 
	 * @param width  The width of the window in screen coordinates (default: 1280).
	 * @param height The height of the window in screen coordinates (default: 768).
	 */
	public void setResolution(int width, int height) {
		setWidth(width);
		setHeight(height);
	}
	
	/**
	 * Return the window title.
	 * 
	 * @return The window title.
	 */
	public String getTitle() {
		return getString("Title");
	}
	
	/**
	 * Set the title of the window.
	 * 
	 * @param title The title of the window (default: "Mercury-Engine").
	 */
	public void setTitle(String title) {
		addString("Title", title);
	}
	
	/**
	 * Return the frame-rate.
	 * 
	 * @return The frame-rate upper limit.
	 */
	public int getFrameRate() {
		return getInteger("FrameRate");
	}
	
	/**
	 * Set the frame-rate, how high the application's frames-per-second can go
	 * 
	 * @param frameRate The frame-rate (Default: -1 no frame rate limit imposed).
	 */
	public void setFrameRate(int frameRate) {
		addInteger("FrameRate", frameRate);
	}
	
	/**
	 * Set the number of samples per pixel. If the value is &gt;1, the rendered 
	 * pixels will be multi-sampled.
	 * 
	 * @param frameRate The number of samples (Default: 1 &rarr; single-sampled).
	 */
	public void setSamples(int samples) {
		addInteger("Samples", samples);
	}
	
	/**
	 * Return an integer value from the settings.
	 * 
	 * @param key The key of the setting.
	 * @return	  The value or 0 if not set.
	 */
	public int getInteger(String key) {
		Integer i = (Integer) get(key);
		if(i == null) {
			return 0;
		}
		return i.intValue();
	}
	
	/**
	 * Add an integer on the settings.
	 * 
	 * @param key   The key of the setting.
	 * @param value	The setting value.
	 */
	public void addInteger(String key, int value) {
		put(key, Integer.valueOf(value));
	}
	
	/**
	 * Return a float value from the settings.
	 * 
	 * @param key The key of the setting.
	 * @return	  The value or 0f if not set.
	 */
	public float getFloat(String key) {
		Float f = (Float) get(key);
		if(f == null) {
			return 0f;
		}
		return f.floatValue();
	}
	
	/**
	 * Add a float on the settings.
	 * 
	 * @param key   The key of the setting.
	 * @param value	The setting value.
	 */
	public void addFloat(String key, float value) {
		put(key, Float.valueOf(value));
	}
	
	/**
	 * Return a boolean value from the settings.
	 * 
	 * @param key The key of the setting.
	 * @return	  The value or false if not set.
	 */
	public boolean getBoolean(String key) {
		Boolean b = (Boolean) get(key);
		if(b == null) {
			return false;
		}
		return b.booleanValue();
	}
	
	/**
	 * Add a boolean on the settings.
	 * 
	 * @param key   The key of the setting.
	 * @param value	The setting value.
	 */
	public void addBoolean(String key, boolean value) {
		put(key, Boolean.valueOf(value));
	}
	
	/**
	 * Return a string value from the settings.
	 * 
	 * @param key The key of the setting.
	 * @return	  The value or null if not set.
	 */
	public String getString(String key) {
		String s = (String) get(key);
		if(s == null) {
			return null;
		}
		return s;
	}
	
	/**
	 * Add a string on the settings.
	 * 
	 * @param key   The key of the setting.
	 * @param value	The setting value.
	 */
	public void addString(String key, String value) {
		put(key, value);
	}
}
