package fr.mercury.nucleus.asset.loader;

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.asset.loader.data.AssetData;
import fr.mercury.nucleus.texture.ColorSpace;
import fr.mercury.nucleus.texture.Image;
import fr.mercury.nucleus.texture.Image.Format;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.data.Allocator;

public class AWTImageReader implements AssetLoader<Image, VoidLoaderConfig> {

    /**
     * The logger of the application.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.app");
    /**
     * The image STB asset loader descriptor.
     */
    public static final AssetLoaderDescriptor<AWTImageReader> DESCRIPTOR = new AssetLoaderDescriptor<>(
            AWTImageReader::new, FileExtensions.TEXTURE_FILE_EXTENSION);

    /**
     * The asset manager.
     */
    private AssetManager assetManager;

    @Override
    public Image load(AssetData data) {
        return load(data, VoidLoaderConfig.get());
    }

    @Override
    public Image load(AssetData data, VoidLoaderConfig config) {
        var ext = data.getExtension();
        if (ImageIO.getImageReadersBySuffix(ext) == null) {
            throw new MercuryException("The image extension " + ext + " is not supported by AWT!");
        }

        ImageIO.setUseCache(false);
        Image image = null;

        try (var is = data.openStream(); var bis = new BufferedInputStream(is)) {
            var img = ImageIO.read(bis);
            var width = img.getWidth();
            var height = img.getHeight();

            var channels = img.getTransparency() == Transparency.OPAQUE ? 3 : 4;
            var buffer = Allocator.alloc(width * height * channels);

            // Check if it's a known format.
            image = handleKnownFormat(data, img, buffer);
            // Prevent the user that the image has been successfully loaded.
            if (image != null) {
                logger.info("Successfully loaded image with AWT: " + data.getName() + ", image= " + image);
                return image;
            }

            var format = channels == 4 ? Format.RGBA8 : Format.RGB8;

            // Otherwise pack the image data in RGBA or RGB order.
            for (var y = 0; y < height; ++y) {
                for (var x = 0; x < width; ++x) {

                    var rgba = img.getRGB(x, y);

                    var r = (byte) ((rgba & 0x00FF0000) >> 16);
                    var g = (byte) ((rgba & 0x0000FF00) >> 8);
                    var b = (byte) ((rgba & 0x000000FF));

                    buffer.put(r).put(g).put(b);

                    if (channels == 4) {
                        var a = (byte) ((rgba & 0xFF000000) >> 24);
                        buffer.put(a);
                    }
                }
            }
            buffer.flip();

            // Create the image and fill its data with the decoded image.
            image = new Image(width, height, format, buffer);

            var settings = assetManager.getApplication().getSettings();
            // Enforce sRGB color space if gamma correction is enabled.
            var colorSpace = settings.isGammaCorrection() ? ColorSpace.sRGB : ColorSpace.LINEAR;
            image.setColorSpace(colorSpace);

        } catch (IOException ex) {
            logger.error("Failed to read image with AWT: " + data.getName());
        }

        // Prevent the user that the image has been successfully loaded.
        if (image != null) {
            logger.info("Successfully loaded image with AWT: " + data.getName() + ", image= " + image);
        }

        return image;
    }

    private Image handleKnownFormat(AssetData data, BufferedImage img, ByteBuffer buffer) {
        var type = img.getType();
        Image image = null;

        switch (type) {
            // Often used in PNG files.
            case BufferedImage.TYPE_4BYTE_ABGR:
                var array = (byte[]) getDataArray(data, img);
                assert buffer.capacity() == array.length;
                buffer.put(array);
                buffer.flip();
                image = new Image(img.getWidth(), img.getHeight(), Format.ABGR8, buffer);
                break;
            // Often used in JPEG files.
            case BufferedImage.TYPE_3BYTE_BGR:
                array = (byte[]) getDataArray(data, img);
                assert buffer.capacity() == array.length;
                buffer.put(array);
                buffer.flip();
                image = new Image(img.getWidth(), img.getHeight(), Format.BGR8, buffer);
                break;
        }

        if (image != null) {
            var settings = assetManager.getApplication().getSettings();
            // Enforce sRGB color space if gamma correction is enabled.
            var colorSpace = settings.isGammaCorrection() ? ColorSpace.sRGB : ColorSpace.LINEAR;
            image.setColorSpace(colorSpace);
        }

        return image;
    }

    private Object getDataArray(AssetData data, BufferedImage img) {
        var buffer = img.getRaster().getDataBuffer();
        switch (buffer.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                var byteBuf = (DataBufferByte) buffer;
                return byteBuf.getData();
            case DataBuffer.TYPE_USHORT:
                var shortBuf = (DataBufferUShort) buffer;
                return shortBuf.getData();
            case DataBuffer.TYPE_UNDEFINED:
                throw new IllegalStateException("Undefined data buffer type for image: " + data.getName());
        }
        throw new IllegalStateException("Unresolved data buffer type " + buffer + ", for image: " + data.getName());
    }

    @Override
    public void registerAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
}
