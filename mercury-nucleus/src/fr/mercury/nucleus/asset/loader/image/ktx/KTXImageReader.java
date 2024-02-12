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
    private static final Logger logger = FactoryLogger.getLogger("mercury.assets.ktx");
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
     * The size in bytes of the file identifier for both KTX 1 and KTX 2.0.
     */
    private final static int IDENTIFIER_SIZE = KTX_2_FILE_IDENTIFIER.length;

    /**
     * Loads the texture file from the specified {@link LocatedAsset} into an
     * {@link Image}, to use in the application.
     * 
     * @param asset The located asset to load (not null).
     * @return The loaded image object.
     */
    @Override
    public Image load(LocatedAsset asset) {

        try (var reader = new StreamBinaryReader(asset.openStream())) {

            var chunk = reader.readChunk(ByteOrder.LITTLE_ENDIAN, IDENTIFIER_SIZE);
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

    /**
     * Loads the KTX 1.0 texture file from the specified {@link LocatedAsset} into
     * an {@link Image}, to use in the application.
     * 
     * @param asset  The located asset to load (not null).
     * @param reader The binary reader containing file data (not null).
     * @return The loaded image object.
     * @throws IOException Thrown if an error occured while reading the KTX file.
     */
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

        logger.debug("glType= " + glType + ", glTypeSize= " + glTypeSize + ", glFormat= " + glFormat
                + ", glInternalFormat= " + glInternalFormat + ", glBaseInternalFormat= " + glBaseInternalFormat);

        var pixelReader = readKeyValuePairs(reader, order, bytesOfKeyValueData);
        if (pixelReader == null) {
            logger.debug("Unresolved pixel reader, defaulting to SrTuRo orientation.");
            pixelReader = SrTuRo_PIXEL_READER;
        }

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

        var imgFormat = resolveGLImageFormat(glFormat, glInternalFormat, glType);
        logger.debug("Resolved image format: " + imgFormat);
        var colorSpace = resolveColorSpace(glInternalFormat);
        logger.debug("Resolved color space: " + colorSpace);

        var bytePerPixel = imgFormat.getBitsPerPixel() / 8;
        var mipMapSizes = new int[numberOfMipmapLevels];

        var byteBuffersSize = computeBuffersSize(numberOfMipmapLevels, pixelWidth, pixelHeight, bytePerPixel,
                pixelDepth);
        logger.debug("Total image data size: " + byteBuffersSize);

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

            logger.debug("Reading mipmap level " + mipLevel + " with size " + computedImageSize + " bytes.");
            if (imageSize != computedImageSize) {
                logger.warning("Size of mipmap level " + mipLevel + " is incorrect, size is " + imageSize
                        + " but should be " + computedImageSize);
            }

            var buffer = image.getData();
            buffer.position(offset);

            var readPixels = pixelReader.readPixels(width, height, pixelData, buffer, reader);
            assert readPixels == computedImageSize;
            logger.debug("Read " + readPixels + " for mipmap level " + mipLevel);

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
        for (var i = 0; i < bytesOfKeyValueData;) {
            // First, reading key values.
            var chunk = reader.readChunk(order, Integer.BYTES);
            var keyAndValueByteSize = chunk.readInt();
            // Parsing key values pair separated by a NUL byte.
            chunk = reader.readChunk(order, keyAndValueByteSize);
            var kv = chunk.readString(keyAndValueByteSize).split("\0");
            for (var j = 0; j < kv.length; j += 2) {
                var key = kv[j];
                var value = kv[j + 1];
                logger.debug("Reading key/value pair: " + key + "/" + value);

                if (key.equalsIgnoreCase("KTXorientation")) {
                    if (value.startsWith("S=r,T=d") || value.equalsIgnoreCase("rd") || value.equalsIgnoreCase("rdi")) {
                        pixelReader = SrTdRi_PIXEL_READER;
                        logger.debug("Resolved pixel reader with SrTdRi orientation.");
                    } else if (value.startsWith("S=r,T=u") || value.equalsIgnoreCase("ru")
                            || value.equalsIgnoreCase("ruo")) {
                        pixelReader = SrTuRo_PIXEL_READER;
                        logger.debug("Resolved pixel reader with SrTuRo orientation.");
                    }
                }
            }

            // Padding.
            var padding = 3 - ((keyAndValueByteSize + 3) % 4);
            if (padding > 0) {
                reader.skip(padding);
            }
            i += Integer.BYTES + keyAndValueByteSize + padding;
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

    private Image.Format resolveGLImageFormat(int glFormat, int glInternalFormat, int glType) {
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

    /**
     * Return the {@link ColorSpace} of the provided OpenGL or Vulkan format.
     * 
     * @param format The format to resolve the color space.
     * @return The color space of the format (not null).
     */
    private ColorSpace resolveColorSpace(int format) {
        switch (format) {
            // VK_FORMAT_R8G8B8_SRGB
        case 29:
            // VK_FORMAT_B8G8R8_SRGB
        case 36:
            // VK_FORMAT_R8G8B8A8_SRGB
        case 43:
            // VK_FORMAT_A8B8G8R8_SRGB_PACK32
        case 57:
        case GL21C.GL_SRGB:
        case GL21C.GL_SRGB8:
        case GL21C.GL_SRGB_ALPHA:
        case GL21C.GL_SRGB8_ALPHA8:
            return ColorSpace.sRGB;
        default:
            return ColorSpace.LINEAR;
        }
    }

    /**
     * Compute the {@link Image} buffer size to contain all mipmap level.
     * 
     * @param numberOfMipmapLevels The count of mipmap levels (&ge;1).
     * @param pixelWidth           The width in pixels of the first level image
     *                             (&gt;0).
     * @param pixelHeight          The height in pixels of the first level image
     *                             (&gt;0).
     * @param bytePerPixel         The count of bytes per pixel data (&ge;1).
     * @param pixelDepth           The depth in pixels of the first level image
     *                             (&ge;1).
     * @return The size in bytes to allocate for the image buffer (&ge;1).
     */
    private int computeBuffersSize(int numberOfMipmapLevels, int pixelWidth, int pixelHeight, int bytePerPixel,
            int pixelDepth) {
        var byteBuffersSize = 0;
        for (var mipLevel = 0; mipLevel < numberOfMipmapLevels; ++mipLevel) {
            var width = Math.max(1, pixelWidth >> mipLevel);
            var height = Math.max(1, pixelHeight >> mipLevel);
            var sizeInBytes = width * height * bytePerPixel;
            byteBuffersSize += sizeInBytes;

            logger.debug("Mipmap level " + mipLevel + " with size of " + sizeInBytes + " bytes.");
        }

        return byteBuffersSize * pixelDepth;
    }

    /**
     * Loads the KTX 2.0 texture file from the specified {@link LocatedAsset} into
     * an {@link Image}, to use in the application.
     * 
     * @param asset  The located asset to load (not null).
     * @param reader The binary reader containing file data (not null).
     * @return The loaded image object.
     * @throws IOException Thrown if an error occured while reading the KTX file.
     */
    public Image loadKTX2(LocatedAsset asset, BinaryReader reader) throws IOException {
        var sreader = reader.readAll().seekableReader();

        logger.debug("Reading KTX 2.0 image file with " + ByteOrder.nativeOrder() + " byte order.");

        var chunk = sreader.readChunk(9 * Integer.BYTES);

        // The texture format defined in VkFormat, for OpenGL we try to find
        // a corresponding format.
        var format = chunk.readInt();
        // The size in bytes of the data type.
        var typeSize = chunk.readInt();
        // Width in pixels of texture, not zero.
        var width = chunk.readInt();
        // Height in pixels of texture.
        var height = chunk.readInt();
        // Depth in pixels of texture.
        var depth = chunk.readInt();
        // The number of elements in array texture, or 0 for not an array texture.
        var layerCount = chunk.readInt();
        // The number of faces in cubemap, or 1 for not a cubemap texture.
        var faceCount = chunk.readInt();
        // The number of mimap levels, 1 for a non-mipmapped texture or 0 to generate
        // them.
        var levelCount = chunk.readInt();
        // The index of the supercompression scheme applied to the data, or 0 for none.
        var supercompressionScheme = chunk.readInt();

        if (width <= 0) {
            throw new IOException("The texture width must be greater than 0!");
        }

        if (faceCount <= 0) {
            throw new IOException("The texture must have at least one face defined!");
        }

        if (supercompressionScheme > 0) {
            throw new UnsupportedOperationException("Compressed image data isn't supported!");
        }

        if ((faceCount > 1 && depth > 1) || (faceCount > 1 && layerCount > 1) || (depth > 1 && layerCount > 1)) {
            throw new UnsupportedOperationException(
                    "Cubemap with 3D texture, cubemap with array texture and 3D array texture aren't supported!");
        }

        logger.debug("vkFormat= " + format + ", typeSize= " + typeSize);

        depth = Math.max(1, depth);
        assert faceCount > 0;
        // Slices within the texture is either the count of faces or elements in the
        // array texture.
        var nbSlices = Math.max(faceCount, layerCount);

        chunk = sreader.readChunk(4 * Integer.BYTES + 2 * Long.BYTES);

        var dfdOff = chunk.readInt();
        var dfdLen = chunk.readInt();

        var kvdOff = chunk.readInt();
        var kvdLen = chunk.readInt();

        var sgdOff = chunk.readLong();
        var sgdLen = chunk.readLong();

        logger.debug("[Data Format Descriptor] offset= " + dfdOff + ", length= " + dfdLen);
        logger.debug("[Key/Value pairs] offset= " + kvdOff + ", length= " + kvdLen);
        logger.debug("[Supercompression Global Data] offset= " + sgdOff + ", length= " + sgdLen);

        chunk = sreader.readChunk(3 * Long.BYTES * levelCount);

        var levelIndex = new long[3 * levelCount];
        for (var mipLevel = 0; mipLevel < levelIndex.length; mipLevel += 3) {
            // Mip level offset in bytes.
            levelIndex[mipLevel] = chunk.readLong();
            // Mip level supercompressed data total size in bytes.
            levelIndex[mipLevel + 1] = chunk.readLong();
            // Mip level uncompressed data total size in bytes.
            levelIndex[mipLevel + 2] = chunk.readLong();
        }

        // Data format descriptor.
        // Just skip the descriptor, since we're trying to resolve the format ourselves.
        chunk = sreader.readChunk(Integer.BYTES);
        var dfdTotalSize = chunk.readInt();
        assert dfdLen == dfdTotalSize;
        var skip = dfdTotalSize - Integer.BYTES;
        sreader.skip(skip);

        // Read key/value data.
        var pixelReader = readKeyValuePairs(sreader, ByteOrder.nativeOrder(), kvdLen);
        if (pixelReader == null) {
            logger.debug("Unresolved pixel reader, defaulting to SrTuRo orientation.");
            pixelReader = SrTuRo_PIXEL_READER;
        }

        // If VK_FORMAT_UNDEFINED, use the Data Format Descriptor to resolve the format.
        if (format == 0x0) {
            throw new UnsupportedOperationException(
                    "The format is undefined, but reading Data Format Descriptor isn't supported!");
        }

        var imgFormat = resolveVkFormat(format);
        logger.debug("Resolved image format: " + imgFormat);
        var colorSpace = resolveColorSpace(format);
        logger.debug("Resolved color space: " + colorSpace);

        var bytePerPixel = imgFormat.getBitsPerPixel() / 8;
        var mipMapSizes = new int[levelCount];

        var byteBuffersSize = computeBuffersSize(levelCount, width, height, bytePerPixel, depth);
        logger.info("Total image data size: " + byteBuffersSize);

        var image = createImage(nbSlices, byteBuffersSize, imgFormat, colorSpace, width, height, depth);

        // TODO: Handle Supercompression Global Data.

        // Alignment isn't used since we know the offset of each mipmap level.
        // var alignment = Math.max(0, (MercuryMath.lcm(bytePerPixel, 4)));

        var offset = 0;
        var pixelData = new byte[bytePerPixel];

        for (var mipLevel = 0; mipLevel < levelCount; ++mipLevel) {

            // The size of the mip level in bytes.
            var pos = levelIndex[mipLevel * 3];

            // The size of the mip level in bytes.
            var imageSize = (int) levelIndex[mipLevel * 3 + 1];

            // Calculate the image size based on the format read.
            var w = Math.max(1, width >> mipLevel);
            var h = Math.max(1, height >> mipLevel);

            var computedImageSize = w * h * bytePerPixel;
            mipMapSizes[mipLevel] = imageSize;

            // Position reader at offset minus file identifier size.
            sreader.seek(pos - IDENTIFIER_SIZE);

            logger.debug("Reading mipmap level " + mipLevel + " with size " + computedImageSize + " bytes.");
            if (imageSize != computedImageSize) {
                logger.warning("Size of mipmap level " + mipLevel + " is incorrect, size is " + imageSize
                        + " but should be " + computedImageSize);
            }

            var buffer = image.getData();
            buffer.position(offset);

            var readPixels = pixelReader.readPixels(w, h, pixelData, buffer, sreader);
            assert readPixels == imageSize;
            logger.debug("Read " + readPixels + " for mipmap level " + mipLevel);

            offset += imageSize;
        }

        if (levelCount > 1) {
            image.setMipmapSizes(mipMapSizes);
        }

        return image;
    }

    /**
     * Return a corresponding {@link Format} for the provided Vulkan format.
     * 
     * @param vkFormat The vulkan format to resolve (&ge;0).
     * @return A corresponding format for the image (not null).
     * @throws IOException Thrown if the Vulkan format isn't resolved.
     */
    private Format resolveVkFormat(int vkFormat) throws IOException {
        switch (vkFormat) {
        case 23:
        case 27:
        case 29:
            // VK_FORMAT_R8G8B8_UNORM
            // VK_FORMAT_R8G8B8_UINT
            // VK_FORMAT_R8G8B8_SRGB
            return Format.RGB8;
        case 34:
        case 36:
            // VK_FORMAT_B8G8R8_UINT
            // VK_FORMAT_B8G8R8_SRGB
            return Format.BGR8;
        case 41:
        case 43:
            // VK_FORMAT_R8G8B8A8_UINT
            // VK_FORMAT_R8G8B8A8_SRGB
            return Format.RGBA8;
        case 55:
        case 57:
            // VK_FORMAT_A8B8G8R8_UINT_PACK32
            // VK_FORMAT_A8B8G8R8_SRGB_PACK32
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
            throw new IOException("Unhandled KTX texture format, value= " + vkFormat);
        }
    }

    /**
     * <code>PixelReader</code> is an interface to define a strategy for reading
     * pixels inside a KTX file. In OpenGL the origin is conventionally described as
     * being at the lower left, but this convention is not shared by all image file
     * formats, so KTX file specifies which orientation to follow for reading
     * pixels.
     * <p>
     * Common orientations are as follows:
     * <li>S=r,T=d
     * <li>S=r,T=u
     * <li>S=r,T=d,
     * <li>R=i S=r,
     * <li>T=u,R=o</li> where
     * <li>S indicates the direction of increasing S values
     * <li>T indicates the direction of increasing T values
     * <li>R indicates the direction of increasing R values
     * <li>r indicates increasing to the right
     * <li>l indicates increasing to the left
     * <li>d indicates increasing downwards
     * <li>u indicates increasing upwards
     * <li>o indicates increasing out from the screen (moving towards viewer)
     * <li>i indicates increasing in towards the screen (moving away from viewer)
     * 
     * @author GnosticOccultist
     * 
     * @see SrTdRiPixelReader
     * @see SrTuRoPixelReader
     */
    @FunctionalInterface
    public interface PixelReader {

        /**
         * Reads the pixel from the provided {@link BinaryReader} and populate the given
         * {@link ByteBuffer} with the pixel data.
         * 
         * @param pixelWidth  The width in pixels of the first level image (&gt;0).
         * @param pixelHeight The height in pixels of the first level image (&gt;0).
         * @param pixelData   An array to temporary read bytes to (not null).
         * @param buffer      The buffer to store the pixel data in (not null).
         * @param reader      The binary reader to read pixel data from (not null).
         * @return The count of bytes read.
         * @throws IOException Thrown if an error occured while reading the pixel data.
         */
        int readPixels(int pixelWidth, int pixelHeight, byte[] pixelData, ByteBuffer buffer, BinaryReader reader)
                throws IOException;
    }

    /**
     * The pixel reader for reading from a top-left corner origin.
     */
    public static final PixelReader SrTdRi_PIXEL_READER = new PixelReader() {

        @Override
        public int readPixels(int pixelWidth, int pixelHeight, byte[] pixelData, ByteBuffer buffer, BinaryReader reader)
                throws IOException {
            var pixelRead = 0;
            for (var row = pixelHeight - 1; row >= 0; --row) {
                for (var pixel = 0; pixel < pixelWidth; ++pixel) {
                    reader.read(pixelData);
                    for (var i = 0; i < pixelData.length; ++i) {
                        var index = buffer.position() + (row * pixelWidth + pixel) * pixelData.length + i;
                        buffer.put(index, pixelData[i]);
                    }
                    pixelRead += pixelData.length;
                }
            }
            return pixelRead;
        }
    };

    /**
     * The pixel reader for reading from a bottom-left corner origin (OpenGL
     * standard).
     */
    public static final PixelReader SrTuRo_PIXEL_READER = new PixelReader() {

        @Override
        public int readPixels(int pixelWidth, int pixelHeight, byte[] pixelData, ByteBuffer buffer, BinaryReader reader)
                throws IOException {
            var pixelRead = 0;
            for (var row = 0; row < pixelHeight; ++row) {
                for (var pixel = 0; pixel < pixelWidth; ++pixel) {
                    reader.read(pixelData);
                    buffer.put(pixelData);
                    pixelRead += pixelData.length;
                }
            }
            return pixelRead;
        }
    };
}