package fr.mercury.exempli.gratia.renderer.instancing;

import java.util.Random;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.asset.AssimpLoader.ConfigFlag;
import fr.mercury.nucleus.math.objects.Transform;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState.WindingOrder;
import fr.mercury.nucleus.renderer.logic.state.PolygonModeState;
import fr.mercury.nucleus.renderer.logic.state.PolygonModeState.PolygonMode;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Face;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.Usage;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.TextureAtlas;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
import fr.mercury.nucleus.utils.data.BufferUtils;

/**
 * <code>TestRenderState</code> showcase the usage of a {@link Mesh} in a simple case of instanced rendering. 
 * A mesh will use instanced rendering automatically when {@link Mesh#getInstanceCount()} returns a value greater than one.
 * <p>
 * Instanced rendering allows to render multiple instances of the same mesh using only one draw call. But, there is also one 
 * drawback, which is that the user needs to create a vertex attribute (limited at a size of 4 per vertex data) whenever he 
 * wants to change an instance instead of using uniforms.
 * 
 * @author GnosticOccultist
 */
public class TestMeshInstancing extends MercuryApplication {

	/**
	 * The random object to randomize transforms.
	 */
	private static final Random RANDOM = new Random(171717);

	/**
	 * Launch method for the <code>TestMeshInstancing</code>, no arguments required.
	 * 
	 * @param args The arguments to pass to the application.
	 */
	public static void main(String[] args) {
		var app = new TestMeshInstancing();
		app.start();
	}

	@Override
	protected void initialize() {

		// Load the 2x2 texture atlas, make sure to apply nearest filtering
		// to prevent adjacent texture borders.
		TextureAtlas atlas = assetManager.loadTextureAtlas("/textures/simple_texture_atlas.png", 2, 2)
				.setFilter(MinFilter.NEAREST, MagFilter.NEAREST)
				.setWrapMode(WrapMode.CLAMP_EDGES, WrapMode.CLAMP_EDGES);
		atlas.upload();

		// Select the first index of the atlas.
		atlas.setIndex(0);

		/*
		 * Load and prepare the cube in the scene. Its transform will be used as a
		 * reference to all other instances, but with a delta applied.
		 */
		var cube = (PhysicaMundi) assetManager.loadAssimp("/model/cube.obj", ConfigFlag.IGNORE_ROOT_NODE);
		cube.setName("cube");
		cube.setTranslation(0.0f, 0.0f, 2f).setRotation(0.0f, 0.0f, 0.0f).setScale(1f, 1f, 1f);

		/*
		 * Select the fifth material which is "Unlit_no_fog_instanced" to render the
		 * cube using mesh instancing. This material requires a special vertex attribute
		 * named "instanceMatrix", which will contain the matrix transform to apply to
		 * each instance.
		 */
		Material[] materials = assetManager.loadMaterial("/materials/unlit.json");
		assert materials[4] != null;
		cube.setMaterial(materials[4]);
		cube.getMaterial().texture = atlas;

		/*
		 * Tell the mesh that it needs to be rendered using an instanced draw call, by
		 * setting an instance count greater than one.
		 */
		var mesh = cube.getMesh();
		var instanceCount = 100;
		mesh.setInstanceCount(instanceCount);

		/*
		 * Create and populate the buffer with one 4x4 float matrix per instance.
		 */
		var data = BufferUtils.createFloatBuffer(instanceCount * 16);
		for (int i = 0; i < instanceCount; i++) {
			var transform = randomizeTransform(RANDOM);
			transform.populate(data);
		}
		// Flip the buffer since the method above uses relative put methods.
		data.flip();

		/*
		 * Finally setup a VertexBuffer for the "instanceMatrix" attribute which
		 * contains the buffer.
		 */
		mesh.setupBuffer("instanceMatrix", 4, Usage.DYNAMIC_DRAW, data);

		var polygonState = new PolygonModeState().setPolygonMode(Face.FRONT_AND_BACK, PolygonMode.LINE).enable();
		var faceCulling = new FaceCullingState().setFace(Face.BACK).setWindingOrder(WindingOrder.CLOCKWISE).enable();
		scene.setRenderStates(polygonState, faceCulling);

		scene.attach(cube);
	}

	/**
	 * Randomize the {@link Transform} for each instance using the given {@link Random}.
	 * 
	 * @param rand The random to use.
	 * @return 	   A new transform with a randomized translation.
	 */
	private Transform randomizeTransform(Random rand) {
		var result = new Transform();

		// Use a delta of 2 for the translation.
		var delta = 2;

		var offsetX = (float) (rand.nextDouble() * (delta * 2) - delta);
		var offsetY = (float) (rand.nextDouble() * (delta * 2) - delta);
		var offsetZ = (float) (rand.nextDouble() * (delta * 2) - delta);

		result.translate(offsetX, offsetY, offsetZ);

		return result;
	}
}
