package fr.mercury.nucleus.asset.loader.image;

import fr.mercury.nucleus.asset.AssetDescriptor;
import fr.mercury.nucleus.texture.Image;

public class ImageDescriptor<C extends ImageAssetConfig> extends AssetDescriptor<Image> {

    public ImageDescriptor(String name) {
        super(name, new ImageAssetConfig());
    }

    public ImageDescriptor(String name, C config) {
        super(name, config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public C getConfig() {
        return (C) super.getConfig();
    }
}
