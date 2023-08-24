package fr.mercury.exempli.gratia.texture;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.application.service.TaskExecutorService;
import fr.mercury.nucleus.renderer.logic.state.BlendState;
import fr.mercury.nucleus.renderer.logic.state.DepthBufferState;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Face;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.Mesh;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.TextureAtlas;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>TestTextureAtlas</code> showcase the usage of {@link TextureAtlas} to save on <code>OpenGL</code> 
 * texture units when applying a texture to a {@link Mesh}.
 * 
 * @author GnosticOccultist
 */
public class TestTextureAtlas extends MercuryApplication {

    /**
     * The physica-mundi to represent a cube in the scene.
     */
    private PhysicaMundi cube;
    /**
     * The texture altas used for texturing the cube mesh.
     */
    private TextureAtlas atlas;

    /**
     * Launch method for the <code>TestTextureAtlas</code>, no arguments required.
     * 
     * @param args The arguments to pass to the application.
     */
    public static void main(String[] args) {
        TestTextureAtlas app = new TestTextureAtlas();
        app.start();
    }

    @Override
    @OpenGLCall
    protected void initialize() {
        // Load the 2x2 texture atlas, make sure to apply nearest filtering
        // to prevent adjacent texture borders.
        atlas = assetManager.loadTextureAtlas("/textures/simple_texture_atlas.png", 2, 2)
                .setFilter(MinFilter.NEAREST, MagFilter.NEAREST)
                .setWrapMode(WrapMode.CLAMP_EDGES, WrapMode.CLAMP_EDGES);
        atlas.upload();

        // Load and prepare the cube in the scene.
        cube = assetManager.loadPhysicaMundi("/model/cube.obj");
        cube.setName("cube");
        cube.setTranslation(0.0f, 0.0f, -7.0f).setRotation(0.0f, 0.0f, 0.0f).setScale(1f, 1f, 1f);

        // Select the fourth material which is "Unlit_atlas" to render the cube using
        // a texture atlas.
        Material[] materials = assetManager.loadMaterial("/materials/unlit.json");
        assert materials[3] != null;
        cube.setMaterial(materials[3]);
        // Set the texture of the cube to the loaded texture atlas.
        cube.getMaterial().addVariable("texture_sampler", atlas);

        // Finally, attach the cube to the main scene.
        scene.attach(cube);

        scene.setRenderStates(new DepthBufferState(), new BlendState(),
                new FaceCullingState().setFace(Face.BACK).enable());

        // Link the task execution module to the application and schedule an updating
        // index task each second.
        TaskExecutorService module = new TaskExecutorService();
        module.scheduleAtFixedRate(this::updateIndex, 1000);
        linkService(module);
    }

    @Override
    @OpenGLCall
    protected void update(ReadableTimer timer) {
        // Rotate slowly the cube.
        cube.rotate(0.01f, 0.01f, 0.01f);
    }

    /**
     * Update the {@link TextureAtlas} index by switching to the next one (in row
     * order).
     */
    private void updateIndex() {
        var nextIndex = atlas.getIndex() + 1;
        if (nextIndex > 3) {
            nextIndex = 0;
        }

        atlas.setIndex(nextIndex);
    }
}
