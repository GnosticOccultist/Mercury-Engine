package fr.mercury.nucleus.asset.loader.image.ktx;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL21C;
import fr.alchemy.utilities.file.io.binary.BinaryReader;
import fr.alchemy.utilities.file.io.binary.StreamBinaryReader;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.asset.loader.AssetLoader;
import fr.mercury.nucleus.asset.loader.AssetLoaderDescriptor;
import fr.mercury.nucleus.asset.locator.AssetLocator.LocatedAsset;
import fr.mercury.nucleus.texture.ColorSpace;
import fr.mercury.nucleus.texture.Image;
import fr.mercury.nucleus.texture.Image.Format;
import fr.mercury.nucleus.utils.data.BufferUtils;

public class KTXImageReader implements AssetLoader<Image> {

    /**
     * The logger of the application.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.assets");
    /**
     * The KTX texture file extension.
     */
    public static final String KTX_TEXTURE_EXTENSION = "ktx";
    /**
     * The KTX 2.0 texture file extension.
     */
    public static final String KTX_2_TEXTURE_EXTENSION = "ktx2";
    /**
     * The KTX texture asset loader descriptor.
     */
    public static final AssetLoaderDescriptor<KTXImageReader> DESCRIPTOR = new AssetLoaderDescriptor<>(
            KTXImageReader::new, KTX_TEXTURE_EXTENSION, KTX_2_TEXTURE_EXTENSION);

    /**
     * The KTX file identifier.
     */
    private final static byte[] KTX_FILE_IDENTIFIER = { (byte) 0xAB, (byte) 0x4B, (byte) 0x54, (byte) 0x58, (byte) 0x20,
            (byte) 0x31, (byte) 0x31, (byte) 0xBB, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A };
    /**
     * The KTX 2.0 file identifier.
     */
    private final static byte[] KTX_2_FILE_IDENTIFIER = { (byte) 0xAB, (byte) 0x4B, (byte) 0x54, (byte) 0x58,
            (byte) 0x20, (byte) 0x32, (byte) 0x30, (byte) 0xBB, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A };
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

        try (var reader = new StreamBinaryReader(asset.openStream())) {

            var chunk = reader.readChunk(ByteOrder.LITTLE_ENDIAN, 12);
            var identifier = chunk.readString(12);

            if (Arrays.equals(identifier.getBytes(), KTX_FILE_IDENTIFIER)) {

                return loadKTX(asset, reader);

            } else if (Arrays.equals(identifier.getBytes(), KTX_2_FILE_IDENTIFIER)) {

                return loadKTX2(asset, reader);

            } else {
                throw new IOException("Unrecognized KTX file identifier: " + identifier + ", expecting "
                        + new String(KTX_FILE_IDENTIFIER) + " or " + new String(KTX_2_FILE_IDENTIFIER));
            }
        } catch (IOException ex) {
            logger.error("Failed to read KTX file!", ex);
        }

        return null;
    }

    public Image loadKTX(LocatedAsset asset, BinaryReader reader) throws IOException {

        var chunk = reader.readChunk(4);
        var endianness = chunk.readInt();

        // Opposite endianness.
        var order = ByteOrder.nativeOrder();
        if (endianness == 0x01020304) {
            order = ByteOrder.LITTLE_ENDIAN;
        }

        logger.debug("Reading KTX image file with " + order + " byte order.");

        chunk = reader.readChunk(order, 12 * Integer.BYTES);
        // 0 for compressed textures, or component type.
        var glType = chunk.readInt();
        // Size in bytes of glType.
        var glTypeSize = chunk.readInt();
        // 0 for compressed textures, or texture format.
        var glFormat = chunk.readInt();
        // 0 for compressed textures, or texture format.
        var glInternalFormat = chunk.readInt();
        // Base internal texture format, RGB, RGBA, ALPHA.
        var glBaseInternalFormat = chunk.readInt();
        // Width in pixels of texture, not zero.
        var pixelWidth = chunk.readInt();
        // Height in pixels of texture, 0 for 1D texture.
        var pixelHeight = chunk.readInt();
        // Depth in pixels of texture, 0 for 2D texture.
        var pixelDepth = chunk.readInt();
        // The number of elements in array texture, or 0 for not an array texture.
        var numberOfArrayElements = chunk.readInt();
        // The number of faces in cubemap, or 1 for not a cubemap texture.
        var numberOfFaces = chunk.readInt();
        // The number of mimap levels, or 1 for a non-mipmapped texture (0 to generate).
        var numberOfMipmapLevels = chunk.readInt();
        // The number of bytes of key/value data following the header.
        var bytesOfKeyValueData = chunk.readInt();

        if ((numberOfFaces > 1 && pixelDepth > 1) || (numberOfFaces > 1 && numberOfArrayElements > 1)
                || (pixelDepth > 1 && numberOfArrayElements > 1)) {
            throw new UnsupportedOperationException(
                    "Cubemap with 3D texture, cubemap with array texture and 3D array texture aren't supported!");
        }

        logger.info("glType= " + glType + ", glTypeSize= " + glTypeSize + ", glFormat= " + glFormat
                + ", glInternalFormat= " + glInternalFormat + ", glBaseInternalFormat= " + glBaseInternalFormat);

        var pixelReader = readKeyValuePairs(reader, order, bytesOfKeyValueData);
        if (pixelReader == null) {
            pixelReader = new SrTuRoPixelReader();
        }

        logger.info("Using pixel reader implementation " + pixelReader.getClass().getName());

        // Make sure mipmap levels, pixel depth and array elements is at least 1 to be
        // able to iterate
        // through the loop.
        numberOfMipmapLevels = Math.max(1, numberOfMipmapLevels);
        pixelDepth = Math.max(1, pixelDepth);
        numberOfArrayElements = Math.max(1, numberOfArrayElements);
        assert numberOfFaces > 0;
        // Slices within the texture is either the count of faces or elements in the
        // array texture.
        var nbSlices = Math.max(numberOfFaces, numberOfArrayElements);

        var imgFormat = resolveImageFormat(glFormat, glInternalFormat, glType);
        logger.info("Resolved image format: " + imgFormat);
        var colorSpace = resolveColorSpace(glInternalFormat);
        logger.info("Resolved color space: " + colorSpace);

        var bytePerPixel = imgFormat.getBitsPerPixel() / 8;
        var mipMapSizes = new int[numberOfMipmapLevels];

        var byteBuffersSize = computeBuffersSize(numberOfMipmapLevels, pixelWidth, pixelHeight, bytePerPixel,
                pixelDepth);
        logger.info("Total image data size: " + byteBuffersSize);

        var image = createImage(nbSlices, byteBuffersSize, imgFormat, colorSpace, pixelWidth, pixelHeight, pixelDepth);

        var pixelData = new byte[bytePerPixel];
        var offset = 0;
        // Iterate over the mip level.
        for (var mipLevel = 0; mipLevel < numberOfMipmapLevels; ++mipLevel) {
            chunk = reader.readChunk(order, Integer.BYTES);

            // The size of the mip level in bytes.
            var imageSize = chunk.readInt();

            // Calculate the image size based on the format read.
            var width = Math.max(1, pixelWidth >> mipLevel);
            var height = Math.max(1, pixelHeight >> mipLevel);

            var computedImageSize = width * height * bytePerPixel;
            mipMapSizes[mipLevel] = computedImageSize;

            logger.info("Reading mipmap level " + mipLevel + " with size " + computedImageSize + " bytes.");
            if (imageSize != computedImageSize) {
                logger.warning("Size of mipmap level " + mipLevel + " is incorrect, size is " + imageSize
                        + " but should be " + computedImageSize);
            }

            var buffer = image.getData();
            buffer.position(offset);

            var readPixels = pixelReader.readPixels(width, height, pixelData, buffer, reader);
            logger.info("Read " + readPixels + " for mipmap level " + mipLevel);

            // Mipmap padding.
            reader.skip(3 - ((computedImageSize + 3) % 4));
            offset += computedImageSize;
        }

        if (numberOfMipmapLevels > 1) {
            image.setMipmapSizes(mipMapSizes);
        }

        return image;
    }

    private PixelReader readKeyValuePairs(BinaryReader reader, ByteOrder order, int bytesOfKeyValueData)
            throws IOException {
        PixelReader pixelReader = null;
        for (var i = 0; i < bytesOfKeyValueData; ++i) {
            // First, reading key values.
            var chunk = reader.readChunk(order, Integer.BYTES);
            var keyAndValueByteSize = chunk.readInt();
            // Parsing key values pair separated by a NUL byte.
            chunk = reader.readChunk(order, keyAndValueByteSize);
            var kv = chunk.readString(keyAndValueByteSize).split("\0");
            for (var j = 0; j < kv.length; j += 2) {
                var key = kv[j];
                var value = kv[j + 1];
                logger.info("Reading key/value pair: " + key + "/" + value);

                if (key.equalsIgnoreCase("KTXorientation")) {
                    if (value.startsWith("S=r,T=d")) {
                        pixelReader = new SrTdRiPixelReader();
                    } else {
                        pixelReader = new SrTuRoPixelReader();
                    }
                }
            }

            // Padding.
            var padding = 3 - ((keyAndValueByteSize + 3) % 4);
            if (padding > 0) {
                reader.skip(padding);
            }
            i += 4 + keyAndValueByteSize + padding;
        }

        return pixelReader;
    }

    private Image createImage(int nbSlices, int byteBuffersSize, Format imgFormat, ColorSpace colorSpace,
            int pixelWidth, int pixelHeight, int pixelDepth) {
        if (nbSlices > 1) {
            throw new UnsupportedOperationException("Multi-slices (cubemap or texture array) image isn't supported!");
        }

        // TODO: Should we enforce sRGB space.
        var buffer = BufferUtils.createByteBuffer(byteBuffersSize);
        var img = new Image(pixelWidth, pixelHeight, imgFormat, buffer);
        img.setColorSpace(colorSpace);
        return img;
    }

    private Image.Format resolveImageFormat(int glFormat, int glInternalFormat, int glType) {
        if (glFormat == GL11C.GL_RGB && glType == GL11C.GL_UNSIGNED_BYTE) {
            if (glFormat == glInternalFormat || glInternalFormat == GL11C.GL_RGB8) {
                return Image.Format.RGB8;
            }
            if (glFormat == glInternalFormat || glInternalFormat == GL21C.GL_SRGB8) {
                return Image.Format.RGB8;
            }
        } else if (glFormat == GL12C.GL_BGR && glType == GL11C.GL_UNSIGNED_BYTE) {
            if (glFormat == glInternalFormat || glInternalFormat == GL11C.GL_RGB8) {
                return Image.Format.BGR8;
            }
        } else if (glFormat == GL11C.GL_RGBA && glType == GL11C.GL_UNSIGNED_BYTE) {
            if (glFormat == glInternalFormat || glInternalFormat == GL11C.GL_RGBA8) {
                return Image.Format.RGBA8;
            }
            if (glFormat == glInternalFormat || glInternalFormat == GL21C.GL_SRGB8_ALPHA8) {
                return Image.Format.RGBA8;
            }
        } else if (glFormat == GL12C.GL_BGRA && glType == GL11C.GL_UNSIGNED_BYTE) {
            if (glFormat == glInternalFormat || glInternalFormat == GL11C.GL_RGBA8) {
                return Image.Format.BGRA8;
            }
        } else if (glFormat == GL12C.GL_BGRA && glType == GL12C.GL_UNSIGNED_INT_8_8_8_8) {
            if (glFormat == glInternalFormat || glInternalFormat == GL11C.GL_RGBA8) {
                return Image.Format.ABGR8;
            }
        } else {
            throw new UnsupportedOperationException("Unhandled glFormat in KTX texture, value=" + glFormat);
        }

        throw new UnsupportedOperationException(
                "Unhandled glInternalFormat in KTX texture, value=" + glInternalFormat + "  " + glFormat);
    }

    private ColorSpace resolveColorSpace(int glInternalFormat) {
        switch (glInternalFormat) {
        case GL21C.GL_SRGB:
        case GL21C.GL_SRGB8:
        case GL21C.GL_SRGB_ALPHA:
        case GL21C.GL_SRGB8_ALPHA8:
            return ColorSpace.sRGB;
        default:
            return ColorSpace.LINEAR;
        }
    }

    private int computeBuffersSize(int numberOfMipmapLevels, int pixelWidth, int pixelHeight, int bytePerPixel,
            int pixelDepth) {
        var byteBuffersSize = 0;
        for (var mipLevel = 0; mipLevel < numberOfMipmapLevels; ++mipLevel) {
            var width = Math.max(1, pixelWidth >> mipLevel);
            var height = Math.max(1, pixelHeight >> mipLevel);
            byteBuffersSize += width * height * bytePerPixel;
            logger.info("mip level size " + mipLevel + ": " + width * height * bytePerPixel);
        }

        return byteBuffersSize * pixelDepth;
    }

    public Image loadKTX2(LocatedAsset asset, BinaryReader reader) throws IOException {
        // The texture format defined in VkFormat.
//        var format = in.readInt();
//        var typeSize = in.readInt();
//        var width = in.readInt();
//        var height = in.readInt();
//        var depth = in.readInt();
//        var layerCount = in.readInt();
//        var faceCount = in.readInt();
//        var levelCount = in.readInt();
//        var supercompressionScheme = in.readInt();
//
//        if (width <= 0) {
//            throw new IOException("The texture width must be greater than 0!");
//        }
//
//        if (faceCount <= 0) {
//            throw new IOException("The texture must have at least one face defined!");
//        }
//
//        if (format == 0x00 && supercompressionScheme == 0) {
//            throw new IOException("Unspecified format for non-compressed texture!");
//        }
//
//        var frmt = Format.RGB8;
//        if (format != 0x00) {
//            frmt = getFormat(format);
//        }
//
//        var dfdOff = in.readInt();
//        var dfdLen = in.readInt();
//
//        var kvdOff = in.readInt();
//        var kvdLen = in.readInt();
//
//        var sgdOff = in.readLong();
//        var sgdLen = in.readLong();
//
//        logger.info(frmt.toString());
//
//        logger.info(dfdOff + ", " + dfdLen + " | " + kvdOff + ", " + kvdLen + " | " + sgdOff + ", " + sgdLen);
        return null;
    }

    private Format getFormat(int format) throws IOException {
        switch (format) {
        case 27:
            // VK_FORMAT_R8G8B8_UINT
            return Format.RGB8;
        case 34:
            // VK_FORMAT_B8G8R8_UINT
            return Format.BGR8;
        case 41:
            // VK_FORMAT_R8G8B8A8_UINT
            return Format.RGBA8;
        case 55:
            // VK_FORMAT_A8B8G8R8_UINT_PACK32
            return Format.ABGR8;
        case 90:
            // VK_FORMAT_R16G16B16_SFLOAT
            return Format.RGB16F;
        case 97:
            // VK_FORMAT_R16G16B16A16_SFLOAT
            return Format.RGBA16F;
        case 106:
            // VK_FORMAT_R32G32B32_SFLOAT
            return Format.RGB32F;
        case 109:
            // VK_FORMAT_R32G32B32A32_SFLOAT
            return Format.RGBA32F;
        default:
            throw new IOException("Unhandled KTX texture format, value= " + format);
        }
    }

    @Override
    public void registerAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @FunctionalInterface
    public interface PixelReader {

        int readPixels(int pixelWidth, int pixelHeight, byte[] pixelData, ByteBuffer buffer, BinaryReader reader)
                throws IOException;
    }
}