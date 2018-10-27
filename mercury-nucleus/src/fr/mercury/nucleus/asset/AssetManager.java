package fr.mercury.nucleus.asset;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.file.FileUtils;
import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform;
import fr.mercury.nucleus.scene.PhysicaMundi;
import fr.mercury.nucleus.texture.Texture2D;
import fr.mercury.nucleus.utils.MercuryException;

/**
 * <code>AssetManager</code> manages all the assets which can be used inside an <code>Application</code>.
 * For example, it will be able to load an asset with a registered <code>AssetLoader</code> and translate a physical
 * file into a virtual object which can be later used inside a {@link ShaderProgram}, ... 
 * 
 * @author GnosticOccultist
 */
public class AssetManager {
	
	/**
	 * The table containing the asset loaders ordered by their extensions.
	 */
	private final Map<String[], AssetLoader<?>> loaders = new HashMap<>();
	
	/**
	 * Instantiates a new <code>AssetManager</code>. 
	 * Calling this constructor shouldn't be necessary because an usable instance of the manager is already 
	 * present inside the {@link MercuryApplication}.
	 */
	public AssetManager() {
		registerLoader(GLSLLoader.class, FileExtensions.SHADER_FILE_EXTENSIONS);
		registerLoader(SimpleOBJLoader.class, new String[] {FileExtensions.OBJ_MODEL_FORMAT});
		registerLoader(ImageReader.class, FileExtensions.TEXTURE_FILE_EXTENSION);
	}
	
	public PhysicaMundi loadPhysicaMundi(String path) {
		AssetLoader<PhysicaMundi> loader = acquireLoader(path);
		if(loader != null) {
			return loader.load(path);
		}
		
		throw new MercuryException("The asset '" + path + "' cannot be loaded using the registered loaders.");
	}
	
	/**
	 * Loads the provided asset by translating it into a <code>Texture2D</code>
	 * using a {@link ImageReader}. The texture can then be used inside a <code>ShaderProgram</code>,
	 * by creating an adapted {@link Uniform}.
	 * <p>
	 * If no loader is found it will throw an exception.
	 * 
	 * @param path The path of the asset to load, must be a texture file extension.
	 * @return	   The loaded texture or null.
	 */
	public Texture2D loadTexture2D(String path) {
		AssetLoader<Texture2D> loader = acquireLoader(path);
		if(loader != null) {
			return loader.load(path);
		}
		
		throw new MercuryException("The asset '" + path + "' cannot be loaded using the registered loaders.");
	}
	
	/**
	 * Loads the provided asset by translating it into a <code>ShaderSource</code>
	 * using a {@link GLSLLoader}. The source can then be used inside a <code>ShaderProgram</code>,
	 * by calling {@link ShaderProgram#attachSource(ShaderSource)}.
	 * <p>
	 * If no loader is found it will throw an exception.
	 * 
	 * @param path The path of the asset to load, must be a shader file extension.
	 * @return	   The loaded shader source or null.
	 */
	public ShaderSource loadShaderSource(String path) {
		AssetLoader<ShaderSource> loader = acquireLoader(path);
		if(loader != null) {
			return loader.load(path);
		}
		
		throw new MercuryException("The asset '" + path + "' cannot be loaded using the registered loaders.");
	}

	/**
	 * Acquire an appropriate <code>AssetLoader</code> for the asset by checking
	 * its extension. If no loader is found it returns null.
	 * 
	 * @param path The path of the asset file.
	 * @return	   The corresponding asset loader or null.
	 */
	@SuppressWarnings("unchecked")
	public <T> AssetLoader<T> acquireLoader(String path) {
		String extension = FileUtils.getExtension(path);
		
		for(Entry<String[], AssetLoader<?>> loader : loaders.entrySet()) {
			for(String ext : loader.getKey()) {
				if(ext.equals(extension)) {
					return (AssetLoader<T>) loader.getValue();
				}
			}
		}
		
		System.err.println("No asset loaders are registered for the extension: '" + extension + "'.");
		return null;
	}
	
	/**
	 * Register a specified type of <code>AssetLoader</code> with the readable extensions.
	 * 
	 * @param type		 The type of loader to register.
	 * @param extensions The readable extensions with this loader.
	 */
	public <T extends AssetLoader<?>> void registerLoader(Class<T> type, String[] extensions) {
		AssetLoader<?> instance = null;
		try {
			instance = type.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			System.err.println("Failed to instantiate the loader: " + type + 
					". Please check that the constructor arguments are empty.");
			e.printStackTrace();
		}
		
		if(instance != null) {
			loaders.put(extensions, instance);
		}
	}
}
