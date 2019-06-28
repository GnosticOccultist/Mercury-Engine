package fr.mercury.nucleus.asset;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import fr.alchemy.utilities.Validator;

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
	 * Loads asynchronously the specific asset type from the specified path using 
	 * a {@link CompletableFuture}.
	 * 
	 * @param path The path of the object to load (not null or empty).
	 * @return	   A future task which will contain the loaded object once finished.
	 */
	default CompletableFuture<T> loadFuture(String path) {
		Validator.nonEmpty(path, "The asset path to load can't be null or empty!");
		return CompletableFuture.supplyAsync(() -> load(path));
	}
	
	/**
	 * Loads asynchronously the specific asset type from the specified path using 
	 * a {@link CompletableFuture} and execute the given listener to handle the asset once loaded.
	 * 
	 * @param path 	   The path of the object to load (not null or empty).
	 * @param listener The listener to get the asset once loaded.
	 * @return	  	   A future task which will contain the loaded object once finished, 
	 * 				   but shouldn't be needed if you provide a listener for the result.
	 */
	default CompletableFuture<Void> loadFuture(String path, Consumer<T> listener) {
		Validator.nonEmpty(path, "The asset path to load can't be null or empty!");
		return CompletableFuture.supplyAsync(() -> load(path)).thenAccept(listener);
	}
	
	/**
	 * Loads the specific asset type from the specified path, to use
	 * in the application.
	 * 
	 * @param path The path to the object to load.
	 * @return	   The loaded object.
	 */
	T load(String path);
	
	/**
	 * Sets the {@link AssetManager} for this <code>AssetLoader</code>, which
	 * can be reused to load sub-assets, dependent from the first one loaded.
	 * <p>
	 * This function is not necessarily used or implemented.
	 * 
	 * @param assetManager The asset manager.
	 */
	default void registerAssetManager(AssetManager assetManager) {}
}
