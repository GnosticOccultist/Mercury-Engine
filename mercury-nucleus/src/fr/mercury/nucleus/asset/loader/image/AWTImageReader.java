package fr.mercury.nucleus.asset.loader.image;

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
 * <code>AWTImageReader</code> is an implementation of {@link AssetLoader}
 * designed to read a texture file present on the disk using <code>AWT</code>
 * and {@link BufferedImage}. The loader returns an {@link Image} containing
 * pixel data and a format, which can later be turned into a {@link Texture} to
 * be used inside a rendering context.
 * 
 * @author GnosticOccultist
 */
public class AWTImageReader implements AssetLoader<Image> {

    /**
     * The logger of the application.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.assets");
    /**
     * The image AWT asset loader descriptor.
     */
    public static final AssetLoaderDescriptor<AWTImageReader> DESCRIPTOR = new AssetLoaderDescriptor<>(
            AWTImageReader::new, FileExtensions.TEXTURE_FILE_EXTENSION);

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
        var ext = asset.asset().getExtension();
        if (ImageIO.getImageReadersBySuffix(ext) == null) {
            throw new MercuryException("The image extension " + ext + " is not supported by AWT!");
        }

        ImageIO.setUseCache(false);
        Image image = null;

        try (var is = asset.openStream(); var bis = new BufferedInputStream(is)) {
            var img = ImageIO.read(bis);
            var width = img.getWidth();
            var height = img.getHeight();

            var channels = img.getTransparency() == Transparency.OPAQUE ? 3 : 4;
            var buffer = Allocator.alloc(width * height * channels);

            var imageDescriptor = asset.asset(ImageDescriptor.class);
            var flipMode = imageDescriptor.getConfig().flipMode();

            // Check if it's a known format.
            image = handleKnownFormat(asset, img, buffer, flipMode);
            // Prevent the user that the image has been successfully loaded.
            if (image != null) {
                logger.info("Successfully loaded image with AWT: " + asset.getName() + ", image= " + image);
                return image;
            }

            var format = channels == 4 ? Format.RGBA8 : Format.RGB8;

            // Otherwise pack the image data in RGBA or RGB order.

            if (img.getTransparency() == Transparency.OPAQUE) {
                for (var y = 0; y < height; ++y) {
                    for (var x = 0; x < width; ++x) {

                        var ny = y;
                        if (flipMode.equals(FlipMode.FLIP_Y)) {
                            ny = height - y - 1;
                        }

                        var rgba = img.getRGB(x, ny);

                        var r = (byte) ((rgba & 0x00FF0000) >> 16);
                        var g = (byte) ((rgba & 0x0000FF00) >> 8);
                        var b = (byte) ((rgba & 0x000000FF));

                        buffer.put(r).put(g).put(b);
                    }
                }
            } else {
                for (var y = 0; y < height; ++y) {
                    for (var x = 0; x < width; ++x) {

                        var ny = y;
                        if (flipMode.equals(FlipMode.FLIP_Y)) {
                            ny = height - y - 1;
                        }

                        var argb = img.getRGB(x, ny);

                        var a = (byte) ((argb & 0xFF000000) >> 24);
                        var r = (byte) ((argb & 0x00FF0000) >> 16);
                        var g = (byte) ((argb & 0x0000FF00) >> 8);
                        var b = (byte) ((argb & 0x000000FF));

                        buffer.put(r).put(g).put(b).put(a);
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
            logger.error("Failed to read image with AWT: " + asset.getName());
        }

        // Prevent the user that the image has been successfully loaded.
        if (image != null) {
            logger.info("Successfully loaded image with AWT: " + asset.getName() + ", image= " + image);
        }

        return image;
    }

    /**
     * Handle known data format such as 8-bits ABGR and 8-bits BGR, which are often
     * used in Windows systems.
     * 
     * @param asset    The asset data to load (not null).
     * @param img      The read buffered image (not null).
     * @param buffer   An empty byte buffer to contain the pixel data.
     * @param flipMode The flip mode to apply to the image (not null).
     * @return An image or null if it isn't a standard format.
     */
    private Image handleKnownFormat(LocatedAsset asset, BufferedImage img, ByteBuffer buffer, FlipMode flipMode) {
        assert flipMode != null;

        var width = img.getWidth();
        var height = img.getHeight();
        var type = img.getType();
        Image image = null;

        switch (type) {
        // Often used in PNG files.
        case BufferedImage.TYPE_4BYTE_ABGR:
            var array = (byte[]) getDataArray(asset, img, flipMode);
            assert buffer.capacity() == array.length;
            if (flipMode.equals(FlipMode.FLIP_Y)) {
                flipData(array, width, height, 32);
            }

            buffer.put(array);
            buffer.flip();
            image = new Image(width, height, Format.ABGR8, buffer);
            break;
        // Often used in JPEG files.
        case BufferedImage.TYPE_3BYTE_BGR:
            array = (byte[]) getDataArray(asset, img, flipMode);
            assert buffer.capacity() == array.length;
            if (flipMode.equals(FlipMode.FLIP_Y)) {
                flipData(array, width, height, 24);
            }

            buffer.put(array);
            buffer.flip();
            image = new Image(width, height, Format.BGR8, buffer);
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

    /**
     * Return an array containing the pixel data of the given {@link BufferedImage}.
     * 
     * @param asset    The asset data to load (not null).
     * @param img      The read buffered image (not null).
     * @param flipMode The flip mode to apply to the pixel data (not null).
     * @return An array containing the pixel data, either in byte or short values.
     */
    private Object getDataArray(LocatedAsset asset, BufferedImage img, FlipMode flipMode) {
        var buffer = img.getRaster().getDataBuffer();
        switch (buffer.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            var byteBuf = (DataBufferByte) buffer;
            return byteBuf.getData();
        case DataBuffer.TYPE_USHORT:
            var shortBuf = (DataBufferUShort) buffer;
            return shortBuf.getData();
        case DataBuffer.TYPE_UNDEFINED:
            throw new IllegalStateException("Undefined data buffer type for image: " + asset.getName());
        }
        throw new IllegalStateException("Unresolved data buffer type " + buffer + ", for image: " + asset.getName());
    }

    /**
     * Flips the provided pixel data along the Y-axis.
     * 
     * @param data   The array containing the pixel data (not null).
     * @param width  The width of the image in pixels (&gt;0).
     * @param height The height of the image in pixels (&gt;0).
     * @param bpp    The amount of bits per pixel (8, 24, 32).
     */
    private void flipData(byte[] data, int width, int height, int bpp) {
        var scSz = (width * bpp) / 8;
        var sln = new byte[scSz];
        var y2 = 0;
        for (var y1 = 0; y1 < height / 2; ++y1) {
            y2 = height - y1 - 1;
            System.arraycopy(data, y1 * scSz, sln, 0, scSz);
            System.arraycopy(data, y2 * scSz, data, y1 * scSz, scSz);
            System.arraycopy(sln, 0, data, y2 * scSz, scSz);
        }
    }

    @Override
    public void registerAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
}
