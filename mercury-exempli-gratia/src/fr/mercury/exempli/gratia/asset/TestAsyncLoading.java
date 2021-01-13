package fr.mercury.exempli.gratia.asset;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.application.MercuryContext;
import fr.mercury.nucleus.application.service.TaskExecutorService;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.renderer.logic.state.BlendState;
import fr.mercury.nucleus.renderer.logic.state.DepthBufferState;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Face;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>TestAsyncLoading</code> gives an example on how to load an asset asynchronously using the {@link AssetManager}.
 * 
 * @author GnosticOccultist
 */
public class TestAsyncLoading extends MercuryApplication {

    /**
     * Launch method for the <code>TestAsyncLoading</code>, no arguments required.
     * 
     * @param args The arguments to pass to the application.
     */
    public static void main(String[] args) {
        TestAsyncLoading app = new TestAsyncLoading();
        app.start();
    }

    @Override
    @OpenGLCall
    protected void initialize() {
        // First, add a task executor service.
        var executor = new TaskExecutorService();
        linkService(executor);

        // Load a complex model, like the sponza, asynchronously and then upload it on
        // the main thread.
        assetManager.loadAnimaMundiAsync("resources/model/sponza/sponza.gltf", executor, a -> uploadOnMainThread(a));

        logger.info("Continuing on " + Thread.currentThread().getName() + "...");

        scene.setRenderStates(new DepthBufferState().enable(), new BlendState().enable(),
                new FaceCullingState().setFace(Face.BACK).enable());
    }

    @OpenGLCall
    private void uploadOnMainThread(AnimaMundi animaMundi) {
        assert MercuryContext.isMainThread();

        // Configure the transform and name of the model, and attach it to the scene so it
        // can be rendered.
        animaMundi.setTranslation(5.0f, -2.3F, 0.0F).setScale(1f, 1f, 1f);
        animaMundi.setName("Sponza");
        scene.attach(animaMundi);

        logger.info("Successfully loaded model " + animaMundi + " and upload it to the GPU on "
                + Thread.currentThread().getName());
    }

    @Override
    @OpenGLCall
    protected void update(ReadableTimer timer) {

    }
}