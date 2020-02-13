package fr.mercury.nucleus.asset;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.math.objects.Vector2f;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.Usage;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.Mesh.Mode;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.data.BufferUtils;

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
	 * The object name definer inside an .obj file.
	 */
	private static final String OBJECT_NAME = "o";
	/**
	 * The group name definer inside an .obj file.
	 */
	private static final String GROUP_NAME = "g";
	/**
	 * The smoothing groip definer inside an .obj file.
	 */
	private static final String SMOOTHING_GROUP_TYPE = "s";
	
	/**
	 * The store used for storing the loaded data.
	 */
	private final MeshStore store = new MeshStore();
	
	@Override
	public PhysicaMundi load(String path) {
		try {
			// Clear the store for the new loaded OBJ file.
			store.clear();
			// Set the name of the geometry to the filename by default.
			store.setName(FileUtils.getFileName(path));
			
			var reader = FileUtils.readBuffered(path);
			
			String line = null;
			long currentSmoothGroup = -1;
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
					case GROUP_NAME:
						if(tokens.length < 2) {
							logger.warning("An object name can't be empty!");
						}
						
						// Find all group names and set them to the store.
						final String[] currentGroupNames = new String[tokens.length - 1];
	                    store.setCurrentGroupNames(currentGroupNames);
	                    System.arraycopy(tokens, 1, currentGroupNames, 0, tokens.length - 1);
						break;
					case SMOOTHING_GROUP_TYPE:
						if(tokens.length != 2) {
							logger.warning("A smoothing group should only define an index, but found " 
									+ String.valueOf(tokens.length - 1) + " arguments.");
						}
						if("off".equalsIgnoreCase(tokens[1])) {
							currentSmoothGroup = 0;
						} else {
							currentSmoothGroup = Long.parseLong(tokens[1]);
						}
						break;
					case FACE_TYPE:
						if(tokens.length < 4) {
							logger.warning("A face must have at least 3 vertices, but " 
									+ String.valueOf(tokens.length - 1) + " are defined in the obj file!");
						}
						int size = tokens.length - 1;
						if(tokens.length == 5) {
							size = 6;
						}
						var indices = new IndexGroup[size];
						for(int i = 0; i < tokens.length - 1; i++) {
							indices[i] = new IndexGroup(tokens[i + 1], currentSmoothGroup);
	                    }
						// If we have 4 elements per face, build a triangle fan.
						if(tokens.length == 5) {
							indices[3] = new IndexGroup(tokens[0 + 1], currentSmoothGroup);
							indices[4] = new IndexGroup(tokens[2 + 1], currentSmoothGroup);
							indices[5] = new IndexGroup(tokens[3 + 1], currentSmoothGroup);
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
			return store.toMercuryPhysica();
		} catch (IOException ex) {
			logger.error("Failed to load OBJ resource from path '" + path + "'!", ex);
			return null;
		}
	}
	
	/**
	 * <code>MeshStore</code> is a utility class for storing the vertex data of a geometry loaded from
	 * an OBJ file.
	 * 
	 * @author GnosticOccultist
	 */
	protected static class MeshStore {
		
		/**
		 * The list of vertices data loaded from a file.
		 */
		private final List<Vector3f> vertices = new ArrayList<>();
		/**
		 * The list of texture coordinates data loaded from a file.
		 */
		private final List<Vector2f> textureCoords = new ArrayList<>();
		/**
		 * The list of normals data loaded from a file.
		 */
		private final List<Vector3f> normals = new ArrayList<>();
		/**
		 * The list of faces data loaded from a file.
		 */
		private final List<Face> faces = new ArrayList<>();
		
		/**
		 * The name of the loaded geometry.
		 */
		private String name;
		/**
		 * The array of group names of the loaded geometry.
		 */
		private String[] groupNames;
		/**
		 * The count of elements per face.
		 */
		private int elementsPerFace;
		
		/**
		 * Add a new vertex data as a {@link Vector3f} to the <code>MeshStore</code>.
		 * 
		 * @param vertex The vertex data loaded from a file.
		 * @return		 The mesh store for chaining purposes.
		 */
		public MeshStore addVertex(Vector3f vertex) {
			this.vertices.add(vertex);
			return this;
		}

		/**
		 * Add a new texture coordinates data as a {@link Vector2f} to the <code>MeshStore</code>.
		 * 
		 * @param textureCoords The texture coordinates data loaded from a file.
		 * @return		 		The mesh store for chaining purposes.
		 */
		public MeshStore addTextureCoord(Vector2f textureCoords) {
			this.textureCoords.add(textureCoords);
			return this;
		}
		
		/**
		 * Add a new normal data as a {@link Vector3f} to the <code>MeshStore</code>.
		 * 
		 * @param normal The normal data loaded from a file.
		 * @return		 The mesh store for chaining purposes.
		 */
		public MeshStore addNormal(Vector3f normal) {
			this.normals.add(normal);
			return this;
		}
		
		/**
		 * Add a new {@link Face} composing by the given {@link IndexGroup} to the <code>MeshStore</code>.
		 * 
		 * @param groups The index groups loaded from a file.
		 * @return		 The mesh store for chaining purposes.
		 */
		public MeshStore addFace(IndexGroup[] groups) {
			this.faces.add(new Face(groups));
			this.elementsPerFace = groups.length;
			return this;
		}
		
		/**
		 * The count of elements stored in the <code>MeshStore</code>.
		 * 
		 * @return The count of elements stored (&ge;0).
		 */
		public int size() {
			return faces.size() * elementsPerFace;
		}
		
		/**
		 * Clears the <code>MeshStore</code> in order to be used for loading a new geometry 
		 * from an OBJ file.
		 */
		public void clear() {
			this.name = null;
			this.groupNames = null;
			this.vertices.clear();
			this.textureCoords.clear();
			this.normals.clear();
			this.faces.clear();
		}
		
		/**
		 * Converts the <code>MeshStore</code> to a {@link Mesh} matching each loaded vertices data
		 * from the OBJ file. 
		 * 
		 * @return A new mesh matching the data stored (not null).
		 */
		public Mesh toMercuryMesh() {
			var mesh = new Mesh();
			
			FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(vertices.size() * 3);
			FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(vertices.size() * 2);
			FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(vertices.size() * 3);
			Buffer buffer = BufferUtils.createIndicesBuffer(size(), vertices.size() - 1);
			
			for(int i = 0; i < vertices.size(); i++) {
				Vector3f vertex = vertices.get(i);
				BufferUtils.populate(positionBuffer, vertex, i);
			}
			
			int index = 0;
			for (Face face : faces) {
				IndexGroup[] groups = face.getFaceVertexIndices();
				for(IndexGroup group : groups) {
					int vIndex = group.vIndex - 1;
					
					if(buffer instanceof ByteBuffer) {
						((ByteBuffer) buffer).put(index, (byte) vIndex);
					} else if(buffer instanceof ShortBuffer) {
						((ShortBuffer) buffer).put(index, (short) vIndex);
					} else if(buffer instanceof IntBuffer) {
						((IntBuffer) buffer).put(index, vIndex);
					}
					
					if(group.vtIndex > IndexGroup.NO_VALUE) {
						Vector2f texCoords = textureCoords.get(group.vtIndex - 1);
						// OpenGL needs the Y-axis to go down, so Y = 1 - V.
						texCoords.set(texCoords.x, 1F - texCoords.y);
						BufferUtils.populate(texCoordBuffer, texCoords, vIndex);
					}
					
					if(group.vnIndex > IndexGroup.NO_VALUE) {
						Vector3f normal = normals.get(group.vnIndex - 1);
						BufferUtils.populate(normalsBuffer, normal, vIndex);
					}
					index++;
				}
			}
			
			mesh.setupBuffer(VertexBufferType.POSITION, Usage.STATIC_DRAW, positionBuffer);
			mesh.setupBuffer(VertexBufferType.TEX_COORD, Usage.STATIC_DRAW, texCoordBuffer);
			mesh.setupBuffer(VertexBufferType.NORMAL, Usage.STATIC_DRAW, normalsBuffer);
			
			mesh.setupBuffer(VertexBufferType.INDEX, Usage.STATIC_DRAW, buffer);
			
			mesh.setMode(Mode.TRIANGLES);
			// Upload to the mesh to be ready for rendering.
			mesh.upload();
			
			return mesh;
		}
		
		/**
		 * Converts the <code>MeshStore</code> to a {@link PhysicaMundi} matching each loaded
		 * vertices data and the name from the OBJ file.
		 * 
		 * @return A new physica-mundi matching the data stored (not null).
		 */
		public PhysicaMundi toMercuryPhysica() {
			if(name == null || name.isEmpty()) {
				this.name = "obj_mesh";
			}
			
			return new PhysicaMundi(name, toMercuryMesh());
		}
		
		/**
		 * Sets the name of the {@link PhysicaMundi} which will be created from the OBJ file.
		 * 
		 * @param name The desired name of the physica-mundi.
		 */
		void setName(String name) {
			this.name = name;
		}
		
		/**
		 * Sets the group names of the current geometry being read from the OBJ file.
		 * 
		 * @param groupNames The array of group names.
		 */
		void setCurrentGroupNames(String[] groupNames) {
			this.groupNames = groupNames;
		}
	}
	
	/**
	 * <code>Face</code> is composed of {@link IndexGroup}, for example 3 in a triangle shape. This class is used to correctly
	 * get the indices used when rendering a {@link Mesh}.
	 * 
	 * @author GnosticOccultist
	 */
	protected static class Face {

		/**
		 * The array of indices groups for the face.
		 */
		private IndexGroup[] indicesGroups;
		
		/**
		 * Instantiates a new <code>Face</code> with the provided {@link IndexGroup}, the number depending on
		 * the type of the face. For a triangle shape needs to define 3 indices groups.
		 * 
		 * @param groups The indices groups composing the face (not null, not empty).
		 */
		public Face(IndexGroup[] groups) {
			Validator.nonEmpty(groups, "The indices groups can't be null!");
			this.indicesGroups = groups;
		}

		/**
		 * Return the array of {@link IndexGroup} composing the <code>Face</code>.
		 * 
		 * @return The array of indices groups composing the face (not null, not empty).
		 */
		public IndexGroup[] getFaceVertexIndices() {
			return indicesGroups;
		}
	}

	/**
	 * <code>IndexGroup</code> is a utility class for storing the index of data of a {@link Face} vertex in an OBJ file.
	 * 
	 * @author GnosticOccultist
	 */
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
		/**
		 * The smoothing group for the index group.
		 */
		private long smoothingGroup;

		public IndexGroup(String group, long smoothingGroup) {
			var tokens = group.split("/");
			this.vIndex = tokens.length < 1 ? NO_VALUE : Integer.parseInt(tokens[0]);
			// Here we check that the texture coordinate index exist, as an OBJ file may define normals without them.
			this.vtIndex = tokens.length < 2 ? NO_VALUE : tokens[1].isEmpty() ? NO_VALUE : Integer.parseInt(tokens[1]);
			this.vnIndex = tokens.length < 3 ? NO_VALUE : Integer.parseInt(tokens[2]);
			this.smoothingGroup = smoothingGroup;
		}
		
		/**
		 * Return the smoothing group index of the <code>IndexGroup</code>.
		 * 
		 * @return The smoothing group index, or 0 if it doesn't use smoothing.
		 */
		public long getSmoothingGroup() {
			// A normal index has been defined, so we don't use smoothing group.
			if(vnIndex >= 0) {
				return 0;
			}
			return smoothingGroup;
		}
	}
}
