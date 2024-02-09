package fr.mercury.nucleus.asset.loader;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.asset.AssetDescriptor;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.asset.locator.AssetLocator.LocatedAsset;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Vector2f;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.Usage;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.Mesh.Mode;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.data.BufferUtils;

public class OBJLoader implements AssetLoader<AnimaMundi> {

    /**
     * The obj asset loader descriptor.
     */
    public static final AssetLoaderDescriptor<OBJLoader> DESCRIPTOR = new AssetLoaderDescriptor<>(OBJLoader::new,
            FileExtensions.OBJ_MODEL_FORMAT);

    /**
     * The logger of the mercury assets.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.asset");
    /**
     * The vertex type definer inside an .obj file.
     */
    private static final String VERTEX_TYPE = "v";
    /**
     * The texture coordinate type definer inside an .obj file.
     */
    private static final String TEX_COORDS_TYPE = "vt";
    /**
     * The normal type definer inside an .obj file.
     */
    private static final String NORMAL_TYPE = "vn";
    /**
     * The face type definer inside an .obj file.
     */
    private static final String FACE_TYPE = "f";
    /**
     * The object name definer inside an .obj file.
     */
    private static final String OBJECT_NAME = "o";
    /**
     * The group name definer inside an .obj file.
     */
    private static final String GROUP_NAME = "g";
    /**
     * The smoothing groip definer inside an .obj file.
     */
    private static final String SMOOTHING_GROUP_TYPE = "s";
    /**
     * The material library type definer inside an .obj file.
     */
    private static final String MTLLIB_TYPE = "mtllib";
    /**
     * The use material library type definer inside an .obj file.
     */
    private static final String USE_MTLLIB_TYPE = "usemtl";
    /**
     * The new material library type definer inside an .mtl file.
     */
    private static final String NEW_MTL_TYPE = "newmtl";
    /**
     * The ambient color value type definer inside an .mtl file.
     */
    private static final String AMBIENT_TYPE = "Ka";
    /**
     * The diffuse color value type definer inside an .mtl file.
     */
    private static final String DIFFUSE_TYPE = "Kd";

    /**
     * The store used for storing the loaded data.
     */
    private final MeshStore store = new MeshStore();
    /**
     * The current obj material.
     */
    private ObjMaterial currentMaterial;
    /**
     * A material template.
     */
    private Material[] templates;
    /**
     * The asset manager.
     */
    private AssetManager assetManager;
    /**
     * The root nucleus of the model.
     */
    private NucleusMundi nucleus;

    @Override
    public AnimaMundi load(LocatedAsset data) {

        var name = data.asset().getName();

        // Clear the store for the new loaded OBJ file.
        store.clear();
        // Set the name of the geometry to the filename by default.
        store.setName(name);

        try (var reader = FileUtils.readBuffered(data.openStream())) {

            String line = null;
            long currentSmoothGroup = -1;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Ignore any commented lines.
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                // Tokenize the line.
                var tokens = line.split("\\s+");

                if (tokens.length == 0) {
                    continue;
                }

                var type = tokens[0];

                switch (type) {
                case VERTEX_TYPE:
                    var vertex = new Vector3f(Float.valueOf(tokens[1]), Float.valueOf(tokens[2]),
                            Float.valueOf(tokens[3]));
                    if (tokens.length > 4) {
                        logger.warning("Only 3 components is handled per vertex, but "
                                + String.valueOf(tokens.length - 1) + " are defined in the obj file!");
                    }
                    store.addVertex(vertex);
                    break;
                case TEX_COORDS_TYPE:
                    var textureCoords = new Vector2f(Float.valueOf(tokens[1]), Float.valueOf(tokens[2]));
                    if (tokens.length > 3 && Float.valueOf(tokens[3]) != 0) {
                        logger.warning("Only 2 components is handled per texture coordinates, but "
                                + String.valueOf(tokens.length - 1) + " are defined in the obj file!");
                    }
                    store.addTextureCoord(textureCoords);
                    break;
                case NORMAL_TYPE:
                    var normal = new Vector3f(Float.valueOf(tokens[1]), Float.valueOf(tokens[2]),
                            Float.valueOf(tokens[3]));
                    store.addNormal(normal);
                    break;
                case GROUP_NAME:
                    if (tokens.length < 2) {
                        logger.warning("An object name can't be empty!");
                    }

                    // Find all group names and set them to the store.
                    final String[] currentGroupNames = new String[tokens.length - 1];
                    store.setCurrentGroupNames(currentGroupNames);
                    System.arraycopy(tokens, 1, currentGroupNames, 0, tokens.length - 1);
                    break;
                case MTLLIB_TYPE:
                    if (tokens.length < 2) {
                        logger.warning("mtllib must define at least one argument, but found "
                                + String.valueOf(tokens.length - 1) + " arguments.");
                    }

                    // Load material template.
                    // TODO: Use a caching system for assets.
                    if (templates == null) {
                        this.templates = assetManager.loadMaterial("materials/unlit.json");
                    }

                    // Load material libraries.
                    for (var i = 1; i <= tokens.length - 1; i++) {
                        loadMaterialLibrary(tokens[i], data, store.getMaterials());
                    }
                    break;
                case USE_MTLLIB_TYPE:
                    if (tokens.length != 2) {
                        logger.warning("usemtl must define only one argument, but found "
                                + String.valueOf(tokens.length - 1) + " arguments.");
                    }

                    store.setCurrentMaterial(store.getMaterials().get(tokens[1]));
                    break;
                case SMOOTHING_GROUP_TYPE:
                    if (tokens.length != 2) {
                        logger.warning("A smoothing group should only define an index, but found "
                                + String.valueOf(tokens.length - 1) + " arguments.");
                    }
                    if ("off".equalsIgnoreCase(tokens[1])) {
                        currentSmoothGroup = 0;
                    } else {
                        currentSmoothGroup = Long.parseLong(tokens[1]);
                    }
                    break;
                case FACE_TYPE:
                    if (tokens.length < 4) {
                        logger.warning("A face must have at least 3 vertices, but " + String.valueOf(tokens.length - 1)
                                + " are defined in the obj file!");
                    }

                    var size = tokens.length - 1;
                    var indices = Array.ofType(IndexGroup.class);

                    for (int i = 1; i <= size; ++i) {
                        indices.add(new IndexGroup(tokens[i], currentSmoothGroup, store));
                    }
                    store.addFace(indices);
                    break;
                case OBJECT_NAME:
                    if (tokens.length < 2) {
                        logger.warning("An object name can't be empty!");
                    }
                    store.setName(tokens[1]);
                    break;
                }
            }

            store.commit();

            return nucleus;

        } catch (IOException ex) {
            logger.error("Failed to load OBJ resource '" + name + "'!", ex);
            return null;
        }
    }

    private void loadMaterialLibrary(String name, LocatedAsset asset, Map<String, ObjMaterial> materials) {
        var mtlFile = asset.sibling(new AssetDescriptor<>(name));
        
        try (var reader = FileUtils.readBuffered(mtlFile.openStream())) {

            String line = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Ignore any commented lines.
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                // Tokenize the line.
                var tokens = line.split("\\s+");

                if (tokens.length == 0) {
                    continue;
                }

                var type = tokens[0];

                if (NEW_MTL_TYPE.equals(type)) {
                    // Start a new obj material.
                    currentMaterial = new ObjMaterial(tokens[1]);
                    materials.put(tokens[1], currentMaterial);
                    continue;
                }

                if (currentMaterial == null) {
                    throw new IOException("No obj material currently set!");
                }

                switch (type) {
                case AMBIENT_TYPE:
                    var color = new Color(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    currentMaterial.setKa(color);
                    break;
                case DIFFUSE_TYPE:
                    color = new Color(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    currentMaterial.setKd(color);
                    break;
                }
            }

        } catch (IOException ex) {
            logger.error("Failed to load MTL '" + name + "' resource '" + mtlFile + "'!", ex);
        }
    }

    @Override
    public void registerAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * <code>MeshStore</code> is a utility class for storing the vertex data of a
     * geometry loaded from an OBJ file.
     * 
     * @author GnosticOccultist
     */
    protected class MeshStore {

        /**
         * The list of vertices data loaded from a file.
         */
        private final List<Vector3f> vertices = new ArrayList<>();
        /**
         * The list of texture coordinates data loaded from a file.
         */
        private final List<Vector2f> textureCoords = new ArrayList<>();
        /**
         * The list of normals data loaded from a file.
         */
        private final List<Vector3f> normals = new ArrayList<>();
        /**
         * The index set data loaded from a file.
         */
        private final IndexSet indexSet = new IndexSet();
        /**
         * The table containing OBJ materials.
         */
        private final Map<String, ObjMaterial> materials = new HashMap<>();

        /**
         * The name of the loaded geometry.
         */
        private String name;
        /**
         * The array of group names of the loaded geometry.
         */
        private String[] groupNames;
        /**
         * The obj material.
         */
        private ObjMaterial material;
        private int meshCount;

        /**
         * Add a new vertex data as a {@link Vector3f} to the <code>MeshStore</code>.
         * 
         * @param vertex The vertex data loaded from a file.
         * @return The mesh store for chaining purposes.
         */
        public MeshStore addVertex(Vector3f vertex) {
            this.vertices.add(vertex);
            return this;
        }

        /**
         * Add a new texture coordinates data as a {@link Vector2f} to the
         * <code>MeshStore</code>.
         * 
         * @param textureCoords The texture coordinates data loaded from a file.
         * @return The mesh store for chaining purposes.
         */
        public MeshStore addTextureCoord(Vector2f textureCoords) {
            this.textureCoords.add(textureCoords);
            return this;
        }

        /**
         * Add a new normal data as a {@link Vector3f} to the <code>MeshStore</code>.
         * 
         * @param normal The normal data loaded from a file.
         * @return The mesh store for chaining purposes.
         */
        public MeshStore addNormal(Vector3f normal) {
            this.normals.add(normal);
            return this;
        }

        /**
         * Add a new {@link Face} composing by the given {@link IndexGroup} to the
         * <code>MeshStore</code>.
         * 
         * @param groups The index groups loaded from a file.
         * @return The mesh store for chaining purposes.
         */
        public MeshStore addFace(Array<IndexGroup> groups) {
            // Build a triangle fan.
            var first = groups.get(0);
            var firstIndex = indexSet.findIndex(first);
            var second = groups.get(1);
            var secondIndex = indexSet.findIndex(second);
            for (var i = 2; i < groups.size(); ++i) {
                var third = groups.get(i);
                var thirdIndex = indexSet.findIndex(third);
                indexSet.addIndex(firstIndex);
                indexSet.addIndex(secondIndex);
                indexSet.addIndex(thirdIndex);

                second = third;
                secondIndex = thirdIndex;
            }

            return this;
        }

        /**
         * Return the material's stored in the <code>MeshStore</code>.
         * 
         * @return The table containing obj materials (not null).
         */
        public Map<String, ObjMaterial> getMaterials() {
            return materials;
        }

        /**
         * Clears the <code>MeshStore</code> in order to be used for loading a new
         * geometry from an OBJ file.
         */
        public void clear() {
            this.name = null;
            this.groupNames = null;
            this.material = null;

            this.indexSet.clear();
        }

        public void commit() {
            var physica = toMercuryPhysica();
            if (physica == null) {
                return;
            }

            var mat = material.toMercuryMaterial(templates);
            physica.setMaterial(mat);

            if (nucleus == null) {
                nucleus = new NucleusMundi(name);
            }

            nucleus.attach(physica);
            store.clear();
        }

        /**
         * Converts the <code>MeshStore</code> to a {@link Mesh} matching each loaded
         * vertices data from the OBJ file.
         * 
         * @return A new mesh matching the data stored (not null).
         */
        public Mesh toMercuryMesh() {
            if (indexSet.size() <= 0) {
                return null;
            }

            var mesh = new Mesh();

            FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(indexSet.size() * 3);
            FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(indexSet.size() * 2);
            FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(indexSet.size() * 3);
            Buffer buffer = BufferUtils.createIndicesBuffer(indexSet.indicesCount(), indexSet.size() - 1);

            boolean hasNormals, hasTexCoords;

            var j = 0;
            var vertGroups = new long[indexSet.size()];

            var groups = Array.ofType(Long.class);
            for (var indexGroup : indexSet) {
                var smoothGroup = indexGroup.getSmoothingGroup();
                vertGroups[j] = smoothGroup;

                if (!groups.contains(smoothGroup)) {
                    groups.add(smoothGroup);
                }

                var vector = vertices.get(indexGroup.vIndex);
                positionBuffer.put(vector.x()).put(vector.y()).put(vector.z());

                if (indexGroup.vnIndex > IndexGroup.NO_VALUE) {
                    vector = normals.get(indexGroup.vnIndex);
                    normalsBuffer.put(vector.x()).put(vector.y()).put(vector.z());
                    hasNormals = true;
                }
                if (indexGroup.vtIndex > IndexGroup.NO_VALUE) {
                    var v = textureCoords.get(indexGroup.vtIndex);
                    texCoordBuffer.put(v.x()).put(1.0f - v.y());
                    hasTexCoords = true;
                }

                j++;
            }

            for (var index : indexSet.indices) {
                if (buffer instanceof ByteBuffer) {
                    ((ByteBuffer) buffer).put(index, index.byteValue());
                } else if (buffer instanceof ShortBuffer) {
                    ((ShortBuffer) buffer).put(index, index.shortValue());
                } else if (buffer instanceof IntBuffer) {
                    ((IntBuffer) buffer).put(index, index.intValue());
                }
            }

//            for (int i = 0; i < vertices.size(); i++) {
//                Vector3f vertex = vertices.get(i);
//                BufferUtils.populate(positionBuffer, vertex, i);
//            }
//
//            int index = 0;
//            for (Face face : faces) {
//                IndexGroup[] groups = face.getFaceVertexIndices();
//                for (IndexGroup group : groups) {
//                    int vIndex = group.vIndex - 1;
//
//                    if (buffer instanceof ByteBuffer) {
//                        ((ByteBuffer) buffer).put(index, (byte) vIndex);
//                    } else if (buffer instanceof ShortBuffer) {
//                        ((ShortBuffer) buffer).put(index, (short) vIndex);
//                    } else if (buffer instanceof IntBuffer) {
//                        ((IntBuffer) buffer).put(index, vIndex);
//                    }
//
//                    if (group.vtIndex > IndexGroup.NO_VALUE) {
//                        Vector2f texCoords = textureCoords.get(group.vtIndex - 1);
//                        // OpenGL needs the Y-axis to go down, so Y = 1 - V.
//                        texCoords.set(texCoords.x, 1F - texCoords.y);
//                        BufferUtils.populate(texCoordBuffer, texCoords, vIndex);
//                    }
//
//                    if (group.vnIndex > IndexGroup.NO_VALUE) {
//                        Vector3f normal = normals.get(group.vnIndex - 1);
//                        BufferUtils.populate(normalsBuffer, normal, vIndex);
//                    }
//                    index++;
//                }
//            }

            mesh.setupBuffer(VertexBufferType.POSITION, Usage.STATIC_DRAW, positionBuffer);
            mesh.setupBuffer(VertexBufferType.TEX_COORD, Usage.STATIC_DRAW, texCoordBuffer);
            mesh.setupBuffer(VertexBufferType.NORMAL, Usage.STATIC_DRAW, normalsBuffer);

            mesh.setupIndexBuffer(buffer);

            mesh.setMode(Mode.TRIANGLES);

            meshCount++;

            // The mesh should be uploaded by the Renderer only.
            return mesh;
        }

        /**
         * Converts the <code>MeshStore</code> to a {@link PhysicaMundi} matching each
         * loaded vertices data and the name from the OBJ file.
         * 
         * @return A new physica-mundi matching the data stored (not null).
         */
        public PhysicaMundi toMercuryPhysica() {
            if (name == null || name.isEmpty()) {
                this.name = "obj_mesh";
            }

            var mesh = toMercuryMesh();
            if (mesh == null) {
                return null;
            }

            var physica = new PhysicaMundi(name, toMercuryMesh());
            return physica;
        }

        /**
         * Sets the name of the {@link PhysicaMundi} which will be created from the OBJ
         * file.
         * 
         * @param name The desired name of the physica-mundi.
         */
        void setName(String name) {
            commit();
            this.name = name;
        }

        /**
         * Sets the group names of the current geometry being read from the OBJ file.
         * 
         * @param groupNames The array of group names.
         */
        void setCurrentGroupNames(String[] groupNames) {
            commit();
            this.groupNames = groupNames;
        }

        /**
         * Set the {@link ObjMaterial} for the mesh.
         * 
         * @param material The obj material.
         */
        void setCurrentMaterial(ObjMaterial material) {
            if (material != null) {
                commit();
                this.material = material;
            }
        }
    }

    protected static class IndexSet implements Iterable<IndexGroup> {

        private final Map<IndexGroup, Integer> indexStore = new HashMap<>();

        private final Array<Integer> indices = Array.ofType(Integer.class);

        public int findIndex(IndexGroup set) {
            if (indexStore.containsKey(set)) {
                return indexStore.get(set);
            }

            var index = indexStore.size();
            indexStore.put(set, index);
            return index;
        }

        public void addIndex(int index) {
            this.indices.add(index);
        }

        public int size() {
            return indexStore.size();
        }

        public int indicesCount() {
            return indices.size();
        }

        public void clear() {
            this.indices.clear();
            this.indexStore.clear();
        }

        @Override
        public Iterator<IndexGroup> iterator() {
            return indexStore.keySet().iterator();
        }
    }

    /**
     * <code>Face</code> is composed of {@link IndexGroup}, for example 3 in a
     * triangle shape. This class is used to correctly get the indices used when
     * rendering a {@link Mesh}.
     * 
     * @author GnosticOccultist
     */
    protected static class Face {

        /**
         * The array of indices groups for the face.
         */
        private IndexGroup[] indicesGroups;

        /**
         * Instantiates a new <code>Face</code> with the provided {@link IndexGroup},
         * the number depending on the type of the face. For a triangle shape needs to
         * define 3 indices groups.
         * 
         * @param groups The indices groups composing the face (not null, not empty).
         */
        public Face(IndexGroup[] groups) {
            Validator.nonEmpty(groups, "The indices groups can't be null!");
            this.indicesGroups = groups;
        }

        /**
         * Return the array of {@link IndexGroup} composing the <code>Face</code>.
         * 
         * @return The array of indices groups composing the face (not null, not empty).
         */
        public IndexGroup[] getFaceVertexIndices() {
            return indicesGroups;
        }
    }

    /**
     * <code>IndexGroup</code> is a utility class for storing the index of data of a
     * {@link Face} vertex in an OBJ file.
     * 
     * @author GnosticOccultist
     */
    protected static class IndexGroup {

        /**
         * The no value for an index value &rarr;-1
         */
        public static final int NO_VALUE = -1;

        /**
         * The position index for the vertex.
         */
        public int vIndex;
        /**
         * The texture coordinate index for the vertex.
         */
        public int vtIndex;
        /**
         * The normal index for the vertex.
         */
        public int vnIndex;
        /**
         * The smoothing group for the index group.
         */
        private long smoothingGroup;

        public IndexGroup(String group, long smoothingGroup, MeshStore store) {
            var tokens = group.split("/");
            this.vIndex = tokens.length < 1 ? NO_VALUE : parseValue(tokens[0], store.vertices.size());
            // Here we check that the texture coordinate index exist, as an OBJ file may
            // define normals without them.
            this.vtIndex = tokens.length < 2 ? NO_VALUE : parseValue(tokens[1], store.textureCoords.size());
            this.vnIndex = tokens.length < 3 ? NO_VALUE : parseValue(tokens[2], store.normals.size());
            this.smoothingGroup = smoothingGroup;
        }

        private int parseValue(String token, int currentPosition) {
            if (token == null || token.isEmpty()) {
                return NO_VALUE;
            }

            var value = Integer.parseInt(token);
            if (value < 0) {
                value += currentPosition;
            } else {
                // OBJ is 1 based, so drop 1.
                value--;
            }
            return value;
        }

        /**
         * Return the smoothing group index of the <code>IndexGroup</code>.
         * 
         * @return The smoothing group index, or 0 if it doesn't use smoothing.
         */
        public long getSmoothingGroup() {
            // A normal index has been defined, so we don't use smoothing group.
            if (vnIndex > NO_VALUE) {
                return 0;
            }
            return smoothingGroup;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + vIndex;
            result = 31 * result + vtIndex;
            result = 31 * result + vnIndex;
            result = 31 * result + (int) (smoothingGroup ^ smoothingGroup >>> 32);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof IndexGroup)) {
                return false;
            }
            var o = (IndexGroup) obj;
            return vIndex == o.vIndex && vnIndex == o.vnIndex && vtIndex == o.vtIndex
                    && smoothingGroup == o.smoothingGroup;
        }
    }

    /**
     * <code>ObjMaterial</code> is a utility class for storing OBJ material values
     * in an MTL file.
     * 
     * @author GnosticOccultist
     */
    protected static class ObjMaterial {

        private final String name;

        private Color Ka = null;

        private Color Kd = null;

        public ObjMaterial(String name) {
            this.name = name;
        }

        public Material toMercuryMaterial(Material[] templates) {
            templates[0].getFirstShader();
            var mat = templates[0].copy();
            System.out.println(Kd);
            mat.addVariable("diffuseColor", Kd);

            return mat;
        }

        void setKa(Color value) {
            this.Ka = value;
        }

        void setKd(Color value) {
            this.Kd = value;
        }
    }
}
