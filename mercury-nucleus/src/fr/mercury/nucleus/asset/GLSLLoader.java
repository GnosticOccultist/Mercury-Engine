package fr.mercury.nucleus.asset;

import java.io.BufferedReader;
import java.io.IOException;

import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource.ShaderType;

/**
 * <code>GLSLLoader</code> is a loader which is capable of loading 'glsl' files.
 * <p>
 * It is principally used for loading {@link ShaderSource} code, but can also
 * be used to inject imported 'glsl' file, if specified so in the source, using 
 * {@value #IMPORT_TAG} followed by the path of the file to import.
 * 
 * @author GnosticOccultist
 */
public final class GLSLLoader implements AssetLoader<ShaderSource> {
	
	/**
	 * The logger of the application.
	 */
	private static final Logger logger = FactoryLogger.getLogger("mercury.app");
	/**
	 * The tag for import glsl file inside the shader source.
	 */
	private static final String IMPORT_TAG = "#import";
	
	/**
	 * Load the <code>ShaderSource</code> from the provided file path.
	 * 
	 * @param path The file path.
	 * @return     The readed shader source code.
	 */
	public ShaderSource load(String path) {
		
		StringBuilder sb = new StringBuilder();
		try (final BufferedReader bufferedReader = FileUtils.readBuffered(path)) {
			
			String line = null;
			
			while ((line = bufferedReader.readLine()) != null) {
				
				if(line.startsWith(IMPORT_TAG)) {
					
					String importPath = line.trim().substring(IMPORT_TAG.length() + 1).trim();
					// If the import begins with quotes, remove them before reading the file.
					if(importPath.startsWith("\"") && importPath.endsWith("\"") && importPath.length() > 3) {
						importPath = importPath.substring(1, importPath.length() - 1);
					}
					// It shouldn't need to import the main-file itself into it.
					if(path.equals(importPath)) {
						throw new IOException(path + " cannot import itself!");
					}
					
					// Read the import file and inject its content in the string builder.
					readImport(importPath, sb);
					
					logger.info("Successfully imported: " + importPath);
				} else {
					sb.append(line).append('\n');
				}
			}
			
			logger.info("Successfully loaded shader source: " + path);
			
		} catch (IOException ex) {
			logger.error("Failed to load shader source: " + path + " Error: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		ShaderSource shaderSource = new ShaderSource(ShaderType.fromExtension(
				FileUtils.getExtension(path)), sb.toString());
		
		return shaderSource;
	}
	
	/**
	 * Reads the imported 'glsl' file with the provided path, and stores
	 * its content to the provided {@link StringBuilder}.
	 * 
	 * @param path The path of the file to import.
	 * @param sb   The string builder to fill.
	 * @return	   The filled string builder with the imported file content.
	 */
	private StringBuilder readImport(String path, StringBuilder sb) {
		try (final BufferedReader bufferedReader = FileUtils.readBuffered(path)) {
			
			String line = null;
			
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line).append('\n');
			}
			
		} catch (IOException ex) {
			logger.error("Failed to read import: " + path + " Error: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		return sb;
	}
}
