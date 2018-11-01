package fr.mercury.nucleus.scenegraph;

import fr.alchemy.utilities.Validator;

/**
 * <code>PhysicaMundi</code> represents a physical object constituting a manifestation of the <code>AnimaMundi</code>.
 * It is described with a <code>Mesh</code>, which represents its geometry as well as a <code>Transform</code> to know
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
	 * Instantiates a new <code>PhysicaMundi</code> by creating an
	 * empty <code>Mesh</code> so no rendering will occur.
	 */
	public PhysicaMundi() {
		this(new Mesh());
	}
	
	/**
	 * Instantiates a new <code>PhysicaMundi</code> by setting its 
	 * <code>Mesh</code> to the provided one.
	 * <p>
	 * The provided mesh cannot be null.
	 * 
	 * @param mesh The mesh to use.
	 */
	public PhysicaMundi(Mesh mesh) {
		super();
		setMesh(mesh);
	}
	
	/**
	 * Return the <code>Mesh</code> used by the <code>PhysicaMundi</code>.
	 * 
	 * @return The mesh of the physica-mundi.
	 */
	public Mesh getMesh() {
		return mesh;
	}
	
	/**
	 * Sets the <code>Mesh</code> used by the <code>PhysicaMundi</code>.
	 * <p>
	 * The provided mesh cannot be null.
	 * 
	 * @param mesh The mesh to be rendered.
	 */
	public void setMesh(Mesh mesh) {
		Validator.nonNull(mesh, "The mesh cannot be null!");
		
		this.mesh = mesh;
	}
}
