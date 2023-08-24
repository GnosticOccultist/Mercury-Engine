package fr.mercury.nucleus.asset.loader.data;

import java.io.InputStream;
import java.util.Optional;

public abstract class AssetData {

    public abstract InputStream openStream();

    public abstract AssetData resolve(AssetData other);

    public Optional<AssetData> tryResolve(AssetData other) {
        return Optional.ofNullable(resolve(other));
    }
    
    public abstract AssetData sibling(String path);

    public abstract String getName();

    public abstract String getPath();

    @Override
    public String toString() {
        return getName();
    }
}
