package fr.mercury.nucleus.asset;

/**
 * <code>AssetLoader</code> is an interface for implementing a specific type of
 * asset loader. The asset loader is in charge of loading an asset in various format
 * (a disk-file for example) and parsing it into a usable object.
 * 
 * @param <T> The type of asset to load.
 * 
 * @author GnosticOccultist
 */
public interface AssetLoader<T> {
	
	/**
	 * Loads the specific asset type from the specified path, to use
	 * in the application.
	 * 
	 * @param path The path to the object to load.
	 * @return	   The loaded object.
	 */
	T load(String path);
}
