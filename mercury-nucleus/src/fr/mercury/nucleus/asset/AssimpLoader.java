package fr.mercury.nucleus.asset;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIFile;
import org.lwjgl.assimp.AIFileIO;
import org.lwjgl.assimp.AIFileOpenProc;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.SystemUtils;
import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.file.FileUtils;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Transform;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.Usage;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.Mesh.Mode;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.data.BufferUtils;

public class AssimpLoader implements AssetLoader<AnimaMundi> {

	@Override
	public AnimaMundi load(String path) {
		// Define our own IO logic for Assimp.
		AIFileIO io = AIFileIO.create();
		
		var openProc = new AIFileOpenProc() {
			
			@Override
			public long invoke(long pFileIO, long fileName, long openMode) {
				var file = AIFile.create();
				var filePath = MemoryUtil.memUTF8(fileName);
				
				var buffer = MemoryUtil.memAlloc(8192);
				var data = FileUtils.toByteBuffer(filePath, buffer, this::resize);
				
				file.ReadProc((pFile, pBuffer, size, count) -> {
					var max = Math.min(data.remaining(), size * count);
                    MemoryUtil.memCopy(MemoryUtil.memAddress(data) + data.position(), pBuffer, max);
                    return max;
				});
				
				file.SeekProc((pFile, offset, origin) -> {
					if(origin == Assimp.aiOrigin_CUR) {
						data.position(data.position() + (int) offset);
                    } else if (origin == Assimp.aiOrigin_SET) {
                        data.position((int) offset);
                    } else if (origin == Assimp.aiOrigin_END) {
                        data.position(data.limit() + (int) offset);
                    }
                    return 0;
				});
				
				file.FileSizeProc(pFile -> {
					return data.limit();
				});
				
				return file.address();
			}
			
			private ByteBuffer resize(ByteBuffer buffer, Integer size) {
				var newBuffer = BufferUtils.createByteBuffer(size);
				buffer.flip();
				newBuffer.put(buffer);
				MemoryUtil.memFree(buffer);
				return newBuffer;
			}
		};
		
		io.set(openProc, (pFileIO, pFile) -> {}, MemoryUtil.NULL);
		
		AIScene scene = Assimp.aiImportFileEx(path, Assimp.aiProcess_JoinIdenticalVertices 
				| Assimp.aiProcess_Triangulate | Assimp.aiProcess_GenSmoothNormals, io);
		
		if(scene == null) {
			throw new MercuryException("Error while loading model '" 
					+ path + "': " + Assimp.aiGetErrorString());
		}
		
		AnimaMundi result = readScene(scene);
		
		/*
		 * Release the imported scene when finished.
		 */
		Assimp.aiReleaseImport(scene);
		
		return result;
	}

	private AnimaMundi readScene(AIScene scene) {
		AINode rootNode = scene.mRootNode();
		NucleusMundi rootNucleus = new NucleusMundi(rootNode.mName().dataString());
		
		int meshCount = scene.mNumMeshes();
		PointerBuffer aiMeshes = scene.mMeshes();
		AIMesh aiMesh = AIMesh.create(aiMeshes.get(0));
		
		// TODO: Handle all node hierarchy.
		// TODO: Add a flag to ignore the root-node.
			
		return processMesh(aiMesh);
	}

	/**
	 * Create and return a new {@link AnimaMundi} based on the provided <code>Assimp</code> mesh data.
	 * 
	 * @param aiMesh The mesh data to create the anima-mundi from (not null).
	 * @return		 A new anima-mundi with a mesh matching the one loaded with assimp (not null).
	 */
	private AnimaMundi processMesh(AIMesh aiMesh) {
		Validator.nonNull(aiMesh, "The Assimp mesh data can't be null!");
		var mesh = new Mesh();
		
		toFloatBuffer(aiMesh.mVertices(), 3, b -> mesh.setupBuffer(VertexBufferType.POSITION, Usage.STATIC_DRAW, b));
		toFloatBuffer(aiMesh.mTextureCoords(0), 2, b -> mesh.setupBuffer(VertexBufferType.TEX_COORD, Usage.STATIC_DRAW, b));
		toFloatBuffer(aiMesh.mNormals(), 3, b -> mesh.setupBuffer(VertexBufferType.NORMAL, Usage.STATIC_DRAW, b));
		toFloatBuffer(aiMesh.mTangents(), 3, b -> mesh.setupBuffer(VertexBufferType.TANGENT, Usage.STATIC_DRAW, b));
		
		toIntBuffer(aiMesh.mFaces(), b -> mesh.setupBuffer(VertexBufferType.INDEX, Usage.STATIC_DRAW, b));
		
		var mode = convertPrimitive(aiMesh.mPrimitiveTypes());
		mesh.setMode(mode);
		
		mesh.upload();
		return new PhysicaMundi(aiMesh.mName().dataString(), mesh);
	}
	
	/**
	 * Converts the given <code>Assimp</code> primitive type to the corresponding {@link Mode} for
	 * the {@link Mesh} to create.
	 * 
	 * @param type The primitive type to convert.
	 * @return	   A mesh mode corresponding to the assimp library primitive type (not null).
	 * 
	 * @throws UnsupportedOperationException Thrown if the primitive type isn't handled by the loader.
	 */
	private Mesh.Mode convertPrimitive(int type) {
		switch (type) {
			case Assimp.aiPrimitiveType_POINT:
				return Mode.POINTS;
			case Assimp.aiPrimitiveType_LINE:
				return Mode.LINES;
			case Assimp.aiPrimitiveType_TRIANGLE:
				return Mode.TRIANGLES;
			default:
				throw new UnsupportedOperationException("Unsupported primitive type with index: '" + type + "'");
		}
	}
	
	/**
	 * Converts the given <code>Assimp</code> 4x4 matrix into a {@link Matrix4f} readable by a {@link Transform}.
	 * 
	 * @param m		The 4x4 matrix to convert (not null).
	 * @param store The matrix to store the result in or null to instantiate a new one.
	 * @return		The store matrix containing the converted data or a new instanced one.
	 */
	private Matrix4f convertMatrix(AIMatrix4x4 m, Matrix4f store) {
		Validator.nonNull(m, "The Assimp matrix can't be null!");
		var result = store == null ? new Matrix4f() : store;
		return result.set(m.a1(), m.a2(), m.a3(), m.a4(), m.b1(), m.b2(), m.b3(), m.b4(), 
				m.c1(), m.c2(), m.c3(), m.c4(), m.d1(), m.d2(), m.d3(), m.d4());
	}
	
	private void toIntBuffer(AIFace.Buffer source, Consumer<IntBuffer> consumer) {
		var count = source != null ? source.remaining() : 0;
		if(count == 0) {
			return;
		}
		
		var buffer = BufferUtils.createIntBuffer(count * 3);
		for(var i = 0; i < count; ++i) {
			var face = source.get();
			buffer.put(face.mIndices());
		}
		
		consumer.accept(buffer);
	}
	
	private void toFloatBuffer(AIVector3D.Buffer source, int componentSize, Consumer<FloatBuffer> consumer) {
		var count = source != null ? source.remaining() : 0;
		if(count == 0) {
			// FIXME : Should we send an empty vertex buffer here ?
			return;
		}
		
		var buffer = BufferUtils.createFloatBuffer(count * componentSize);
		for(var i = 0; i < count; ++i) {
			var vec = source.get();
			buffer.put(vec.x());
			buffer.put(vec.y());
			
			if(componentSize > 2) {
				buffer.put(vec.z());
			}
		}
		
		consumer.accept(buffer);
	}
}
