package fr.mercury.nucleus.asset.loader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.file.json.AlchemyJSON;
import fr.alchemy.utilities.file.json.JSONArray;
import fr.alchemy.utilities.file.json.JSONObject;
import fr.alchemy.utilities.file.json.JSONValue;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource.ShaderType;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexAttribute;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.utils.MercuryException;

public class MaterialLoader implements AssetLoader<Material[]> {

    /**
     * The logger of the application.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.app");
    /**
     * The maximum authorized defines per shader.
     */
    private static final int MAX_DEFINES = 20;
    /**
     * The JSON material asset loader descriptor.
     */
    public static final AssetLoaderDescriptor<MaterialLoader> DESCRIPTOR = new AssetLoaderDescriptor<>(
            MaterialLoader::new, 
            FileExtensions.JSON_FORMAT
    );

    /**
     * The asset manager managing this asset loader.
     */
    private AssetManager assetManager;
    /**
     * The buffer used to append defines.
     */
    private final StringBuffer buffer = new StringBuffer(MAX_DEFINES);

    @Override
    public Material[] load(String path) {
        var extension = FileUtils.getExtension(path);

        if (FileExtensions.JSON_FORMAT.equals(extension)) {
            try (final InputStreamReader isr = FileUtils.readStream(path)) {
                return loadJSON(isr);
            } catch (IOException ex) {
                logger.error("An error has occured while reading material file '" + path + "' !", ex);
            }
        }

        throw new MercuryException("Unable to load material from file with extension: " + extension);
    }

    private Material[] loadJSON(Reader reader) throws IOException {

        JSONObject object = AlchemyJSON.parse(reader).asObject();
        Material[] materials = new Material[object.size()];
        // Perform the action for all possible materials described in the file.
        for (int i = 0; i < object.size(); i++) {
            var name = object.names().get(i);
            var matObj = object.get(name).asObject();

            // Retrieve the potential description of the material.
            String description = null;
            if (matObj != null) {
                var descrObj = matObj.get("description");
                if (descrObj != null) {
                    description = descrObj.asString();
                }
            }

            // Prevent having a null name for material.
            if (name == null || name.isEmpty()) {
                name = "Undefined";
            }

            // Create the material with the data.
            Material mat = new Material(name, description);
            // Load the specified shaders in the file into the material.
            loadShaders(mat, matObj);

            // Load the possibly declared uniforms.
            loadUniforms(mat, matObj);

            // Load the attributes used in the shaders.
            loadAttributes(mat, matObj);

            logger.info("Successfully loaded material '" + name + "' !");
            materials[i] = mat;
        }

        return materials;
    }

    private void loadShaders(Material mat, JSONObject matObj) throws IOException {

        var shaders = matObj.getOptional("shaders").orElseThrow(IOException::new).asArray();

        for (int i = 0; i < shaders.size(); i++) {
            var shader = shaders.get(i).asObject();

            // Retrieve the type of shader.
            var shaderType = shader.get("type").asString();
            // Retrieve also the path to the source of the shader.
            var shaderPath = shader.get("source").asString();

            // Check that the retrieved type and the extension of the source path
            // correspond.
            var extension = FileUtils.getExtension(shaderPath);
            if (ShaderType.fromExtension(extension) != ShaderType.valueOf(shaderType.toUpperCase())) {
                throw new IllegalStateException("The specified shader type '" + shaderType + "' doesn't "
                        + "correspond to the source path extension '" + extension + "' !");
            }

            var source = assetManager.loadShaderSource(shaderPath);
            // Loads the defines for the source code.
            loadDefines(source, shader);

            // Add the shader source to the material.
            mat.addShaderSource(mat.getName(), source);
        }
    }

    /**
     * Loads the declared defines, if any, from the provided shader
     * {@link JSONObject} and set them to the given {@link ShaderSource}.
     * 
     * @param source    The source to fill with defines.
     * @param shaderObj The shader JSON object that can possibly contain the
     *                  defines.
     */
    private void loadDefines(ShaderSource source, JSONObject shaderObj) {
        // Try accessing the defines is specified in the JSON file.
        var definesOpt = shaderObj.getOptional("defines").map(JSONArray.class::cast);

        if (definesOpt.isPresent() && !definesOpt.get().isEmpty()) {
            // Hurrah, we found some defines values.
            var defines = definesOpt.get();
            // Append them to the buffer...
            for (int i = 0; i < defines.size(); i++) {
                buffer.append("#define " + defines.get(i).asString() + "\n");
            }
            // ... and set the content of the buffer as the defines of the shader source.
            source.setDefines(buffer.toString());
        }
    }

    /**
     * Loads the declared uniforms, if any, from the provided material
     * {@link JSONObject} and add them to the given {@link Material}.
     * 
     * @param mat    The material to add the uniforms to.
     * @param matObj The material JSON object that can possibly contain the defines.
     */
    private void loadUniforms(Material mat, JSONObject matObj) {
        // Try accessing the uniforms object and the prefab uniforms array.
        var uniformsOpt = matObj.getOptional("uniforms").map(JSONObject.class::cast);
        var prefabsOpt = uniformsOpt.isPresent() ? uniformsOpt.get().getOptional("prefabs").map(JSONArray.class::cast)
                : Optional.empty().map(JSONArray.class::cast);

        if (prefabsOpt.isPresent() && !prefabsOpt.get().isEmpty()) {
            // Hurrah, some prefab uniform are present.
            var prefabs = prefabsOpt.get();
            // Add them to the material, to be later asked to the renderer.
            for (int i = 0; i < prefabs.size(); i++) {
                mat.getPrefabUniforms().add(prefabs.get(i).asString());
            }
        }
    }

    /**
     * Loads the declared attributes, from the provided material {@link JSONObject}
     * and add them to the given {@link Material}.
     * 
     * @param mat    The material to add the attributes to.
     * @param matObj The material JSON object that contains the attributes.
     */
    private void loadAttributes(Material mat, JSONObject matObj) throws IOException {
        // Try accessing the attributes or throw an exception.
        var attributes = matObj.getOptional("attributes").orElseThrow(IOException::new).asArray();

        for (int i = 0; i < attributes.size(); i++) {
            var attribObj = attributes.get(i).asObject();

            // Retrieve the name used by the attribute in the shader.
            var name = attribObj.get("name").asString();
            // Retrieve also the location of the attribute, if declared.
            var location = attribObj.getOptional("location").map(JSONValue::asInt).orElse(-1);
            // Retrieve also the location of the attribute, if declared.
            var bufferType = attribObj.getOptional("bufferType").map(JSONValue::asString).orElse(null);
            // Retrieve also the stride of the attribute, if declared.
            var stride = attribObj.getOptional("stride").map(JSONValue::asInt).orElse(0);
            // Retrieve also the offset of the attribute, if declared.
            var offset = attribObj.getOptional("offset").map(JSONValue::asInt).orElse(0);
            // Retrieve also the divisor of the attribute, if declared.
            var divisor = attribObj.getOptional("divisor").map(JSONValue::asInt).orElse(0);
            // Retrieve also the span of the attribute, if declared.
            var span = attribObj.getOptional("span").map(JSONValue::asInt).orElse(1);

            var attrib = new VertexAttribute(name, bufferType, location, stride, offset, divisor, span);
            mat.addAttribute(attrib);
        }
    }

    /**
     * Register the asset manager which instantiated this <code>MaterialLoader</code>, 
     * to use it for loading {@link ShaderSource} from specified file paths.
     */
    @Override
    public void registerAssetManager(AssetManager assetManager) {
        Validator.nonNull(assetManager);
        this.assetManager = assetManager;
    }
}
