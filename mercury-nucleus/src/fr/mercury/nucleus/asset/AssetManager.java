package fr.mercury.nucleus.asset;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import fr.alchemy.utilities.Instantiator;
import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.Application;
import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.application.service.AbstractApplicationService;
import fr.mercury.nucleus.asset.loader.AnimaMundiDescriptor;
import fr.mercury.nucleus.asset.loader.AssetLoader;
import fr.mercury.nucleus.asset.loader.AssetLoader.Config;
import fr.mercury.nucleus.asset.loader.AssetLoaderDescriptor;
import fr.mercury.nucleus.asset.loader.MaterialLoader;
import fr.mercury.nucleus.asset.loader.OBJLoader;
import fr.mercury.nucleus.asset.loader.assimp.AssimpLoader;
import fr.mercury.nucleus.asset.loader.image.AWTImageReader;
import fr.mercury.nucleus.asset.loader.image.ImageDescriptor;
import fr.mercury.nucleus.asset.loader.image.STBImageReader;
import fr.mercury.nucleus.asset.loader.image.ktx.KTXImageReader;
import fr.mercury.nucleus.asset.loader.shader.GLSLLoader;
import fr.mercury.nucleus.asset.locator.AssetLocator;
import fr.mercury.nucleus.asset.locator.AssetLocator.LocatedAsset;
import fr.mercury.nucleus.asset.locator.ClasspathLocator;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.Image;
import fr.mercury.nucleus.texture.Texture;
import fr.mercury.nucleus.texture.Texture2D;
import fr.mercury.nucleus.texture.TextureAtlas;
import fr.mercury.nucleus.utils.MercuryException;

/**
 * <code>AssetManager</code> is an implementation of
 * {@link AbstractApplicationService} responsible of locating, loading or
 * caching assets for an {@link Application}.
 * <p>
 * For example, it will be able to load an asset with a registered
 * {@link AssetLoader} to a usable object instance by the application (model,
 * texture, shader, material, etc.).
 * <p>
 * In order to locate an asset, the manager will use a set of
 * {@link AssetLocator}, mainly the {@link ClasspathLocator} which work within
 * the classpath hierarchy.
 * 
 * @author GnosticOccultist
 */
public class AssetManager extends AbstractApplicationService {

    /**
     * The logger of the asset manager.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.assets");
    /**
     * The table containing the asset loaders ordered by their descriptor.
     */
    private final Map<AssetLoaderDescriptor<?>, AssetLoader<?>> loaders = new HashMap<>();
    /**
     * The array of registered asset locators.
     */
    private final Array<AssetLocator> locators = Array.ofType(AssetLocator.class);

    /**
     * Instantiates a new <code>AssetManager</code> with a set of default
     * {@link AssetLoader} already registered. A {@link ClasspathLocator} is
     * registered to locate resources available on the classpath.
     * <p>
     * An instance of asset manager is created automatically by the
     * {@link MercuryApplication} and can be accessed with
     * {@link MercuryApplication#getService(Class)}.
     */
    public AssetManager() {
        registerLoader(GLSLLoader.DESCRIPTOR);
        registerLoader(OBJLoader.DESCRIPTOR);
        registerLoader(AWTImageReader.DESCRIPTOR);
        registerLoader(KTXImageReader.DESCRIPTOR);
        registerLoader(STBImageReader.DESCRIPTOR);
        registerLoader(MaterialLoader.DESCRIPTOR);
        registerLoader(AssimpLoader.DESCRIPTOR);

        registerLocator(new ClasspathLocator(this, Paths.get("")));
    }

    /**
     * Register a specified type of {@link AssetLoader} using the given
     * {@link AssetLoaderDescriptor}.
     * 
     * @param descriptor The asset loader descriptor (not null).
     * @return The asset manager for chaining purposes.
     */
    public <A extends AssetLoader<?>> AssetManager registerLoader(AssetLoaderDescriptor<A> descriptor) {
        registerLoader(descriptor, null);
        return this;
    }

    /**
     * Register a specified type of {@link AssetLoader} using the given
     * {@link AssetLoaderDescriptor}.
     * 
     * @param descriptor  The asset loader descriptor (not null).
     * @param assetLoader An asset loader instance, or null for a later
     *                    instantiation.
     * @return The asset manager for chaining purposes.
     */
    public <A extends AssetLoader<?>> AssetManager registerLoader(AssetLoaderDescriptor<A> descriptor, A assetLoader) {
        Validator.nonNull(descriptor, "The descriptor can't be null!");
        if (assetLoader != null) {
            assetLoader.registerAssetManager(this);
        }

        this.loaders.put(descriptor, assetLoader);
        logger.info("Registering asset loader with descriptor " + descriptor + ", " + assetLoader);
        return this;
    }

    /**
     * Unregister a specified type of {@link AssetLoader} using the given
     * {@link AssetLoaderDescriptor}.
     * 
     * @param descriptor The loader descriptor to unregister (not null).
     * @return The removed asset loader or null if none.
     */
    @SuppressWarnings("unchecked")
    public <A extends AssetLoader<?>> A unregisterLoader(AssetLoaderDescriptor<A> descriptor) {
        Validator.nonNull(descriptor, "The descriptor can't be null!");
        var assetLoader = loaders.remove(descriptor);
        if (assetLoader != null) {
            assetLoader.registerAssetManager(null);
            logger.info("Unregistering asset loader with descriptor " + descriptor + ", " + assetLoader);
        }

        return (A) assetLoader;
    }

    /**
     * Register the provided {@link AssetLocator} to locate assets on filesystem.
     * 
     * @param locator The locator to register (not null).
     * @return The asset manager for chaining purposes.
     */
    public AssetManager registerLocator(AssetLocator locator) {
        Validator.nonNull(locator, "The asset locator can't be null!");
        this.locators.add(locator);
        logger.info("Registering root " + locator);
        return this;
    }

    /**
     * Unregister the specified {@link AssetLocator} to no longer locate assets on
     * filesystem.
     * 
     * @param locator The locator to unregister (not null).
     * @return The asset manager for chaining purposes.
     */
    public AssetManager unregisterLocator(AssetLocator locator) {
        var removed = locators.remove(locator);
        if (removed) {
            logger.info("Unregistering root " + locator);
        }

        return this;
    }

    /**
     * Try to locate the provided {@link AssetDescriptor} using the registered
     * {@link AssetLocator}. It will return an optional {@link LocatedAsset} empty
     * if the asset couldn't be located.
     * 
     * @param asset The descriptor of the asset to locate (not null).
     * @return An optional located asset, empty if the asset couldn't be located
     *         (not null).
     */
    public Optional<LocatedAsset> tryLocating(AssetDescriptor<?> asset) {
        Validator.nonNull(asset, "The asset descriptor can't be null!");

        if (locators.isEmpty()) {
            logger.error("Couldn't resolve '" + asset + " since there are no locators registered!");
            return Optional.empty();
        }

        for (var locator : locators) {
            var located = locator.locateSafe(asset);
            if (located.isPresent()) {
                return located;
            }
        }

        return Optional.empty();
    }

    /**
     * Loads the provided asset by translating it into a {@link PhysicaMundi} using
     * a valid {@link AssetLoader}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path The path of the asset to load, must be a model file extension.
     * @return The loaded physica-mundi or null.
     */
    public PhysicaMundi loadPhysicaMundi(String path) {
        return loadAnimaMundi(path);
    }

    /**
     * Loads the provided asset by translating it into an implementation of
     * {@link AnimaMundi} using a valid {@link AssetLoader}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path The path of the asset to load, must be a model file extension.
     * @return The loaded model or null.
     */
    public <A extends AnimaMundi> A loadAnimaMundi(String path) {
        return load(new AnimaMundiDescriptor(path));
    }

    /**
     * Loads the provided asset by translating it into an implementation of
     * {@link AnimaMundi} using a valid {@link AssetLoader}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path The path of the asset to load, must be a model file extension.
     * @return The loaded model or null.
     */
    public <A extends AnimaMundi> A loadAnimaMundi(AnimaMundiDescriptor asset) {
        return load(asset);
    }

    /**
     * Loads the provided asset asynchronously by translating it into an
     * implementation of {@link AnimaMundi} using a valid {@link AssetLoader}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path     The path of the asset to load, must be a model file
     *                 extension.
     * @param executor The synchronous executor to run the listener on main thread.
     * @param listener The listener to call once the model has been loaded.
     * @return The loaded model or null.
     */
    public <A extends AnimaMundi> CompletableFuture<Void> loadAnimaMundiAsync(String path, Executor executor,
            Consumer<A> listener) {
        var asset = new AnimaMundiDescriptor(path);
        AssetLoader<A> loader = acquireLoader(asset);
        if (loader != null) {
            var located = tryLocating(asset);
            if (located != null) {
                return loader.loadFuture(located.get(), executor, listener);
            }
        }

        throw new MercuryException("The asset '" + path + "' cannot be loaded using the registered loaders.");
    }

    /**
     * Loads the provided asset by translating it into an array of {@link Material}
     * using a {@link MaterialLoader}. Such material can be attributed to a
     * {@link PhysicaMundi} for rendering it.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path The path of the asset to load, must be a JSON file extension.
     * @return The loaded materials or null.
     */
    public Material[] loadMaterial(String path) {
        return load(new AssetDescriptor<Material[]>(path));
    }

    /**
     * Loads the provided asset by translating it into a {@link TextureAtlas} using
     * an {@link AWTImageReader}. The texture can then be used inside a
     * {@link ShaderProgram}, by creating an adapted {@link Uniform}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path    The path of the asset to load, must be a texture file
     *                extension.
     * @param columns The number of columns in the atlas (&gt;1).
     * @param rows    The number of rows in the atlas (&gt;1).
     * @return The loaded texture atlas or null.
     */
    public TextureAtlas loadTextureAtlas(String path, int columns, int rows) {
        var image = loadImage(path);
        if (image == null) {
            return null;
        }

        var texture = new TextureAtlas(columns, rows);
        texture.setImage(image);
        return texture;
    }

    /**
     * Loads the provided asset by translating it into a {@link Texture2D} using an
     * {@link AWTImageReader}. The texture can then be used inside a
     * {@link ShaderProgram}, by creating an adapted {@link Uniform}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path The path of the asset to load, must be a texture file extension.
     * @return The loaded texture or null.
     */
    public Texture2D loadTexture2D(String path) {
        return loadTexture(path, Texture2D.class);
    }

    /**
     * Loads the provided asset by translating it into a {@link Texture}
     * implementation using an {@link AWTImageReader}. The texture can then be used
     * inside a {@link ShaderProgram}, by creating an adapted {@link Uniform}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path The path of the asset to load, must be a texture file extension.
     * @return The loaded texture or null.
     */
    public <T extends Texture> T loadTexture(String path, Class<T> type) {
        var image = loadImage(path);
        if (image == null) {
            return null;
        }

        T texture = Instantiator.fromClass(type);
        texture.setImage(image);
        return texture;
    }

    /**
     * Loads the provided asset into an {@link Image} instance using a
     * {@link AWTImageReader}. Images can be wrapped around a {@link Texture} to be
     * used during rendering of models.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path The path of the asset to load, must be a texture file extension.
     * @return The loaded image or null.
     */
    public Image loadImage(String path) {
        return load(new ImageDescriptor<>(path));
    }

    /**
     * Loads the provided asset into an {@link Image} instance using an
     * {@link AssetLoader} described by the given {@link AssetLoaderDescriptor}.
     * Images can be wrapped around a {@link Texture} to be used during rendering of
     * models.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path       The path of the asset to load, must be a texture file
     *                   extension.
     * @param descriptor The descriptor of the asset loader to use.
     * @return The loaded image or null.
     */
    public <A extends AssetLoader<Image>> Image loadImage(String path, AssetLoaderDescriptor<A> descriptor) {
        return load(new ImageDescriptor<>(path), descriptor);
    }

    /**
     * Loads the provided asset by translating it into a {@link ShaderSource} using
     * a {@link GLSLLoader}. The source can then be used inside a
     * {@link ShaderProgram}, by calling
     * {@link ShaderProgram#attachSource(ShaderSource)}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path The path of the asset to load, must be a shader file extension.
     * @return The loaded shader source or null.
     */
    public ShaderSource loadShaderSource(String path) {
        return load(new AssetDescriptor<ShaderSource>(path));
    }

    /**
     * Loads the provided asset using an {@link AssetLoader} described by the
     * extension of the given {@link AssetData}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param asset The data of the asset to load (not null).
     * @return The loaded asset or null.
     */
    public <T> T load(AssetDescriptor<?> asset) {
        return load(asset, null);
    }

    /**
     * Loads the provided asset using an {@link AssetLoader} described by the given
     * {@link AssetLoaderDescriptor}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param asset      The data of the asset to load (not null).
     * @param config     The loader configuration, or null for none.
     * @param descriptor The asset loader descriptor to use (not null).
     * @return The loaded asset or null.
     */
    public <T, A extends AssetLoader<T>> T load(AssetDescriptor<?> asset, AssetLoaderDescriptor<A> descriptor) {
        AssetLoader<T> loader = descriptor == null ? acquireLoader(asset) : instantiateNewLoader(descriptor);
        if (loader != null) {
            // Try resolving asset path with registered roots.
            var resoved = tryLocating(asset);
            if (resoved != null) {
                return loader.load(resoved.get());
            }

            logger.error("Couldn't resolve '" + asset + "' with the roots registered!");
            throw new MercuryException("The asset '" + asset + "' cannot be loaded using the registered roots.");
        }

        throw new MercuryException("The asset '" + asset + "' cannot be loaded using the registered loaders.");
    }

    /**
     * Loads the provided asset asynchronously using an {@link AssetLoader}
     * described by the extension of the given {@link AssetData}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param asset    The data of the asset to load (not null).
     * @param config   The loader configuration, or null for none.
     * @param executor The synchronous executor to run the listener on main thread.
     * @param listener The listener to call once the model has been loaded.
     * @return The loaded asset or null.
     */
    public <T, C extends Config> CompletableFuture<Void> loadAsync(AssetDescriptor<?> asset, Executor executor,
            Consumer<T> listener) {
        return loadAsync(asset, null, executor, listener);
    }

    /**
     * Loads the provided asset asynchronously using an {@link AssetLoader}
     * described by the given {@link AssetLoaderDescriptor}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param asset      The data of the asset to load (not null).
     * @param config     The loader configuration, or null for none.
     * @param descriptor The asset loader descriptor to use (not null).
     * @param executor   The synchronous executor to run the listener on main
     *                   thread.
     * @param listener   The listener to call once the model has been loaded.
     * @return The loaded asset or null.
     */
    public <T, A extends AssetLoader<T>> CompletableFuture<Void> loadAsync(AssetDescriptor<?> asset,
            AssetLoaderDescriptor<A> descriptor, Executor executor, Consumer<T> listener) {
        AssetLoader<T> loader = descriptor == null ? acquireLoader(asset) : instantiateNewLoader(descriptor);
        if (loader != null) {
            // Try resolving asset path with registered roots.
            var resolved = tryLocating(asset);
            if (resolved != null) {
                return loader.loadFuture(resolved.get(), executor, listener);
            }

            logger.error("Couldn't resolve '" + asset + "' with the roots registered!");
            throw new MercuryException("The asset '" + asset + "' cannot be loaded using the registered roots.");
        }

        throw new MercuryException("The asset '" + asset + "' cannot be loaded using the registered loaders.");
    }

    /**
     * Acquire an appropriate <code>AssetLoader</code> for the asset by checking its
     * extension. If no loader is found it returns null.
     * 
     * @param asset The data of the asset to load (not null).
     * @return The corresponding asset loader or null.
     */
    @SuppressWarnings("unchecked")
    public <A extends AssetLoader<?>> A acquireLoader(AssetDescriptor<?> asset) {
        var extension = asset.getExtension();

        List<AssetLoaderDescriptor<?>> descriptors = new ArrayList<>();
        descriptors.addAll(loaders.keySet());
        descriptors.sort(null);

        for (var descriptor : descriptors) {
            for (var ext : descriptor.getExtensions()) {
                // Support universal extension, even though I doubt it is gonna be used.
                if (ext.equals(extension) || ext.equals(FileExtensions.UNIVERSAL_EXTENSION)) {
                    var loader = loaders.get(descriptor);
                    if (loader == null) {
                        loader = instantiateNewLoader(descriptor);
                        loaders.put(descriptor, loader);
                    }
                    return (A) loader;
                }
            }
        }

        logger.warning("No asset loaders are registered for the extension: '" + extension + "'.");
        return null;
    }

    @SuppressWarnings("unchecked")
    private <A extends AssetLoader<?>> A instantiateNewLoader(AssetLoaderDescriptor<A> descriptor) {
        AssetLoader<?> instance = null;
        try {
            instance = descriptor.supply();
        } catch (Exception ex) {
            logger.error("Failed to instantiate the loader from descriptor: " + descriptor
                    + ". Please check the given supplier.", ex);
        }

        if (instance != null) {
            instance.registerAssetManager(this);
        }

        return (A) instance;
    }
}
