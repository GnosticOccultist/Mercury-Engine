package fr.mercury.nucleus.asset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.math.objects.Vector2f;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;

public class OBJLoader implements AssetLoader<PhysicaMundi> {

	/**
	 * The logger of the mercury assets.
	 */
	private static final Logger logger = FactoryLogger.getLogger("mercury.asset");
	/**
	 * The vertex type definer inside an .obj file.
	 */
	private static final String VERTEX_TYPE = "v";
	/**
	 * The texture coordinate type definer inside an .obj file.
	 */
	private static final String TEX_COORDS_TYPE = "vt";
	/**
	 * The normal type definer inside an .obj file.
	 */
	private static final String NORMAL_TYPE = "vn";
	/**
	 * The face type definer inside an .obj file.
	 */
	private static final String FACE_TYPE = "f";
	/**
	 * The face type definer inside an .obj file.
	 */
	private static final String OBJECT_NAME = "o";
	
	@Override
	public PhysicaMundi load(String path) {
		try {
			var store = new MeshStore();
			
			var reader = FileUtils.readBuffered(path);
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				
				// Ignore any commented lines.
				if(line.length() > 0 && line.charAt(0) == '#') {
					continue;
				}
				
				// Tokenize the line.
				var tokens = line.split("\\s+");
				
				if(tokens.length == 0) {
					continue;
				}
				
				var type = tokens[0];
				
				switch (type) {
					case VERTEX_TYPE:
						var vertex = new Vector3f(Float.valueOf(tokens[1]), 
								Float.valueOf(tokens[2]), Float.valueOf(tokens[3]));
						if(tokens.length > 4) {
							logger.warning("Only 3 components is handled per vertex, but " 
									+ String.valueOf(tokens.length - 1) + " are defined in the obj file!");
						}
						store.addVertex(vertex);
						break;
					case TEX_COORDS_TYPE:
						var textureCoords = new Vector2f(Float.valueOf(tokens[1]), 
								Float.valueOf(tokens[2]));
						if(tokens.length > 3) {
							logger.warning("Only 2 components is handled per texture coordinates, but " 
									+ String.valueOf(tokens.length - 1) + " are defined in the obj file!");
						}
						store.addTextureCoord(textureCoords);
						break;
					case NORMAL_TYPE:
						var normal = new Vector3f(Float.valueOf(tokens[1]), 
								Float.valueOf(tokens[2]), Float.valueOf(tokens[3]));
						store.addNormal(normal);
						break;
					case FACE_TYPE:
						if(tokens.length < 4) {
							logger.warning("A face must have at least 3 vertices, but " 
									+ String.valueOf(tokens.length - 1) + " are defined in the obj file!");
						}
						var indices = new IndexGroup[3];
						for(int i = 0; i < tokens.length; i++) {
							indices[i] = new IndexGroup(tokens[i + 1]);
	                    }
						store.addFace(indices);
						break;
					case OBJECT_NAME:
						if(tokens.length < 2) {
							logger.warning("An object name can't be empty!");
						}
						store.setName(tokens[1]);
						break;
				}
			}
			return new PhysicaMundi(store.toMercuryMesh());
		} catch (IOException ex) {
			logger.error("Failed to load OBJ resource from path '" + path + "'!", ex);
			return null;
		}
	}
	
	protected static class MeshStore {
		
		private final List<Vector3f> vertices = new ArrayList<>();
		private final List<Vector2f> textureCoords = new ArrayList<>();
		private final List<Vector3f> normals = new ArrayList<>();
		private final List<Face> faces = new ArrayList<>();
		
		private String name;
		
		public MeshStore addVertex(Vector3f vertex) {
			this.vertices.add(vertex);
			return this;
		}
		
		public MeshStore addTextureCoord(Vector2f textureCoord) {
			this.textureCoords.add(textureCoord);
			return this;
		}
		
		public MeshStore addNormal(Vector3f normal) {
			this.normals.add(normal);
			return this;
		}
		
		public MeshStore addFace(IndexGroup[] groups) {
			this.faces.add(new Face(groups));
			return this;
		}
		
		public int size() {
			return faces.size() * 3;
		}
		
		public void reset() {
			this.vertices.clear();
			this.textureCoords.clear();
			this.normals.clear();
			this.faces.clear();
		}
		
		public Mesh toMercuryMesh() {
			var mesh = new Mesh();
			
			// TODO: Upload data to the mesh.
			
			return mesh;
		}
		
		public PhysicaMundi toMercuryPhysica() {
			if(name == null || name.isEmpty()) {
				this.name = "obj_mesh";
			}
			
			return new PhysicaMundi(name, toMercuryMesh());
		}
		
		void setName(String name) {
			this.name = name;
		}
	}
	
	protected static class Face {

		/**
		 * List of idxGroup groups for a face triangle (3 vertices per face).
		 */
		private IndexGroup[] idxGroups;
		
		public Face(IndexGroup[] groups) {
			this.idxGroups = groups;
		}

		public IndexGroup[] getFaceVertexIndices() {
			return idxGroups;
		}
	}

	protected static class IndexGroup {

		/**
		 * The no value for an index value &rarr;-1
		 */
		public static final int NO_VALUE = -1;
	        
		/**
		 * The position index for the vertex.
		 */
		public int vIndex;
		/**
		 * The texture coordinate index for the vertex.
		 */
		public int vtIndex;
		/**
		 * The normal index for the vertex.
		 */
		public int vnIndex;

		public IndexGroup(String group) {
			var tokens = group.split("/");
			this.vIndex = tokens.length < 1 ? NO_VALUE : Integer.parseInt(tokens[0]);
			this.vtIndex = tokens.length < 2 ? NO_VALUE : Integer.parseInt(tokens[1]);
			this.vnIndex = tokens.length < 3 ? NO_VALUE : Integer.parseInt(tokens[2]);
		}
	}
}
