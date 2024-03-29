package fr.mercury.nucleus.asset.loader.data;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import fr.alchemy.utilities.file.FileUtils;

public abstract class AssetData {

    public abstract InputStream openStream();

    public abstract AssetData resolve(AssetData other);

    public Optional<AssetData> tryResolve(AssetData other) {
        return Optional.ofNullable(resolve(other));
    }

    public abstract AssetData sibling(String path);

    public abstract String getName();

    public abstract Path getPath();

    public abstract String relativize();

    public abstract byte[] getBytes();

    public abstract long countBytes();

    public String getExtension() {
        return FileUtils.getExtension(getPath());
    }

    @Override
    public String toString() {
        return getName();
    }
}
