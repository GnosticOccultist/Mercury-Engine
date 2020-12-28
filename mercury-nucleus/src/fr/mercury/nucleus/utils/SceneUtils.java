package fr.mercury.nucleus.utils;

import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.scenegraph.visitor.VisitType;

public final class SceneUtils {
	
	/**
	 * Private constructor to inhibit instantiation of <code>SceneUtils</code>.
	 */
	private SceneUtils() {}
	
	public static void applyMaterial(AnimaMundi anima, Material material) {
		anima.visit(a -> {
			if(a instanceof PhysicaMundi) {
				var physica = (PhysicaMundi) a;
				var oldTexture = physica.getMaterial().texture;
				physica.setMaterial(material.copy(oldTexture));
				
			}
		}, VisitType.PRE_ORDER);
	}
}
