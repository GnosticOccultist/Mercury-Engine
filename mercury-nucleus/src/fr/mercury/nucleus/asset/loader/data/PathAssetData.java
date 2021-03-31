package fr.mercury.nucleus.asset.loader.data;

import java.io.InputStream;
import java.nio.file.Path;

import fr.alchemy.utilities.file.FileUtils;

public class PathAssetData extends AssetData {

    protected final Path path;
    
    public PathAssetData(Path path) {
        this.path = path;
    }

    @Override
    public InputStream openStream() {
        return FileUtils.openStream(path);
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }
}
