package fr.mercury.nucleus.asset.loader.image.ktx;

import java.io.IOException;
import java.nio.ByteBuffer;

import fr.alchemy.utilities.file.io.binary.BinaryReader;
import fr.mercury.nucleus.asset.loader.image.ktx.KTXImageReader.PixelReader;

public class SrTdRiPixelReader implements PixelReader {

    @Override
    public int readPixels(int pixelWidth, int pixelHeight, byte[] pixelData, ByteBuffer buffer, BinaryReader reader)
            throws IOException {
        var pixelRead = 0;
        for (var row = pixelHeight - 1; row >= 0; --row) {
            for (var pixel = 0; pixel < pixelWidth; ++pixel) {
                reader.read(pixelData);
                for (var i = 0; i < pixelData.length; ++i) {
                    buffer.put((row * pixelWidth + pixel) * pixelData.length + i, pixelData[i]);
                }
                pixelRead += pixelData.length;
            }
        }
        return pixelRead;
    }

}
