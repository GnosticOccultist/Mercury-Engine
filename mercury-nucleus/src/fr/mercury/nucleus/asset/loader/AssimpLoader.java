package fr.mercury.nucleus.asset.loader;

import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.function.Consumer;

import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIFile;
import org.lwjgl.assimp.AIFileIO;
import org.lwjgl.assimp.AIFileOpenProc;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.asset.loader.data.AssetData;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Transform;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.renderer.logic.state.PolygonModeState;
import fr.mercury.nucleus.renderer.logic.state.PolygonModeState.PolygonMode;
import fr.mercury.nucleus.renderer.logic.state.RenderState;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Face;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.Usage;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.Mesh.Mode;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.Texture2D;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.data.Allocator;
import fr.mercury.nucleus.utils.data.BufferUtils;

public class AssimpLoader implements AssetLoader<AnimaMundi> {

    /**
     * The assimp asset loader descriptor.
     */
    public static final AssetLoaderDescriptor<AssimpLoader> DESCRIPTOR = new AssetLoaderDescriptor<>(
            AssimpLoader::new,
            0, 
            FileExtensions.BLENDER_MODEL_FORMAT, 
            FileExtensions.OBJ_MODEL_FORMAT, 
            FileExtensions.GLTF_MODEL_FORMAT,
            FileExtensions.FBX_MODEL_FORMAT);

    /**
     * The logger of the Assimp loader.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.asset.assimp");
    /**
     * The Assimp flags that the loader uses by default.
     */
    private static final int DEFAULT_ASSIMP_FLAGS = Assimp.aiProcess_JoinIdenticalVertices
            | Assimp.aiProcess_Triangulate | Assimp.aiProcess_GenSmoothNormals | Assimp.aiProcess_SortByPType
            | Assimp.aiProcess_PreTransformVertices | Assimp.aiProcess_FlipUVs;

    private AssetManager assetManager = null;

    @Override
    public AnimaMundi load(AssetData data) {
        return load(data, ConfigFlag.IGNORE_ROOT_NODE | ConfigFlag.LOAD_TEXTURE);
    }

    public AnimaMundi load(AssetData data, int configFlags) {
        // Define our own IO logic for Assimp.
        AIFileIO io = AIFileIO.create();

        var openProc = new AIFileOpenProc() {

            @Override
            public long invoke(long pFileIO, long fileName, long openMode) {
                var file = AIFile.create();
                var filePath = MemoryUtil.memUTF8(fileName);

                var buffer = BufferUtils.createByteBuffer(8192);
                var data = FileUtils.toByteBuffer(filePath, buffer, this::resize);

                file.ReadProc((pFile, pBuffer, size, count) -> {
                    var max = Math.min(data.remaining(), size * count);
                    MemoryUtil.memCopy(MemoryUtil.memAddress(data) + data.position(), pBuffer, max);
                    return max;
                });

                file.SeekProc((pFile, offset, origin) -> {
                    if (origin == Assimp.aiOrigin_CUR) {
                        data.position(data.position() + (int) offset);
                    } else if (origin == Assimp.aiOrigin_SET) {
                        data.position((int) offset);
                    } else if (origin == Assimp.aiOrigin_END) {
                        data.position(data.limit() + (int) offset);
                    }
                    return 0;
                });

                file.FileSizeProc(pFile -> {
                    return data.limit();
                });

                return file.address();
            }

            private ByteBuffer resize(ByteBuffer buffer, Integer size) {
                var newBuffer = BufferUtils.createByteBuffer(size);
                buffer.flip();
                newBuffer.put(buffer);
                return newBuffer;
            }
        };

        io.set(openProc, (pFileIO, pFile) -> {
        }, MemoryUtil.NULL);

        // TODO: Allow the user to choose its own tags.
        AIScene scene = Assimp.aiImportFile(data.getName(), DEFAULT_ASSIMP_FLAGS);
        if (scene == null) {
            throw new MercuryException("Error while loading model '" + data + "': " + Assimp.aiGetErrorString());
        }

        scene.mRootNode().mTransformation(AIMatrix4x4.create().set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1));

        var materials = assetManager.loadMaterial("/materials/unlit.json");
        assert materials[2] != null;
        materials[2].getFirstShader();

        var result = readScene(scene, materials[2], configFlags);

        /*
         * Release the imported scene when finished.
         */
        Assimp.aiReleaseImport(scene);

        return result;
    }

    private AnimaMundi readScene(AIScene scene, Material template, int configFlags) {
        var ignore = ConfigFlag.hasFlag(ConfigFlag.IGNORE_ROOT_NODE, configFlags);

        var materialCount = scene.mNumMaterials();
        var surfaces = Array.ofType(Surface.class, materialCount);
        for (var i = 0; i < materialCount; ++i) {
            var material = AIMaterial.create(scene.mMaterials().get(i));
            surfaces.add(processMaterial(material));
        }

        var aiMeshes = scene.mMeshes();
        var meshCount = scene.mNumMeshes();

        /*
         * If the flag is set to ignore root-node and we only have one mesh in the whole
         * scene, directly return the PhysicaMundi with the converted mesh data.
         */
        if (ignore && meshCount == 1) {
            var aiMesh = AIMesh.create(aiMeshes.get(0));
            return processMesh(aiMesh, surfaces, template);
        }

        var rootNode = scene.mRootNode();
        var rootNucleus = processNode(rootNode, scene, surfaces, template);

        return rootNucleus;
    }

    private Surface processMaterial(AIMaterial material) {

        var result = Allocator.stackSafe(stack -> {
            var aiName = AIString.create();
            Assimp.aiGetMaterialString(material, Assimp.AI_MATKEY_NAME, Assimp.aiTextureType_NONE, 0, aiName);

            var name = aiName.dataString();
            logger.info("Loading material '" + name + "' using Assimp...");

            var aiTexturePath = AIString.callocStack(stack);
            Assimp.aiGetMaterialTexture(material, Assimp.aiTextureType_DIFFUSE, 0, aiTexturePath, (IntBuffer) null,
                    null, null, null, null, null);

            var texturePath = aiTexturePath.dataString();
            if (texturePath != null && texturePath.length() > 0) {
                texturePath = "model/sponza" + File.separator + new File(texturePath).getName();
            }

            var surface = new Surface();
            if (texturePath != null && !texturePath.isEmpty()) {
                Texture2D texture = assetManager.loadTexture2D(texturePath)
                        .setFilter(MinFilter.TRILINEAR, MagFilter.BILINEAR)
                        .setWrapMode(WrapMode.REPEAT, WrapMode.REPEAT);
                surface.diffuse = texture;
            }

            var buffer = stack.mallocInt(1);
            var out = stack.ints(1);

            if (readMaterialInfo(material, Assimp.AI_MATKEY_ENABLE_WIREFRAME, buffer, out)) {
                var wireframe = new PolygonModeState();
                var value = buffer.get(0);
                // Requesting wireframing render mode.
                if (value == 1) {
                    wireframe.setPolygonMode(Face.FRONT_AND_BACK, PolygonMode.LINE);
                    wireframe.enable();
                    surface.renderStates.put(wireframe.type(), wireframe);
                }
            }

            if (readMaterialInfo(material, Assimp.AI_MATKEY_TWOSIDED, buffer, out)) {
                var culling = new FaceCullingState();
                var value = buffer.get(0);
                // Not two-sided, we can cull back faces.
                if (value == 0) {
                    culling.setFace(Face.BACK);
                    culling.enable();
                }

                // Add the state even if its default because the renderer might
                // defaulting to culling back faces.
                surface.renderStates.put(culling.type(), culling);
            }

            return surface;
        });

        return result;
    }

    private NucleusMundi processNode(AINode node, AIScene scene, Array<Surface> materials, Material template) {
        NucleusMundi nucleus = new NucleusMundi(node.mName().dataString());
        logger.info("Processing node: " + nucleus);

        var transform = new Transform();
        transform.set(convertMatrix(node.mTransformation(), new Matrix4f()));
        nucleus.setTransform(transform);

        // Handle the meshes of the node.
        var nodeMeshes = node.mMeshes();
        for (int i = 0, count = node.mNumMeshes(); i < count; ++i) {
            var index = nodeMeshes.get(i);
            var mesh = AIMesh.create(scene.mMeshes().get(index));
            nucleus.attach(processMesh(mesh, materials, template));
        }

        // Handle the children of the node.
        for (int i = 0, count = node.mNumChildren(); i < count; ++i) {
            nucleus.attach(processNode(AINode.create(node.mChildren().get(i)), scene, materials, template));
        }

        return nucleus;
    }

    /**
     * Create and return a new {@link AnimaMundi} based on the provided <code>Assimp</code> mesh data.
     * 
     * @param aiMesh The mesh data to create the anima-mundi from (not null).
     * @return       A new anima-mundi with a mesh matching the one loaded with assimp
     *               (not null).
     */
    private AnimaMundi processMesh(AIMesh aiMesh, Array<Surface> materials, Material template) {
        Validator.nonNull(aiMesh, "The Assimp mesh data can't be null!");
        var mesh = new Mesh();

        toFloatBuffer(aiMesh.mVertices(), 3, b -> mesh.setupBuffer(VertexBufferType.POSITION, Usage.STATIC_DRAW, b));
        toFloatBuffer(aiMesh.mTextureCoords(0), 2,
                b -> mesh.setupBuffer(VertexBufferType.TEX_COORD, Usage.STATIC_DRAW, b));
        toFloatBuffer(aiMesh.mNormals(), 3, b -> mesh.setupBuffer(VertexBufferType.NORMAL, Usage.STATIC_DRAW, b));
        toFloatBuffer(aiMesh.mTangents(), 3, b -> mesh.setupBuffer(VertexBufferType.TANGENT, Usage.STATIC_DRAW, b));

        toIntBuffer(aiMesh.mFaces(), aiMesh.mNumVertices(), b -> mesh.setupIndexBuffer(b));

        var mode = convertPrimitive(aiMesh.mPrimitiveTypes());
        mesh.setMode(mode);

        // The mesh should be uploaded by the Renderer only.
        var physica = new PhysicaMundi(aiMesh.mName().dataString(), mesh);
        logger.info("Processing geometry " + physica);

        var material = template.copyShader();
        // Check if the mesh uses materials.
        var matIndex = aiMesh.mMaterialIndex();
        if (matIndex >= 0 && matIndex < materials.size()) {
            var surface = materials.get(matIndex);
            var texture = surface.diffuse;
            material.addData("texture_sampler", texture);

            for (var state : surface.renderStates.values()) {
                physica.setRenderStates(state);
            }
        }

        physica.setMaterial(material);

        return physica;
    }

    /**
     * Converts the given <code>Assimp</code> primitive type to the corresponding {@link Mode} for 
     * the {@link Mesh} to create.
     * 
     * @param type The primitive type to convert.
     * @return     A mesh mode corresponding to the assimp library primitive type (not
     *             null).
     * 
     * @throws UnsupportedOperationException Thrown if the primitive type isn't
     *                                       handled by the loader.
     */
    private Mesh.Mode convertPrimitive(int type) {
        switch (type) {
        case Assimp.aiPrimitiveType_POINT:
            return Mode.POINTS;
        case Assimp.aiPrimitiveType_LINE:
            return Mode.LINES;
        case Assimp.aiPrimitiveType_TRIANGLE:
            return Mode.TRIANGLES;
        default:
            throw new UnsupportedOperationException("Unsupported primitive type with index: '" + type + "'");
        }
    }

    /**
     * Converts the given <code>Assimp</code> 4x4 matrix into a {@link Matrix4f}
     * readable by a {@link Transform}.
     * 
     * @param m     The 4x4 matrix to convert (not null).
     * @param store The matrix to store the result in or null to instantiate a new
     *              one.
     * @return      The store matrix containing the converted data or a new instanced
     *              one.
     */
    private Matrix4f convertMatrix(AIMatrix4x4 m, Matrix4f store) {
        Validator.nonNull(m, "The Assimp matrix can't be null!");
        var result = store == null ? new Matrix4f() : store;
        return result.set(m.a1(), m.a2(), m.a3(), m.a4(), m.b1(), m.b2(), m.b3(), m.b4(), m.c1(), m.c2(), m.c3(),
                m.c4(), m.d1(), m.d2(), m.d3(), m.d4());
    }

    private boolean readMaterialInfo(AIMaterial material, String key, IntBuffer store, IntBuffer out) {
        store.clear();
        var result = Assimp.aiGetMaterialIntegerArray(material, key, Assimp.aiTextureType_NONE, 0, store, out);
        // Make sure the info is present.
        return result == Assimp.aiReturn_SUCCESS && out.get(0) == 1;
    }

    private void toIntBuffer(AIFace.Buffer source, int verticesCount, Consumer<Buffer> consumer) {
        var count = source != null ? source.remaining() : 0;
        if (count == 0) {
            return;
        }

        var buffer = BufferUtils.createIndicesBuffer(count * 3, verticesCount);
        for (var i = 0; i < count; ++i) {
            var face = source.get();
            BufferUtils.put(buffer, face.mIndices());
        }

        consumer.accept(buffer);
    }

    private void toFloatBuffer(AIVector3D.Buffer source, int componentSize, Consumer<FloatBuffer> consumer) {
        var count = source != null ? source.remaining() : 0;
        if (count == 0) {
            // FIXME : Should we send an empty vertex buffer here ?
            return;
        }

        var buffer = BufferUtils.createFloatBuffer(count * componentSize);
        for (var i = 0; i < count; ++i) {
            var vec = source.get();
            buffer.put(vec.x());
            buffer.put(vec.y());

            if (componentSize > 2) {
                buffer.put(vec.z());
            }
        }
        buffer.flip();

        consumer.accept(buffer);
    }

    @Override
    public void registerAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public class Surface {

        Texture2D diffuse;

        EnumMap<RenderState.Type, RenderState> renderStates = new EnumMap<>(RenderState.Type.class);

        Surface() {}
    }

    /**
     * <code>ConfigFlag</code> contains various flag used to modify the way an
     * {@link AssimpLoader} will load a scene, a mesh or a material.
     * 
     * @author GnosticOccultist
     */
    public static class ConfigFlag {

        /**
         * Whether the loader should ignore the root-node, preventing it to be added to
         * the loaded scene. It can be useful in some cases where the user has already a
         * root node defined and just want the children.
         */
        public static final int IGNORE_ROOT_NODE = 0x1;
        /**
         * Whether the loader should load and apply the textures to the loaded scene.
         */
        public static final int LOAD_TEXTURE = 0x2;

        /**
         * Return whether the provided flags are containing the given one.
         * 
         * @param flag  The configuration flag to check presence of.
         * @param flags The configuration flags in which to search the flag.
         * @return      Whether the flag is described in the provided ones.
         */
        public static boolean hasFlag(int flag, int flags) {
            return (flags & flag) != 0x0;
        }
    }
}
