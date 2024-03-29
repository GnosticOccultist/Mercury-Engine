package fr.mercury.nucleus.asset;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import fr.alchemy.utilities.Instantiator;
import fr.alchemy.utilities.SystemUtils;
import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.AbstractApplicationService;
import fr.mercury.nucleus.application.Application;
import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.asset.loader.AssetLoader;
import fr.mercury.nucleus.asset.loader.AssetLoader.Config;
import fr.mercury.nucleus.asset.loader.AssetLoaderDescriptor;
import fr.mercury.nucleus.asset.loader.GLSLLoader;
import fr.mercury.nucleus.asset.loader.MaterialLoader;
import fr.mercury.nucleus.asset.loader.OBJLoader;
import fr.mercury.nucleus.asset.loader.VoidLoaderConfig;
import fr.mercury.nucleus.asset.loader.assimp.AssimpLoader;
import fr.mercury.nucleus.asset.loader.data.AssetData;
import fr.mercury.nucleus.asset.loader.data.PathAssetData;
import fr.mercury.nucleus.asset.loader.image.AWTImageReader;
import fr.mercury.nucleus.asset.loader.image.STBImageReader;
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
 * <code>AssetManager</code> manages all the assets which can be used inside an
 * {@link Application}. For example, it will be able to load an asset with a
 * registered <code>AssetLoader</code> and translate a physical file into a
 * virtual object which can be later used inside a {@link ShaderProgram}, ...
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
    private final Map<AssetLoaderDescriptor<?>, AssetLoader<?, ?>> loaders = new HashMap<>();
    /**
     * The table containing the asset loaders ordered by their descriptor.
     */
    private final Array<AssetData> roots = Array.ofType(AssetData.class);

    /**
     * Instantiates a new <code>AssetManager</code> with a set of default
     * {@link AssetLoader} already registered.
     * <p>
     * An instance of asset manager is created by the {@link MercuryApplication} and
     * can be accessed with {@link MercuryApplication#getService(Class)}.
     */
    public AssetManager() {
        registerLoader(GLSLLoader.DESCRIPTOR);
        registerLoader(OBJLoader.DESCRIPTOR);
        registerLoader(AWTImageReader.DESCRIPTOR);
        registerLoader(STBImageReader.DESCRIPTOR);
        registerLoader(MaterialLoader.DESCRIPTOR);
        registerLoader(AssimpLoader.DESCRIPTOR);

        registerWorkingDirectory();
    }

    private void registerWorkingDirectory() {
        var workingDirectory = SystemUtils.getWorkingDirectory();
        registerRoot(new PathAssetData(Paths.get("").toAbsolutePath()));
        registerRoot(new PathAssetData(Paths.get("", "resources").toAbsolutePath()));

        var index = workingDirectory.lastIndexOf('\\');
        workingDirectory = workingDirectory.substring(0, index);
        workingDirectory += "/mercury-nucleus/resources";

        registerRoot(new PathAssetData(Paths.get(workingDirectory)));
    }

    /**
     * Register a specified type of {@link AssetLoader} using the given
     * {@link AssetLoaderDescriptor}.
     * 
     * @param descriptor The asset loader descriptor (not null).
     * @return The asset manager for chaining purposes.
     */
    public <A extends AssetLoader<?, ?>> AssetManager registerLoader(AssetLoaderDescriptor<A> descriptor) {
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
    public <A extends AssetLoader<?, ?>> AssetManager registerLoader(AssetLoaderDescriptor<A> descriptor,
            A assetLoader) {
        Validator.nonNull(descriptor, "The descriptor can't be null!");
        loaders.put(descriptor, assetLoader);
        return this;
    }

    /**
     * Unregister a specified type of {@link AssetLoader} using the given
     * {@link AssetLoaderDescriptor}.
     * 
     * @param type       The type of loader to register.
     * @param extensions The readable extensions with this loader.
     * @return The removed asset loader or null if none.
     */
    @SuppressWarnings("unchecked")
    public <A extends AssetLoader<?, ?>> A unregisterLoader(AssetLoaderDescriptor<A> descriptor) {
        return (A) loaders.remove(descriptor);
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
        return load(new PathAssetData(Paths.get(path)));
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
        AssetLoader<A, ?> loader = acquireLoader(path);
        if (loader != null) {
            return loader.loadFuture(new PathAssetData(Paths.get(path)), executor, listener);
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
        return load(new PathAssetData(Paths.get(path)));
    }

    /**
     * Loads the provided asset by translating it into a {@link TextureAtlas} using
     * a {@link STBImageReader}. The texture can then be used inside a
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
     * Loads the provided asset by translating it into a {@link Texture2D} using a
     * {@link STBImageReader}. The texture can then be used inside a
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
     * implementation using a {@link STBImageReader}. The texture can then be used
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
     * {@link STBImageReader} or {@link AWTImageReader}. Images can be wrapped
     * around a {@link Texture} to be used during rendering of models.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param path The path of the asset to load, must be a texture file extension.
     * @return The loaded image or null.
     */
    public Image loadImage(String path) {
        return load(new PathAssetData(Paths.get(path)));
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
    public <A extends AssetLoader<Image, VoidLoaderConfig>> Image loadImage(String path,
            AssetLoaderDescriptor<A> descriptor) {
        return load(new PathAssetData(Paths.get(path)), VoidLoaderConfig.get(), descriptor);
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
        return load(new PathAssetData(Paths.get(path)));
    }

    /**
     * Loads the provided asset using an {@link AssetLoader} described by the
     * extension of the given {@link AssetData}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param data The data of the asset to load (not null).
     * @return The loaded asset or null.
     */
    public <T> T load(AssetData data) {
        return load(data, null, null);
    }

    /**
     * Loads the provided asset using an {@link AssetLoader} described by the
     * extension of the given {@link AssetData}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param data   The data of the asset to load (not null).
     * @param config The loader configuration, or null for none.
     * @return The loaded asset or null.
     */
    public <T, C extends Config> T load(AssetData data, C config) {
        return load(data, config, null);
    }

    /**
     * Loads the provided asset using an {@link AssetLoader} described by the given
     * {@link AssetLoaderDescriptor}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param data       The data of the asset to load (not null).
     * @param config     The loader configuration, or null for none.
     * @param descriptor The asset loader descriptor to use (not null).
     * @return The loaded asset or null.
     */
    public <T, C extends Config, A extends AssetLoader<T, C>> T load(AssetData data, C config,
            AssetLoaderDescriptor<A> descriptor) {
        AssetLoader<T, C> loader = descriptor == null ? acquireLoader(data.getName())
                : instantiateNewLoader(descriptor);
        if (loader != null) {
            // Try resolving asset path with registered roots.
            var resoved = tryResolving(data);
            if (resoved != null) {

                if (config == null) {
                    return loader.load(resoved);
                } else {
                    return loader.load(resoved, config);
                }
            }

            logger.error("Couldn't resolve '" + data + "' with the roots registered!");
            throw new MercuryException("The asset '" + data + "' cannot be loaded using the registered roots.");
        }

        throw new MercuryException("The asset '" + data + "' cannot be loaded using the registered loaders.");
    }

    /**
     * Loads the provided asset asynchronously using an {@link AssetLoader}
     * described by the extension of the given {@link AssetData}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param data     The data of the asset to load (not null).
     * @param config   The loader configuration, or null for none.
     * @param executor The synchronous executor to run the listener on main thread.
     * @param listener The listener to call once the model has been loaded.
     * @return The loaded asset or null.
     */
    public <T, C extends Config> CompletableFuture<Void> loadAsync(AssetData data, C config, Executor executor,
            Consumer<T> listener) {
        return loadAsync(data, null, executor, listener);
    }

    /**
     * Loads the provided asset asynchronously using an {@link AssetLoader}
     * described by the given {@link AssetLoaderDescriptor}.
     * <p>
     * If no loader is found it will throw an exception.
     * 
     * @param data       The data of the asset to load (not null).
     * @param config     The loader configuration, or null for none.
     * @param descriptor The asset loader descriptor to use (not null).
     * @param executor   The synchronous executor to run the listener on main
     *                   thread.
     * @param listener   The listener to call once the model has been loaded.
     * @return The loaded asset or null.
     */
    public <T, C extends Config, A extends AssetLoader<T, C>> CompletableFuture<Void> loadAsync(AssetData data,
            C config, AssetLoaderDescriptor<A> descriptor, Executor executor, Consumer<T> listener) {
        AssetLoader<T, C> loader = descriptor == null ? acquireLoader(data.getName())
                : instantiateNewLoader(descriptor);
        if (loader != null) {
            // Try resolving asset path with registered roots.
            var resoved = tryResolving(data);
            if (resoved != null) {

                if (config == null) {
                    return loader.loadFuture(data, executor, listener);
                } else {
                    return loader.loadFuture(data, config, executor, listener);
                }
            }

            logger.error("Couldn't resolve '" + data + "' with the roots registered!");
            throw new MercuryException("The asset '" + data + "' cannot be loaded using the registered roots.");
        }

        throw new MercuryException("The asset '" + data + "' cannot be loaded using the registered loaders.");
    }

    /**
     * Acquire an appropriate <code>AssetLoader</code> for the asset by checking its
     * extension. If no loader is found it returns null.
     * 
     * @param path The path of the asset file.
     * @return The corresponding asset loader or null.
     */
    @SuppressWarnings("unchecked")
    public <A extends AssetLoader<?, ?>> A acquireLoader(String path) {
        String extension = FileUtils.getExtension(path);

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

    public AssetManager registerRoot(AssetData root) {
        this.roots.add(root);
        return this;
    }

    public AssetManager unregisterRoot(AssetData root) {
        this.roots.remove(root);
        return this;
    }

    private AssetData tryResolving(AssetData data) {
        if (roots.isEmpty()) {
            logger.error("Couldn't resolve '" + data + " since there are no roots registered!");
            return null;
        }

        for (var root : roots) {

            var resolved = root.resolve(data);
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <A extends AssetLoader<?, ?>> A instantiateNewLoader(AssetLoaderDescriptor<A> descriptor) {
        AssetLoader<?, ?> instance = null;
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
