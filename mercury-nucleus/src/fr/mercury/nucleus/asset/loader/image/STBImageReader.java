package fr.mercury.nucleus.asset.loader.image;

import java.nio.ByteBuffer;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.asset.loader.AssetLoader;
import fr.mercury.nucleus.asset.loader.AssetLoaderDescriptor;
import fr.mercury.nucleus.asset.loader.image.ImageAssetConfig.FlipMode;
import fr.mercury.nucleus.asset.locator.AssetLocator.LocatedAsset;
import fr.mercury.nucleus.texture.ColorSpace;
import fr.mercury.nucleus.texture.Image;
import fr.mercury.nucleus.texture.Image.Format;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.data.Allocator;

/**
 * <code>STBImageReader</code> is an implementation of {@link AssetLoader}
 * designed to read a texture file present on the disk using the
 * <code>STB</code> library. The loader returns an {@link Image} containing
 * pixel data and a format, which can later be turned into a {@link Texture} to
 * be used inside a rendering context.
 * 
 * @author GnosticOccultist
 */
public class STBImageReader implements AssetLoader<Image> {

    /**
     * The logger of the application.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.assets");
    /**
     * The image STB asset loader descriptor.
     */
    public static final AssetLoaderDescriptor<STBImageReader> DESCRIPTOR = new AssetLoaderDescriptor<>(
            STBImageReader::new, FileExtensions.TEXTURE_FILE_EXTENSION);

    /**
     * The asset manager.
     */
    private AssetManager assetManager;

    /**
     * Loads the texture file from the specified {@link AssetData} into an
     * {@link Image}, to use in the application.
     * 
     * @param asset The asset data to load (not null).
     * @return The loaded image object.
     */
    @Override
    public Image load(LocatedAsset asset) {

        Image image = null;

        // Creating the image from byte buffer.
        var buffer = Allocator.alloc(16777216);
        image = decodeImage(buffer, asset);

        // Prevent the user that the image has been successfully loaded.
        if (image != null) {
            logger.info("Successfully loaded image with STB: " + asset.getName() + ", image= " + image);
        }

        return image;
    }

    /**
     * Decode the image file and write each pixel color to a {@link ByteBuffer}, and
     * finally create an {@link Image} instance from the loaded data.
     * 
     * @param buffer The byte buffer to fill with pixel data.
     * @param asset  The asset data of the image to load.
     * @return The loaded image instance.
     */
    private Image decodeImage(ByteBuffer buffer, LocatedAsset asset) {

        try (var stack = MemoryStack.stackPush()) {

            // Buffers to retrieve the width, height and component size
            // of the texture file.
            var w = stack.mallocInt(1);
            var h = stack.mallocInt(1);
            var channels = stack.mallocInt(1);

            var imageDescriptor = asset.asset(ImageDescriptor.class);
            var flipMode = imageDescriptor.getConfig().flipMode();
            STBImage.stbi_set_flip_vertically_on_load(flipMode == FlipMode.FLIP_Y);

            // Decode texture image into a byte buffer, by not enforcing pixel channels.
            ByteBuffer decodedImage = STBImage.stbi_load_from_memory(
                    FileUtils.toByteBuffer(asset.openStream(), buffer, this::resize), w, h, channels, 0);

            if (decodedImage == null) {
                throw new MercuryException("Failed to load image: " + STBImage.stbi_failure_reason());
            }

            // Figure out which image format is appropriate.
            Format format = null;
            var chan = channels.get();
            if (chan == 4) {
                format = Format.RGBA8;
            } else if (chan == 3) {
                format = Format.RGB8;
            } else {
                logger.warning("Invalid color channels in file " + asset.getName() + ", channels= " + channels);
                format = Format.RGBA8;
            }

            var settings = assetManager.getApplication().getSettings();
            // Enforce sRGB color space if gamma correction is enabled.
            var colorSpace = settings.isGammaCorrection() ? ColorSpace.sRGB : ColorSpace.LINEAR;

            // Create the image and fill its data with the decoded image.
            var image = new Image(w.get(), h.get(), format, decodedImage);
            image.setColorSpace(colorSpace);
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
     * @return The byte buffer with the updated size.
     */
    private ByteBuffer resize(ByteBuffer buffer, Integer size) {
        var newBuffer = Allocator.alloc(size);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    @Override
    public void registerAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
}