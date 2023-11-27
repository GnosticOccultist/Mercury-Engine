package fr.mercury.nucleus.asset.loader.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.alchemy.utilities.SystemUtils;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;

public class PathAssetData extends AssetData {

    /**
     * The logger of the asset manager.
     */
    protected static final Logger logger = FactoryLogger.getLogger("mercury.assets");

    protected final Path path;

    public PathAssetData(String path) {
        this(Paths.get(path));
    }

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
    public String relativize() {
        var dir = SystemUtils.pathToWorkingDirectory();
        var relative = dir.toUri().relativize(path.toFile().toURI()).toString();
        return relative;
    }

    @Override
    public byte[] getBytes() {
        byte[] result = null;
        try {
            result = Files.readAllBytes(path);
        } catch (IOException ex) {
            logger.error("Failed to count bytes for asset '" + this + "'.");
        }

        return result;
    }

    @Override
    public long countBytes() {
        var result = 0L;
        try {
            result = Files.size(path);
        } catch (IOException ex) {
            logger.error("Failed to count bytes for asset '" + this + "'.");
        }

        return result;
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof AssetData)) {
            return false;
        }

        if (obj instanceof PathAssetData) {
            var other = (PathAssetData) obj;
            return other.path.equals(path);
        }

        var other = (AssetData) obj;
        return getPath().equals(other.getPath());
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
