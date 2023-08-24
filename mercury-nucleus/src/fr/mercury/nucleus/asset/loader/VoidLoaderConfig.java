package fr.mercury.nucleus.asset.loader;

/**
 * <code>VoidLoaderConfig</code> is an implementation of {@link AssetLoader.Config} to define
 * an empty asset loader configuration.
 * 
 * @author GnosticOccultist
 */
public final class VoidLoaderConfig implements AssetLoader.Config {

    /**
     * The instance of void config.
     */
    private static final VoidLoaderConfig INSTANCE = new VoidLoaderConfig();
    
    /**
     * Return the single instance of <code>VoidLoaderConfig</code>.
     * 
     * @return The single instance config.
     */
    public static VoidLoaderConfig get() {
        return INSTANCE;
    }

    /**
     * Private constructor, use {@link VoidLoaderConfig#get()}.
     */
    private VoidLoaderConfig() {
        
    }
}
