package fr.mercury.nucleus.asset.loader.assimp;

import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.assimp.AIFileIO;
import org.lwjgl.system.MemoryUtil;

import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.asset.loader.data.AssetData;
import fr.mercury.nucleus.asset.loader.data.PathAssetData;

class AssimpFileSystem {

    private final AssetManager assetManager;

    private AIFileIO fileIO;

    private final Map<Long, AssimpFile> openedFiles;

    private final Map<String, byte[]> contentCache;

    AssimpFileSystem(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.openedFiles = new TreeMap<>();
        this.contentCache = new TreeMap<>();

        // Define our own IO logic for Assimp.
        this.fileIO = AIFileIO.calloc();

        fileIO.OpenProc((long fileIOHandle, long fileName, long openMode) -> {
            var filePath = MemoryUtil.memUTF8Safe(fileName);

            var assetData = new PathAssetData(filePath);

            var fileHandle = open(assetData);
            return fileHandle;
        });

        fileIO.CloseProc((long fileIOHandle, long fileHandle) -> {
            assert fileIOHandle == fileIO.address();

            var openFile = findFile(fileHandle);
            if (openFile != null) {
                openFile.destroy();
                openedFiles.remove(fileHandle);
            }
        });
    }

    private long open(AssetData data) {
        var result = 0L;
        if (data != null) {
            var path = data.getPath().toString();
            var contentArray = contentCache.get(path);

            var file = new AssimpFile(this, data, contentArray);
            result = file.handle();
            openedFiles.put(result, file);

            // Cache the content for future use:
            contentArray = file.getContentArray();
            contentCache.put(path, contentArray);
        }

        return result;
    }

    AssimpFile findFile(long fileHandle) {
        var result = openedFiles.get(fileHandle);
        return result;
    }

    AIFileIO getFileIO() {
        return fileIO;
    }

    void destroy() {

        for (var file : openedFiles.values()) {
            file.destroy();
        }

        openedFiles.clear();

        if (fileIO != null) {
            fileIO.free();
            fileIO = null;
        }
    }
}
