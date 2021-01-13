package fr.mercury.nucleus.asset.loader;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.collections.array.ReadOnlyArray;
import fr.alchemy.utilities.file.FileExtensions;
import fr.mercury.nucleus.asset.AssetManager;

/**
 * <code>AssetLoaderDescriptor</code> is an immutable class to instantiate and represent an {@link AssetLoader} using its supported
 * extensions. Every instance of this class must be static and registered through the {@link AssetManager} in order to be used.
 * <p>
 * Note that the {@link FileExtensions#UNIVERSAL_EXTENSION} is supported by the asset manager, so it can be used here as an argument.
 * 
 * @param <A> The type of asset loader to describe.
 * 
 * @author GnosticOccultist
 * 
 * @see AssetManager#registerLoader(AssetLoaderDescriptor)
 * @see AssetManager#load(String, AssetLoaderDescriptor)
 */
public final class AssetLoaderDescriptor<A extends AssetLoader<?>> implements Supplier<A>, Comparable<AssetLoaderDescriptor<?>> {

    /**
     * The callable function to create a new loader instance.
     */
    private final Callable<A> loaderSupplier;
    /**
     * The array of supported extensions by the loader.
     */
    private final ReadOnlyArray<String> extensions;
    /**
     * The priority of the loader.
     */
    private final int priority;

    /**
     * Instantiates a new <code>AssetLoaderDescriptor</code> using the provided {@link Callable}
     * and supported extensions.
     * <p>
     * The priority is set by default to a value of 1.
     * 
     * @param loaderSupplier The callable function to create a new loader instance (not null).
     * @param extensions     The set of supported extensions (not null or empty).
     */
    public AssetLoaderDescriptor(Callable<A> loaderSupplier, String... extensions) {
        this(loaderSupplier, 1, extensions);
    }

    /**
     * Instantiates a new <code>AssetLoaderDescriptor</code> using the provided {@link Callable}
     * and supported extensions.
     * 
     * @param loaderSupplier The callable function to create a new loader instance (not null).
     * @param priority       The priority of the asset loader.
     * @param extensions     The set of supported extensions (not null or empty).
     */
    public AssetLoaderDescriptor(Callable<A> loaderSupplier, int priority, String... extensions) {
        Validator.nonNull(loaderSupplier, "The supplier can't be null!");
        Validator.nonEmpty(extensions, "The supported extensions can't be null or empty!");
        this.loaderSupplier = loaderSupplier;
        this.priority = priority;
        this.extensions = Array.of(extensions);
    }

    /**
     * Instantiates a new <code>AssetLoaderDescriptor</code> using the provided {@link Callable}
     * and supported extensions.
     * <p>
     * The priority is set by default to a value of 1.
     * 
     * @param loaderSupplier The callable function to create a new loader instance (not null).
     * @param extensions     The set of supported extensions (not null or empty).
     */
    public AssetLoaderDescriptor(Callable<A> loaderSupplier, Collection<String> extensions) {
        this(loaderSupplier, 1, extensions);
    }

    /**
     * Instantiates a new <code>AssetLoaderDescriptor</code> using the provided {@link Callable}
     * and supported extensions.
     * 
     * @param loaderSupplier The callable function to create a new loader instance (not null).
     * @param priority       The priority of the asset loader.
     * @param extensions     The set of supported extensions (not null or empty).
     */
    public AssetLoaderDescriptor(Callable<A> loaderSupplier, int priority, Collection<String> extensions) {
        Validator.nonNull(loaderSupplier, "The supplier can't be null!");
        Validator.nonEmpty(extensions, "The supported extensions can't be null or empty!");
        this.loaderSupplier = loaderSupplier;
        this.priority = priority;
        this.extensions = Array.of(extensions);
    }

    /**
     * Invoke the internal {@link Callable} function to create a new {@link AssetLoader} instance.
     * This method is used by the {@link AssetManager}, when an asset needs to be loaded.
     * 
     * @return A new asset loader instance (not null).
     * 
     * @throws Exception Thrown if an error occured with instantiation.
     */
    public A supply() throws Exception {
        return loaderSupplier.call();
    }

    /**
     * Invoke the internal {@link Callable} function to create a new {@link AssetLoader} instance.
     * This method is used by the {@link AssetManager}, when an asset needs to be loaded.
     * 
     * @return A new asset loader instance (not null).
     * 
     * @throws RuntimeException Thrown if an error occured with instantiation.
     */
    @Override
    public A get() {
        try {
            A loader = loaderSupplier.call();
            return loader;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Return a read-only array of supported extensions by the {@link AssetLoader} described
     * with the <code>AssetLoaderDescriptor</code>.
     * 
     * @return The array of supported extensions.
     */
    public ReadOnlyArray<String> getExtensions() {
        return extensions;
    }

    /**
     * Return the priority of the {@link AssetLoader} described with the <code>AssetLoaderDescriptor</code>.
     * Such priority is used to determine which asset loader should be preferred over other ones.
     * <p>
     * To bypass this property and load an asset using a specific loader, {@link AssetManager#load(String, AssetLoaderDescriptor)}
     * method can be invoked with an appropriate asset loader descriptor.
     * 
     * @return The priority of the asset loader.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Compares the <code>AssetLoaderDescriptor</code> with the provided one using their respective
     * priority, in a decreasing manner.
     */
    @Override
    public int compareTo(AssetLoaderDescriptor<?> other) {
        return other.getPriority() - getPriority();
    }

    @Override
    public String toString() {
        return extensions + " priority= " + priority;
    }
}
