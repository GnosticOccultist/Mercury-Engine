package fr.mercury.nucleus.asset.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;

import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.asset.loader.data.AssetData;
import fr.mercury.nucleus.asset.loader.data.PathAssetData;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource.ShaderType;

/**
 * <code>GLSLLoader</code> is a loader which is capable of loading 'glsl' files.
 * <p>
 * It is principally used for loading {@link ShaderSource} code, but can also be
 * used to inject imported 'glsl' file, if specified so in the source, using
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
     * The glsl asset loader descriptor.
     */
    public static final AssetLoaderDescriptor<GLSLLoader> DESCRIPTOR = new AssetLoaderDescriptor<>(
            GLSLLoader::new,
            FileExtensions.SHADER_FILE_EXTENSIONS
    );

    /**
     * Load the <code>ShaderSource</code> from the provided file path.
     * 
     * @param path The asset data of the file to read.
     * @return     The readed shader source code.
     */
    @Override
    public ShaderSource load(AssetData data) {

        StringBuilder sb = new StringBuilder();
        read(data, sb);

        ShaderSource shaderSource = new ShaderSource(ShaderType.fromExtension(FileUtils.getExtension(data.getName())),
                sb.toString());

        return shaderSource;
    }

    /**
     * Reads the content of the file with the provided path, and stores its content
     * to the provided {@link StringBuilder}.
     * <p>
     * Whenever the {@link BufferedReader} encounters an {@link #IMPORT_TAG}, it
     * will import the associated file by adding its content to the buffer. Note
     * that the method is recursive, so an imported file can itself define some
     * imports.
     * 
     * @param data The asset data of the file to read.
     * @param sb   The string builder to fill.
     * @return     The filled string builder with the file's content.
     */
    private StringBuilder read(AssetData data, StringBuilder sb) {
        try (final var bufferedReader = FileUtils.readBuffered(data.openStream())) {

            String line = null;

            while ((line = bufferedReader.readLine()) != null) {

                if (line.startsWith(IMPORT_TAG)) {

                    var importPath = line.trim().substring(IMPORT_TAG.length() + 1).trim();
                    // If the import begins with quotes, remove them before reading the file.
                    if (importPath.startsWith("\"") && importPath.endsWith("\"") && importPath.length() > 3) {
                        importPath = importPath.substring(1, importPath.length() - 1);
                    }
                    // It shouldn't need to import the main-file itself into it.
                    if (data.getName().equals(importPath)) {
                        throw new IOException(data.getName() + " cannot import itself!");
                    }

                    // Read the import file and inject its content in the string builder.
                    read(new PathAssetData(Paths.get(importPath)), sb);

                    logger.info("Successfully imported: " + importPath);
                } else {
                    sb.append(line).append('\n');
                }
            }

        } catch (IOException ex) {
            logger.error("Failed to read import: " + data.getName() + " Error: " + ex.getMessage());
            ex.printStackTrace();
        }

        return sb;
    }
}
