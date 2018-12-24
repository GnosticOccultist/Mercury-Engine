package fr.mercury.nucleus.asset;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.file.json.AlchemyJSON;
import fr.alchemy.utilities.file.json.JSONObject;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource.ShaderType;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.utils.MercuryException;

public class MaterialLoader implements AssetLoader<Material[]> {

	/**
	 * The logger of the application.
	 */
	private static final Logger logger = FactoryLogger.getLogger("mercury.app");
	/**
	 * The asset manager managing this asset loader.
	 */
	private AssetManager assetManager;
	
	@Override
	public Material[] load(String path) {
		var extension = FileUtils.getExtension(path);
		
		if(FileExtensions.JSON_FORMAT.equals(extension)) {
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
		for(int i = 0; i < object.size(); i++) {
			var name = object.names().get(i);
			var matObj = object.get(name).asObject();
			
			// Retrieve the potential description of the material.
			String description = null;
			if(matObj != null) {
				var descrObj = matObj.get("description");
				if(descrObj != null) {
					description = descrObj.asString();
				}
			}
			
			// Prevent having a null name for material.
			if(name == null || name.isEmpty()) {
				name = "Undefined";
			}
			
			// Create the material with the data.
			Material mat = new Material(name, description);
			// Load the specified shaders in the file into the material.
			loadShaders(mat, matObj);
			
			logger.info("Successfully loaded material '" + name + "' !");
			materials[i] = mat;
		}
		
		return materials;
	}

	private void loadShaders(Material mat, JSONObject matObj) {
		
		var shaders = matObj.get("shaders").asArray();
		
		for(int i = 0; i < shaders.size(); i++) {
			var shader = shaders.get(i).asObject();
			
			// Retrieve the type of shader.
			var shaderType = shader.get("type").asString();
			// Retrieve also the path to the source of the shader.
			var shaderPath = shader.get("source").asString();
			
			// Check that the retrieved type and the extension of the source path correspond.
			var extension = FileUtils.getExtension(shaderPath);
			if(ShaderType.fromExtension(extension) != ShaderType.valueOf(shaderType.toUpperCase())) {
				throw new IllegalStateException("The specified shader type '" + shaderType + "' doesn't "
						+ "correspond to the source path extension '" + extension + "' !");
			}
			
			var source = assetManager.loadShaderSource(shaderPath);
			mat.addShaderSource(mat.getName(), source);
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
