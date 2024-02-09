package fr.mercury.nucleus.asset.loader;

import fr.mercury.nucleus.asset.AssetDescriptor;
import fr.mercury.nucleus.asset.VoidLoaderConfig;
import fr.mercury.nucleus.asset.loader.AssetLoader.Config;
import fr.mercury.nucleus.scenegraph.AnimaMundi;

public class AnimaMundiDescriptor extends AssetDescriptor<AnimaMundi> {

    public AnimaMundiDescriptor(String name) {
        super(name, VoidLoaderConfig.get());
    }

    public AnimaMundiDescriptor(String name, Config config) {
        super(name, config);
    }

}
