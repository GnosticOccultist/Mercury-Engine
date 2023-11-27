package fr.mercury.nucleus.asset.loader.assimp;

import org.lwjgl.assimp.Assimp;

import fr.mercury.nucleus.asset.loader.AssetLoader;

/**
 * <code>AssimpLoaderConfig</code> is an implementation of
 * {@link AssetLoader.Config} to define a set of parameters to use when loading
 * a scene/model file using the {@link AssimpLoader}.
 * 
 * @author GnosticOccultist
 */
public class AssimpLoaderConfig implements AssetLoader.Config {

    /**
     * The default Assimp loader configuration.
     */
    public static final AssimpLoaderConfig DEFAULT_CONFIG = new AssimpLoaderConfig();

    /**
     * The Assimp flags that the loader uses by default.
     */
    private static final int DEFAULT_ASSIMP_FLAGS = Assimp.aiProcess_JoinIdenticalVertices
            | Assimp.aiProcess_Triangulate | Assimp.aiProcess_GenSmoothNormals | Assimp.aiProcess_SortByPType
            | Assimp.aiProcess_PreTransformVertices | Assimp.aiProcess_FlipUVs | Assimp.aiProcess_ValidateDataStructure
            | Assimp.aiProcess_OptimizeMeshes | Assimp.aiProcess_RemoveRedundantMaterials;

    /**
     * Whether to ignore the root-node.
     */
    private boolean ignoreRootNode;
    /**
     * Whether to load and apply texture to the scene.
     */
    private boolean loadTextures;
    /**
     * The assimp flags to use when loading scene file.
     */
    private int flags;

    /**
     * Instantiates a new <code>AssimpLoaderConfig</code> with the default
     * parameters.
     */
    public AssimpLoaderConfig() {
        this(DEFAULT_ASSIMP_FLAGS, true, true);
    }

    /**
     * Instantiates a new <code>AssimpLoaderConfig</code> to use with the
     * {@link AssimpLoader}.
     * 
     * @param flags          The assimp flags to use when loading scene file.
     * @param ignoreRootNode Whether to ignore the root-node.
     * @param loadTextures   Whether to load and apply texture to the scene.
     */
    public AssimpLoaderConfig(int flags, boolean ignoreRootNode, boolean loadTextures) {
        this.flags = flags;
        this.ignoreRootNode = ignoreRootNode;
        this.loadTextures = loadTextures;
    }

    /**
     * Return whether the loader should ignore the root-node, preventing it to be
     * added to the loaded scene. It can be useful in some cases where the user has
     * already a root node defined and just want the children.
     * 
     * @return Whether to ignore the root-node (default &rarr; true).
     */
    public boolean ignoreRootNode() {
        return ignoreRootNode;
    }

    /**
     * Sets whether the {@link AssimpLoader} should ignore the root-node, preventing
     * it to be added to the loaded scene. It can be useful in some cases where the
     * user has already a root node defined and just want the children.
     * 
     * @param ignoreRootNode Whether to ignore the root-node (default &rarr; true).
     * @return The assimp loader config.
     */
    public AssimpLoaderConfig setIgnoreRootNode(boolean ignoreRootNode) {
        this.ignoreRootNode = ignoreRootNode;
        return this;
    }

    /**
     * Return whether the loader should load and apply the textures to the loaded
     * scene.
     * 
     * @return Whether to load and apply textures (default &rarr; true).
     */
    public boolean loadTextures() {
        return loadTextures;
    }

    /**
     * Sets whether the {@link AssimpLoader} should load and apply the textures to
     * the loaded scene.
     * 
     * @param loadTextures Whether to load and apply textures (default &rarr; true).
     * @return The assimp loader config.
     */
    public AssimpLoaderConfig setLoadTextures(boolean loadTextures) {
        this.loadTextures = loadTextures;
        return this;
    }

    /**
     * Return the assimp flags used by the {@link AssimpLoader}.
     * 
     * @return The assimp flags.
     */
    public int flags() {
        return flags;
    }

    /**
     * Sets the assimp flags to use by the {@link AssimpLoader}.
     * 
     * @param flags The assimp flags.
     */
    public AssimpLoaderConfig setFlags(int flags) {
        this.flags = flags;
        return this;
    }
}
