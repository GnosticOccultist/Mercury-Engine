package fr.mercury.nucleus.scenegraph;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Transform;

/**
 * <code>PhysicaMundi</code> represents a physical object constituting a manifestation of the {@link AnimaMundi}.
 * It is described with a {@link Mesh}, which represents its geometry as well as a {@link Transform} to know
 * how to position, orientate or scale this mesh.
 * 
 * @author GnosticOccultist
 */
public class PhysicaMundi extends AnimaMundi {

	/**
	 * The mesh of the physica-mundi.
	 */
	private Mesh mesh;
	/**
	 * The material of the physica-mundi used for rendering.
	 */
	private Material material;
	
	/**
	 * Instantiates a new <code>PhysicaMundi</code> with no {@link Mesh}
	 * so no rendering will occur.
	 */
	public PhysicaMundi() {
		super();
	}
	
	/**
	 * Instantiates a new <code>PhysicaMundi</code> by setting its {@link Mesh} 
	 * to the provided one.
	 * 
	 * @param mesh The mesh to use (not null).
	 */
	public PhysicaMundi(Mesh mesh) {
		super();
		setMesh(mesh);
	}
	
	/**
	 * Instantiates a new <code>PhysicaMundi</code> with the given name and 
	 * by setting its {@link Mesh} to the provided one.
	 * <p>
	 * The provided mesh cannot be null.
	 * 
	 * @param mesh The mesh to use.
	 */
	public PhysicaMundi(String name, Mesh mesh) {
		super(name);
		setMesh(mesh);
	}
	
	/**
	 * Return the {@link Mesh} used by the <code>PhysicaMundi</code>.
	 * 
	 * @return The mesh of the physica-mundi.
	 */
	public Mesh getMesh() {
		return mesh;
	}
	
	/**
	 * Sets the {@link Mesh} used by the <code>PhysicaMundi</code>.
	 * <p>
	 * The provided mesh cannot be null.
	 * 
	 * @param mesh The mesh to be rendered.
	 */
	public void setMesh(Mesh mesh) {
		Validator.nonNull(mesh, "The mesh cannot be null!");
		
		this.mesh = mesh;
	}
	
	/**
	 * Return the {@link Material} used by the <code>PhysicaMundi</code>.
	 * 
	 * @return The material used to render the physica-mundi.
	 */
	public Material getMaterial() {
		return material;
	}
	
	/**
	 * Sets the {@link Material} used by the <code>PhysicaMundi</code>.
	 * <p>
	 * The provided material cannot be null.
	 * 
	 * @param material The material used to render the physica-mundi.
	 */
	public void setMaterial(Material material) {
		Validator.nonNull(material, "The material cannot be null!");
		
		this.material = material;
	}
}
