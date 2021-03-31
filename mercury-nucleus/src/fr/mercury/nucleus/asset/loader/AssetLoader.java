package fr.mercury.nucleus.asset.loader;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.application.service.TaskExecutorService;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.asset.loader.data.AssetData;

/**
 * <code>AssetLoader</code> is an interface for implementing a specific type of  asset loader. 
 * The asset loader is in charge of loading an asset in various format (a disk-file for example) 
 * and parsing it into a usable object.
 * 
 * @param <T> The type of asset to load.
 * 
 * @author GnosticOccultist
 */
public interface AssetLoader<T> {

    /**
     * Loads asynchronously the specific asset type from the specified path using a
     * {@link CompletableFuture}.
     * <p>
     * Note however than <code>OpenGL</code> specific method can't be invoked inside
     * the future, therefore their must be executed once the asset loaded on the
     * main {@link Thread}.
     * 
     * @param data The asset data to load (not null).
     * @return     A future task which will contain the loaded object once finished.
     * 
     * @see #loadFuture(String, Consumer)
     */
    default CompletableFuture<T> loadFuture(AssetData data) {
        Validator.nonNull(data, "The asset data to load can't be null!");
        return CompletableFuture.supplyAsync(() -> load(data));
    }

    /**
     * Loads asynchronously the specific asset type from the specified path using a
     * {@link CompletableFuture} and execute the given listener to handle the asset
     * once loaded.
     * <p>
     * Note however than <code>OpenGL</code> specific method can't be invoked inside
     * the future or in the listener, therefore their must be executed once the
     * asset loaded on the main {@link Thread}.
     * 
     * @param data     The asset data to load (not null).
     * @param listener The listener to get the asset once loaded.
     * @return         A future task which will contain the loaded object once finished, but
     *                 shouldn't be needed if you provide a listener for the result.
     * 
     * @see #loadFuture(String, Consumer)
     */
    default CompletableFuture<Void> loadFuture(AssetData data, Consumer<T> listener) {
        Validator.nonNull(data, "The asset data to load can't be null!");
        return CompletableFuture.supplyAsync(() -> load(data)).thenAccept(listener);
    }

    /**
     * Loads asynchronously the specific asset type from the specified path using a
     * {@link CompletableFuture} and execute the given listener to handle the asset
     * once loaded.
     * <p>
     * Note however than <code>OpenGL</code> specific method can't be invoked inside
     * the future or in the listener, unless the provided {@link Executor} is
     * running on the main {@link Thread}, like {@link TaskExecutorService#getGraphicsExecutor()}
     * 
     * @param data     The asset data to load (not null).
     * @param executor The executor to use for running the given listener.
     * @param listener The listener to get the asset once loaded.
     * @return         A future task which will contain the loaded object once finished, but
     *                 shouldn't be needed if you provide a listener for the result.
     * 
     * @see #loadFuture(String, Consumer)
     * @see TaskExecutorService
     */
    default CompletableFuture<Void> loadFuture(AssetData data, Executor executor, Consumer<T> listener) {
        Validator.nonNull(data, "The asset data to load can't be null!");
        return CompletableFuture.supplyAsync(() -> load(data)).thenAcceptAsync(listener, executor);
    }

    /**
     * Loads the specific asset type from the specified path, to use in the
     * application.
     * 
     * @param data The asset data to load (not null).
     * @return     The loaded object.
     */
    T load(AssetData data);

    /**
     * Sets the {@link AssetManager} for this <code>AssetLoader</code>, which can be
     * reused to load sub-assets, dependent from the first one loaded.
     * <p>
     * This function is not necessarily used or implemented.
     * 
     * @param assetManager The asset manager (not null).
     */
    default void registerAssetManager(AssetManager assetManager) {}
}
