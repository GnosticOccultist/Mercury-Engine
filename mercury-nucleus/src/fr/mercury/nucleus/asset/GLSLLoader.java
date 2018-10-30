package fr.mercury.nucleus.asset;

import java.io.BufferedReader;
import java.io.IOException;

import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource.ShaderType;

/**
 * <code>GLSLLoader</code> is a loader which is capable of loading 'glsl' files.
 * <p>
 * It is principally used for loading <code>ShaderSource</code> code.
 * 
 * @author GnosticOccultist
 */
public final class GLSLLoader implements AssetLoader<ShaderSource> {
	
	/**
	 * Load the <code>ShaderSource</code> from the provided file path.
	 * 
	 * @param path The file path.
	 * @return     The readed shader source code.
	 */
	public ShaderSource load(String path) {
		
		StringBuilder sb = new StringBuilder();
		try (final BufferedReader bufferedReader = FileUtils.read(path)) {
			
			String line = null;
			
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line).append('\n');
			}
			
		} catch (IOException ex) {
			FactoryLogger.getLogger("mercury.app")
					.error("Failed to load shader source: " + path);
			ex.printStackTrace();
        }
		
		ShaderSource shaderSource = new ShaderSource(ShaderType.fromExtension(
				FileUtils.getExtension(path)), sb.toString());
		
		return shaderSource;
	}
}
