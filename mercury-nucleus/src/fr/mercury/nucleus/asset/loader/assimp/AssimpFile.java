package fr.mercury.nucleus.asset.loader.assimp;

import java.nio.ByteBuffer;

import org.lwjgl.assimp.AIFile;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.system.MemoryUtil;

import fr.mercury.nucleus.asset.locator.AssetLocator.LocatedAsset;

class AssimpFile {

    private AIFile aiFile;

    private final ByteBuffer content;

    private final byte[] contentArray;

    AssimpFile(AssimpFileSystem fileSystem, LocatedAsset located, byte[] contentArray) {
        if (contentArray == null) {
            contentArray = located.getBytes();
        }

        this.contentArray = contentArray;
        this.content = ByteBuffer.wrap(contentArray);

        this.aiFile = AIFile.calloc();

        aiFile.ReadProc((long fileHandle, long destAddress, long bytesPerRecord, long recordCount) -> {
            var byteCount = bytesPerRecord * recordCount;
            assert byteCount >= 0L : byteCount;
            assert byteCount <= Integer.MAX_VALUE : byteCount;

            var targetBuffer = MemoryUtil.memByteBuffer(destAddress, (int) byteCount);

            var file = fileSystem.findFile(fileHandle);
            var result = file.read(targetBuffer, bytesPerRecord, recordCount);

            return result;
        });

        aiFile.FileSizeProc((long fileHandle) -> {
            var file = fileSystem.findFile(fileHandle);
            return file.size();
        });

        aiFile.TellProc((long fileHandle) -> {
            var file = fileSystem.findFile(fileHandle);
            return file.currentPosition();
        });

        aiFile.SeekProc((long fileHandle, long offset, int origin) -> {
            var file = fileSystem.findFile(fileHandle);
            file.seek(offset, origin);
            return Assimp.aiReturn_SUCCESS;
        });
    }

    private long read(ByteBuffer targetBuffer, long bytesPerRecord, long recordCount) {
        var numRecordsCopied = 0L;
        while (numRecordsCopied < recordCount && content.remaining() >= bytesPerRecord) {
            // Copy one record:
            for (var byteIndex = 0L; byteIndex < bytesPerRecord; ++byteIndex) {
                var b = content.get();
                targetBuffer.put(b);
            }
            ++numRecordsCopied;
        }

        return numRecordsCopied;
    }

    private void seek(long offset, int origin) {
        long originPos = 0L;
        if (origin == Assimp.aiOrigin_SET) {
            originPos = 0L;
        } else if (origin == Assimp.aiOrigin_CUR) {
            originPos = currentPosition();
        } else {
            assert origin == Assimp.aiOrigin_END : origin;
            originPos = size();
        }

        var pos = originPos + offset;
        content.position((int) pos);
    }

    byte[] getContentArray() {
        return contentArray;
    }

    private long currentPosition() {
        var result = content.position();
        return result;
    }

    private long size() {
        var result = content.capacity();
        return result;
    }

    long handle() {
        assert aiFile != null;
        return aiFile.address();
    }

    void destroy() {
        if (aiFile != null) {
            aiFile.free();
            aiFile = null;
        }
    }
}
