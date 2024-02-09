package fr.mercury.nucleus.asset;

import fr.alchemy.utilities.file.FileUtils;
import fr.mercury.nucleus.asset.loader.AssetLoader;

public class AssetDescriptor<T> {

    private final String name;

    private final AssetLoader.Config config;

    public AssetDescriptor(String name) {
        this(name, VoidLoaderConfig.get());
    }

    public AssetDescriptor(String name, AssetLoader.Config config) {
        this.name = name;
        this.config = config;
    }

    public String getExtension() {
        return FileUtils.getExtension(name);
    }

    public String getName() {
        return name;
    }

    public AssetLoader.Config getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "AssetDescriptor [name=" + name + ", config=" + config + "]";
    }
}
