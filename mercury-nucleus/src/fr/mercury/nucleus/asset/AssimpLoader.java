package fr.mercury.nucleus.asset;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

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
		System.out.println(path);
		AIScene scene = Assimp.aiImportFile(path, Assimp.aiProcess_JoinIdenticalVertices 
				| Assimp.aiProcess_Triangulate | Assimp.aiProcess_GenSmoothNormals);
		
		if(scene == null) {
			throw new MercuryException("Error while loading model '" 
					+ path + "': " +Assimp.aiGetErrorString());
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
		for(int i = 0; i < meshCount; i++) {
			AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
			
			return processMesh(aiMesh);
		}
		
		return rootNucleus;
	}

	private AnimaMundi processMesh(AIMesh aiMesh) {
		Mesh mesh = new Mesh();
		mesh.setMode(convertPrimitive(aiMesh.mPrimitiveTypes()));
		
		mesh.setupBuffer(VertexBufferType.POSITION, Usage.STATIC_DRAW, fillFloatBuffer(aiMesh.mVertices(), 3));
		mesh.setupBuffer(VertexBufferType.TEX_COORD, Usage.STATIC_DRAW, fillFloatBuffer(aiMesh.mTextureCoords(0), 2));
		mesh.setupBuffer(VertexBufferType.NORMAL, Usage.STATIC_DRAW, fillFloatBuffer(aiMesh.mNormals(), 3));
		mesh.setupBuffer(VertexBufferType.TANGENT, Usage.STATIC_DRAW, fillFloatBuffer(aiMesh.mTangents(), 3));
		
		mesh.setupBuffer(VertexBufferType.INDEX, Usage.STATIC_DRAW, fillIntBuffer(aiMesh.mFaces()));
		
		mesh.upload();
		return new PhysicaMundi(aiMesh.mName().dataString(), mesh);
	}
	
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
	
	private IntBuffer fillIntBuffer(AIFace.Buffer source) {
		int count = source.remaining();
		
		IntBuffer buffer = BufferUtils.createIntBuffer(count * 3);
		for(int i = 0; i < count; ++i) {
			AIFace face = source.get();
			buffer.put(face.mIndices());
		}
		
		return buffer;
	}
	
	private FloatBuffer fillFloatBuffer(AIVector3D.Buffer source, int componentSize) {
		int count = source.remaining();
		
		FloatBuffer buffer = BufferUtils.createFloatBuffer(count * componentSize);
		for(int i = 0; i < count; ++i) {
			AIVector3D vec = source.get();
			buffer.put(vec.x());
			buffer.put(vec.y());
			
			if(componentSize > 2) {
				buffer.put(vec.z());
			}
		}
		
		return buffer;
	}
}
