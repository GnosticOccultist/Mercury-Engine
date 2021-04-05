package fr.mercury.nucleus.asset.loader.data;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import fr.alchemy.utilities.file.FileUtils;

public class PathAssetData extends AssetData {

    protected final Path path;

    public PathAssetData(Path path) {
        this.path = format(path);
    }

    @Override
    public InputStream openStream() {
        return FileUtils.openStream(path);
    }

    @Override
    public AssetData resolve(AssetData other) {
        var resolvePath = path.resolve(other.getPath());
        
        if (!Files.exists(resolvePath)) {
            return null;
        }
        
        var resolved = new PathAssetData(resolvePath);
        return resolved;
    }

    public PathAssetData sub(Path other) {
        return new PathAssetData(path.resolve(format(other)));
    }
    
    @Override
    public AssetData sibling(String other) {
        var sibling = path.resolveSibling(format(other));
        return new PathAssetData(sibling);
    }
    
    private Path format(Path path) {
        if (path.startsWith(File.separator)) {
            path = path.subpath(0, path.getNameCount());
        }
        
        return path;
    }
    
    private String format(String path) {
        if (path.startsWith(File.separator) || path.startsWith("/")) {
            path = path.substring(1);
        }
        
        return path;
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public String getPath() {
        return path.toString();
    }
    
    @Override
    public String toString() {
        return path.toString();
    }
}
