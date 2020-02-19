package fr.mercury.nucleus.asset;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.texture.Image;
import fr.mercury.nucleus.texture.Texture;
import fr.mercury.nucleus.utils.MercuryException;

/**
 * <code>ImageReader</code> is an implementation of {@link AssetLoader} designed
 * to load a texture file, present on the disk, into an {@link Image} which is turned
 * into a {@link Texture} to be used with the rendering context.
 * 
 * @author GnosticOccultist
 */
public class ImageReader implements AssetLoader<Image> {

	/**
	 * The logger of the application.
	 */
	private static final Logger logger = FactoryLogger.getLogger("mercury.app");
	
	@Override
	public Image load(String path) {
		
		Image image = null;
		
		// Creating the image from byte buffer.	
		ByteBuffer buffer = BufferUtils.createByteBuffer(16777216);
		image = decodeImage(buffer, path);
		
		// Prevent the user that the image has been successfully loaded.
		if(image != null) {
			logger.info("Successfully loaded image data with image file: " + path);
		}
		
		return image;
	}
	
	/**
	 * Decode the image file and write each pixel color to a {@link ByteBuffer},
	 * and finally create an {@link Image} instance from the loaded data. 
	 * 
	 * @param buffer The byte buffer to fill with pixel data.
	 * @param path	 The path to the image to load.
	 * @return		 The loaded image instance.
	 */
	private Image decodeImage(ByteBuffer buffer, String path) {
		
		try (MemoryStack stack = MemoryStack.stackPush()) {
			
			// Buffers to retrieve the width, height and component size
			// of the texture file.
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer components = stack.mallocInt(1);
			
			// Decode texture image into a byte buffer.
			ByteBuffer decodedImage = STBImage.stbi_load_from_memory(
					FileUtils.toByteBuffer(path, buffer, this::resize), w, h, components, 4);
			
			if(decodedImage == null) {
				throw new MercuryException("Failed to load image: " + STBImage.stbi_failure_reason());
			}
			
			// Create the image and fill its data with the decoded image. 
			Image image = new Image(w.get(), h.get(), decodedImage);
			// Rewind the buffer so freeing the data doesn't crash.
			decodedImage.rewind();
			
			// Free the decoded image buffer, since it isn't needed anymore.
			STBImage.stbi_image_free(decodedImage);
			
			return image;
		}
	}
	
	/**
	 * Resize the {@link ByteBuffer} to the provided size, by copying its content to
	 * a larger one, and 
	 * 
	 * @param buffer The buffer to resize.
	 * @param size   The new size of the buffer.
	 * @return		 The byte buffer with the updated size.
	 */
	private ByteBuffer resize(ByteBuffer buffer, Integer size) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(size);
		buffer.flip();
		newBuffer.put(buffer);
		MemoryUtil.memFree(buffer);
		return newBuffer;
	}
}
