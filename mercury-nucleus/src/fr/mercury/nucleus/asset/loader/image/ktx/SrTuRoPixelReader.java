package fr.mercury.nucleus.asset.loader.image.ktx;

import java.io.IOException;
import java.nio.ByteBuffer;
import fr.alchemy.utilities.file.io.binary.BinaryReader;
import fr.mercury.nucleus.asset.loader.image.ktx.KTXImageReader.PixelReader;

public class SrTuRoPixelReader implements PixelReader {

    @Override
    public int readPixels(int pixelWidth, int pixelHeight, byte[] pixelData, ByteBuffer buffer, BinaryReader reader)
            throws IOException {
        int pixelRead = 0;
        for (var row = 0; row < pixelHeight; ++row) {
            for (var pixel = 0; pixel < pixelWidth; ++pixel) {
                reader.read(pixelData);
                buffer.put(pixelData);
                pixelRead += pixelData.length;
            }
        }
        return pixelRead;
    }

}
