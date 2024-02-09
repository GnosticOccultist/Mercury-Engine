package fr.mercury.nucleus.asset.locator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.asset.AssetDescriptor;
import fr.mercury.nucleus.asset.AssetManager;

/**
 * <code>ClasspathLocator</code> is an implementation of {@link AssetLocator} to
 * locate an asset on the classpath. This locator is used to locate almost all
 * assets used in any sub-project even if it has been compressed into a jar
 * file.
 * 
 * @author GnosticOccultist
 */
public class ClasspathLocator implements AssetLocator {

    /**
     * The logger of the asset locator.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.assets.locator");

    /**
     * The asset manager.
     */
    private final AssetManager assetManager;
    /**
     * The root path.
     */
    private final Path root;

    /**
     * Instantiates a new <code>ClasspathLocator</code> to locate assets on
     * classpath.
     * 
     * @param assetManager The asset manager (not null).
     * @param root         The root to search in (not null).
     */
    public ClasspathLocator(AssetManager assetManager, Path root) {
        Validator.nonNull(assetManager, "The asset manager can't be null!");
        Validator.nonNull(root, "The root can't be null!");
        this.assetManager = assetManager;
        this.root = root;
    }

    @Override
    public LocatedAsset locate(AssetDescriptor<?> asset) {
        var name = root.resolve(asset.getName()).toString();

        logger.info("Trying to locate asset " + asset + " at '" + name + "'.");

        var url = Thread.currentThread().getContextClassLoader().getResource(name);
        if (url == null) {
            return null;
        }

        if ("file".equals(url.getProtocol())) {
            try {
                var path = Paths.get(url.toURI()).toString();
                // Make sure the path is valid.
                if (!path.endsWith(name)) {
                    throw new RuntimeException(
                            "Requested asset name " + name + " isn't relative to absolute path " + path);
                }

            } catch (URISyntaxException ex) {
                logger.error("Wrong URI format for " + url + " for asset " + asset, ex);
            }
        }

        try {
            return create(assetManager, asset, url);
        } catch (IOException ex) {
            logger.error("Failed to open stream from URL " + url + " for asset " + asset, ex);
        }

        return null;
    }

    /**
     * Tries to create a new valid {@link URLLocatedAsset} from the given
     * {@link URL} by attempting to open a connection.
     * 
     * @param assetManager The asset manager (not null).
     * @param asset        The descriptor of the asset to open.
     * @param url          The url pointing to the asset.
     * @return A valid located asset or null.
     * @throws IOException Thrown if an error occured while opening asset stream.
     */
    URLLocatedAsset create(AssetManager assetManager, AssetDescriptor<?> asset, URL url) throws IOException {
        var connection = url.openConnection();
        connection.setUseCaches(false);
        var in = connection.getInputStream();
        return in != null ? new URLLocatedAsset(assetManager, asset, url, in) : null;
    }

    /**
     * <code>URLLocatedAsset</code> is an implementation of {@link LocatedAsset}
     * which uses an {@link URL} to access the asset data.
     * 
     * @author GnosticOccultist
     */
    class URLLocatedAsset extends LocatedAsset {

        /**
         * The URL pointing to the located asset.
         */
        final URL url;
        /**
         * The input stream, or null if not opened.
         */
        InputStream in;

        /**
         * Instantiates a new valid <code>URLLocatedAsset</code>. Use
         * {@link ClasspathLocator#create(AssetManager, AssetDescriptor, URL)} if the
         * {@link URL} hasn't been tested yet.
         * 
         * @param assetManager The asset manager (not null).
         * @param asset        The descriptor of the located asset (not null).
         * @param url          The url pointing to the asset (not null).
         * @param in           An opened input stream (not null).
         */
        URLLocatedAsset(AssetManager assetManager, AssetDescriptor<?> asset, URL url, InputStream in) {
            super(assetManager, asset);
            Validator.nonNull(url, "The URL can't be null!");
            Validator.nonNull(in, "The input stream can't be null!");
            this.url = url;
            this.in = in;
        }

        @Override
        public InputStream openStream() {
            if (in != null) {
                var alias = in;
                in = null;
                return alias;
            }

            try {
                var connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            } catch (IOException ex) {
                logger.error("Failed to re-open stream from URL " + url + " for asset " + asset(), ex);
            }

            return null;
        }

        @Override
        public LocatedAsset sibling(AssetDescriptor<?> sibling) {
            try {
                var name = sibling.getName();
                if (name.startsWith("/")) {
                    name = name.substring(1);
                }

                var path = Paths.get(getName()).resolveSibling(name).toString();

                var sibUrl = Thread.currentThread().getContextClassLoader().getResource(path);
                if (sibUrl == null) {
                    throw new RuntimeException("Unable to find sibling asset with path '" + path + "'!" + name);
                }

                return ClasspathLocator.this.create(assetManager, sibling, sibUrl);

            } catch (IOException ex) {
                logger.error("Failed to open stream from sibling URL " + url + " for asset " + sibling, ex);
            }

            return null;
        }

        @Override
        public byte[] getBytes() {
            byte[] result = null;
            try {
                var path = getPath();
                result = Files.readAllBytes(path);
            } catch (IOException ex) {
                logger.error("Failed to count bytes for asset '" + this + "'.", ex);
            }

            return result;
        }

        /**
         * Return a valid {@link Path} for the <code>URLLocatedAsset</code>
         * 
         * @return A valid path (not null).
         */
        protected Path getPath() {
            try {
                // At this point the file should be accessible.
                var result = Paths.get(url.toURI());
                return result;
            } catch (URISyntaxException ex) {
                throw new AssertionError(ex);
            }
        }
    }
}
