package fr.mercury.nucleus.asset;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.file.FileUtils;
import fr.mercury.nucleus.asset.loader.AssetLoader;

/**
 * <code>AssetDescriptor</code> represents the base description of an asset to
 * load using an {@link AssetLoader}. It defines a {@link AssetLoader.Config} to
 * apply a specific loading configuration.
 * 
 * @author GnosticOccultist
 */
public class AssetDescriptor<T> {

    /**
     * The name of the asset.
     */
    private final String name;
    /**
     * The loading configuration to use.
     */
    private final AssetLoader.Config config;

    /**
     * Instantiates a new <code>AssetDescriptor</code> with the given name and
     * {@link VoidLoaderConfig}.
     * 
     * @param name The name of the asset (not null, not empty).
     */
    public AssetDescriptor(String name) {
        this(name, VoidLoaderConfig.get());
    }

    /**
     * Instantiates a new <code>AssetDescriptor</code> with the given name and
     * {@link AssetLoader.Config}.
     * 
     * @param name   The name of the asset (not null, not empty).
     * @param config The configuration to use when loading the asset (not null).
     */
    public AssetDescriptor(String name, AssetLoader.Config config) {
        Validator.nonEmpty(name, "The asset name can't be null or empty!");
        Validator.nonNull(config, "The config can't be null!");
        this.name = name;
        this.config = config;
    }

    /**
     * Return the extension of the <code>AssetDescriptor</code>.
     * 
     * @return The extension of the asset (not null, not empty).
     */
    public String getExtension() {
        return FileUtils.getExtension(name);
    }

    /**
     * Return the name of the <code>AssetDescriptor</code>.
     * 
     * @return The name of the asset (not null, not empty).
     */
    public String getName() {
        return name;
    }

    /**
     * Return the {@link AssetLoader.Config} used when loading the
     * <code>AssetDescriptor</code>.
     * 
     * @return The loader config (not null).
     */
    public AssetLoader.Config getConfig() {
        return config;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AssetDescriptor)) {
            return false;
        }

        var other = (AssetDescriptor<?>) obj;
        return name.equals(other.name);
    }

    @Override
    public String toString() {
        return "AssetDescriptor [name=" + name + ", config=" + config + "]";
    }
}
