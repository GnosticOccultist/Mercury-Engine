package fr.mercury.exempli.gratia.renderer.logic.state;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.LoggerLevel;
import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.renderer.logic.state.BlendState;
import fr.mercury.nucleus.renderer.logic.state.BlendState.BlendFunction;
import fr.mercury.nucleus.renderer.logic.state.DepthBufferState;
import fr.mercury.nucleus.renderer.logic.state.DepthBufferState.DepthFunction;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.renderer.logic.state.RenderState;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Face;
import fr.mercury.nucleus.renderer.queue.BucketType;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.texture.Texture2D;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>TestRenderState</code> showcase the usage of the {@link RenderState} implementations in a scenegraph to modify the 
 * current state of the graphics API context before rendering occurs.
 * 
 * @author GnosticOccultist
 */
public class TestRenderState extends MercuryApplication {

    /**
     * The physica-mundi to represent a cube in the scene.
     */
    private PhysicaMundi cube;
    /**
     * The physica-mundi to represent a teapot in the scene.
     */
    private PhysicaMundi teapot;
    /**
     * The physica-mundi to represent a capricorn in the scene.
     */
    private PhysicaMundi capricorn;

    static {
        FactoryLogger.getLogger("mercury.renderer").setActive(LoggerLevel.DEBUG, true);
    }

    /**
     * Launch method for the <code>TestRenderState</code>, no arguments required.
     * 
     * @param args The arguments to pass to the application.
     */
    public static void main(String[] args) {
        TestRenderState app = new TestRenderState();
        app.start();
    }

    @Override
    @OpenGLCall
    protected void initialize() {
        // Load the 2D texture for the cube and upload it directly to the GPU.
        Texture2D textureOcto = assetManager.loadTexture2D("/textures/octostone.png")
                .setFilter(MinFilter.TRILINEAR, MagFilter.BILINEAR).setWrapMode(WrapMode.REPEAT, WrapMode.CLAMP_BORDER);
        textureOcto.upload();

        // Load the 2D texture for the cube and upload it directly to the GPU.
        Texture2D texture = assetManager.loadTexture2D("/textures/simple_texture_atlas.png")
                .setFilter(MinFilter.NEAREST, MagFilter.NEAREST).setWrapMode(WrapMode.REPEAT, WrapMode.REPEAT);
        texture.upload();

        PhysicaMundi box = assetManager.loadPhysicaMundi("/model/cube.obj");
        box.setName("box");
        box.setTranslation(0.0f, 0.0f, -15f).setRotation(0.0f, 0.0f, 0.0f).setScale(1f, 1f, 1f);

        NucleusMundi transparentNucleus = new NucleusMundi("Transparent Group");

        // Load and prepare the cube in the scene.
        cube = assetManager.loadPhysicaMundi("/model/cube.obj");
        cube.setName("cube");
        cube.setTranslation(0.0f, 0.0f, -10f).setRotation(0.0f, 0.0f, 0.0f).setScale(1f, 1f, 1f);

        // Load and prepare the teapot in the scene.
        teapot = assetManager.loadPhysicaMundi("/model/teapot.obj");
        teapot.setName("teapot");
        teapot.setTranslation(-5.0f, -0.5f, -10f).setRotation(0.0f, 0.0f, 0.0f).setScale(1f, 1f, 1f);

        // Load and prepare the capricorn in the scene.
        capricorn = assetManager.loadPhysicaMundi("/model/capricorn.obj");
        capricorn.setName("capricorn");
        capricorn.setTranslation(5.0f, -1.0f, -10f).setRotation(0.0f, 0.0f, 0.0f).setScale(0.05f, 0.05f, 0.05f);

        // Select the third material which is "Unlit_no_fog" to render the cube using
        // a texture without fog effect.
        Material[] materials = assetManager.loadMaterial("/materials/unlit.json");
        assert materials[2] != null;
        materials[2].getFirstShader();

        box.setMaterial(materials[2]);
        // Set the texture of the cube to the loaded texture atlas.
        box.getMaterial().addVariable("texture_sampler", textureOcto);

        var transparentMat = materials[2].copyShader();
        cube.setMaterial(transparentMat);
        // Set the texture of the cube to the loaded texture atlas.
        cube.getMaterial().addVariable("texture_sampler", texture);
        teapot.setMaterial(transparentMat);
        capricorn.setMaterial(transparentMat);

        DepthBufferState zState = new DepthBufferState().mask().setFunction(DepthFunction.LESS_OR_EQUAL).enable();
        BlendState blendState = new BlendState().setSRCFactor(BlendFunction.SOURCE_ALPHA)
                .setDSTFactor(BlendFunction.ONE_MINUS_SOURCE_ALPHA).enable();
        transparentNucleus.setRenderStates(zState, blendState);
        transparentNucleus.setBucket(BucketType.TRANSPARENT);

        transparentNucleus.attachAll(cube, teapot, capricorn);
        // Finally, attach the cube, teapot and capricorn to the main scene.
        scene.attachAll(box, transparentNucleus);
        scene.setRenderStates(new DepthBufferState().enable(), new BlendState(),
                new FaceCullingState().setFace(Face.BACK).enable());
    }

    @Override
    @OpenGLCall
    protected void update(ReadableTimer timer) {
        super.update(timer);

        // Rotate slowly the cube, teapot and capricorn.
        cube.rotate(0, 0.01f, 0);
        teapot.rotate(0, 0.01f, 0);
        capricorn.rotate(0, 0.01f, 0);
    }
}
