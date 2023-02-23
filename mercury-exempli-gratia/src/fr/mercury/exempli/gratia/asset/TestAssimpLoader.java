package fr.mercury.exempli.gratia.asset;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.asset.loader.AssimpLoader;
import fr.mercury.nucleus.input.Axis;
import fr.mercury.nucleus.input.Button;
import fr.mercury.nucleus.input.DelegateInputProcessor;
import fr.mercury.nucleus.input.Input;
import fr.mercury.nucleus.input.control.CameraControl;
import fr.mercury.nucleus.input.layer.InputLayer;
import fr.mercury.nucleus.input.layer.LayeredInputProcessor;
import fr.mercury.nucleus.input.layer.LayeredInputProcessor.InputListener;
import fr.mercury.nucleus.renderer.logic.state.BlendState;
import fr.mercury.nucleus.renderer.logic.state.DepthBufferState;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Face;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>TestAssimpLoader</code> showcase the usage of an {@link AssimpLoader} to load a {@link PhysicaMundi} in Mercury 
 * using the <code>Assimp</code> libraries via <code>LWJGL3</code> bindings.
 * 
 * @author GnosticOccultist
 */
public class TestAssimpLoader extends MercuryApplication implements InputListener {
    
    public static final InputLayer CAM_LOOK_X = InputLayer.function("camera", "look-x");
    
    public static final InputLayer CAM_LOOK_Y = InputLayer.function("camera", "look-y");
    
    public static final InputLayer CAM_FOCUS = InputLayer.function("camera", "focus");
    
    public static final InputLayer CAM_MOVE = InputLayer.function("camera", "move");
    
    public static final InputLayer CAM_STRAFE = InputLayer.function("camera", "strafe");

    /**
     * The physica-mundi to represent the sponza model in the scene.
     */
    private AnimaMundi sponza;
    /**
     * The camera control.
     */
    private CameraControl camControl;

    /**
     * Launch method for the <code>TestAssimpLoader</code>, no arguments required.
     * 
     * @param args The arguments to pass to the application.
     */
    public static void main(String[] args) {
        TestAssimpLoader app = new TestAssimpLoader();
        app.start();
    }

    @Override
    @OpenGLCall
    protected void initialize() {
        camControl = CameraControl.newFirstPersonControl(camera);
        
        var layeredInput = new LayeredInputProcessor();
        getService(DelegateInputProcessor.class).setDelegate(layeredInput);
        
        // Activate the camera's input group.
        layeredInput.activate(InputLayer.group("camera"));
        
        layeredInput.map(CAM_LOOK_X, Axis.MOUSE_X);
        layeredInput.map(CAM_LOOK_Y, Axis.MOUSE_Y);
        layeredInput.map(CAM_FOCUS, Button.MOUSE_LEFT);
        layeredInput.map(CAM_MOVE, Input.Keys.KEY_W);
        layeredInput.map(CAM_MOVE, -1, Input.Keys.KEY_S);
        layeredInput.map(CAM_STRAFE, -1, Input.Keys.KEY_D);
        layeredInput.map(CAM_STRAFE, Input.Keys.KEY_A);
        layeredInput.listen(this, CAM_LOOK_X, CAM_LOOK_Y, CAM_FOCUS, CAM_MOVE, CAM_STRAFE);
        
        // Load and prepare the cube in the scene.
        // TODO: Allow to add config flags when loading a model.
        sponza = assetManager.loadAnimaMundi("resources/model/sponza/sponza.gltf");
        sponza.setName("sponza");
        sponza.setTranslation(5.0f, -2.3F, 0.0F).setScale(1f, 1f, 1f);

        scene.setRenderStates(new DepthBufferState().enable(), new BlendState().enable(),
                new FaceCullingState().setFace(Face.BACK).enable());

        // Finally, attach the sponza to the main scene.
        scene.attachAll(sponza);
    }

    @Override
    @OpenGLCall
    protected void update(ReadableTimer timer) {
        camControl.update(timer);
    }

    @Override
    public void trigger(InputLayer layer, double value) {
        var input = getService(DelegateInputProcessor.class);
        if (layer == CAM_LOOK_X && !input.isCursorVisible()) {
            camControl.rotate(value, 0);
        } 
        if (layer == CAM_LOOK_Y && !input.isCursorVisible()) {
            camControl.rotate(0, value);
        }
        if (layer == CAM_FOCUS) {
            if (value == 1.0f) {
                input.setCursorVisible(false);
            } else {
                input.setCursorVisible(true);
            }
        }
        
        
        if (layer == CAM_MOVE) {
            camControl.move(value, 0, 0);
        }
        
        if (layer == CAM_STRAFE) {
            camControl.move(0, 0, value);
        }
    }
}
