package fr.mercury.exempli.gratia.asset;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.LoggerLevel;
import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.application.MercurySettings;
import fr.mercury.nucleus.input.Axis;
import fr.mercury.nucleus.input.Button;
import fr.mercury.nucleus.input.DelegateInputProcessor;
import fr.mercury.nucleus.input.Input;
import fr.mercury.nucleus.input.InputState;
import fr.mercury.nucleus.input.control.CameraControl;
import fr.mercury.nucleus.input.layer.InputLayer;
import fr.mercury.nucleus.input.layer.LayeredInputProcessor;
import fr.mercury.nucleus.input.layer.LayeredInputProcessor.InputStateListener;
import fr.mercury.nucleus.input.layer.LayeredInputProcessor.InputValueListener;
import fr.mercury.nucleus.math.MercuryMath;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.scenegraph.shape.Quad;
import fr.mercury.nucleus.texture.Texture;
import fr.mercury.nucleus.texture.TextureState.MagFilter;
import fr.mercury.nucleus.texture.TextureState.MinFilter;
import fr.mercury.nucleus.texture.TextureState.WrapMode;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>HelloKTX2App</code> is the base example to render a KTX 2.0 mipmapped
 * texture onto a spinning {@link Quad}.
 * <p>
 * Available controls:
 * <li>Left mouse button &rarr; focus camera to be able to rotate the camera
 * view, by keeping it pressed</li>
 * <li>Mouse motion &rarr; rotate the camera view when focused</li>
 * <li>W / S key &rarr; move the camera along the Z-axis</li>
 * <li>A / D key &rarr; move the camera along the X-axis</li>
 * <li>Space / Left-shift key &rarr; move the camera along the Y-axis</li>
 * <li>Mouse motion &rarr; rotate the camera view when focused</li>
 * <li>Left arrow &rarr; decrement the mipmap level of the KTX texture</li>
 * <li>Right arrow &rarr; increment the mipmap level of the KTX texture</li>
 * <li>N key &rarr; toggle mipmapping for the KTX texture</li>
 * 
 * @author GnosticOccultist
 */
public class HelloKTX2App extends MercuryApplication implements InputValueListener, InputStateListener {

    public static final InputLayer CAM_LOOK_X = InputLayer.function("camera", "look-x");

    public static final InputLayer CAM_LOOK_Y = InputLayer.function("camera", "look-y");

    public static final InputLayer CAM_FOCUS = InputLayer.function("camera", "focus");

    public static final InputLayer CAM_MOVE = InputLayer.function("camera", "move");

    public static final InputLayer CAM_STRAFE = InputLayer.function("camera", "strafe");

    public static final InputLayer CAM_ELEVATE = InputLayer.function("camera", "elevate");

    public static final InputLayer BASE_LEVEL = InputLayer.function("camera", "base_level");

    public static final InputLayer ENABLE_MIPMAPPING = InputLayer.function("camera", "enable_mipmapping");

    static {
        // Enable debug level logging for the KTX file loader.
        FactoryLogger.getLogger("mercury.assets.ktx").setActive(LoggerLevel.DEBUG, true);
    }

    /**
     * The KTX texture.
     */
    private Texture texture;
    /**
     * The quad geometry
     */
    private PhysicaMundi quad;
    /**
     * The camera control.
     */
    private CameraControl camControl;
    /**
     * The current base mipmap level.
     */
    private int currentBaseLevel;
    /**
     * Whether mipmapping is enabled.
     */
    private boolean mipmappingEnabled = true;
    /**
     * The angle of rotation for the quad.
     */
    private float angle;

    /**
     * Launch method for the <code>HelloKTX2App</code>, no arguments required.
     * 
     * @param args The arguments to pass to the application.
     */
    public static void main(String[] args) {
        var app = new HelloKTX2App();

        var settings = new MercurySettings(true);
        settings.setTitle("HelloKTX2App");
        // Enable graphics and memory allocation debug output.
        settings.setGraphicsDebugOutput(true);
        settings.setMemoryAllocationDebug(true);
        // Enable gamma correction.
        settings.setGammaCorrection(true);
        // Set the modified settings.
        app.setSettings(settings);

        // Start the application.
        app.start();
    }

    @Override
    @OpenGLCall
    protected void initialize() {
        this.camControl = CameraControl.newFirstPersonControl(camera);

        // Set the delegate input processor.
        var layeredInput = new LayeredInputProcessor();
        getService(DelegateInputProcessor.class).setDelegate(layeredInput);

        // Activate the camera's input group.
        layeredInput.activate(InputLayer.group("camera"));

        // Add mappings for each of the input layers.
        layeredInput.map(CAM_LOOK_X, Axis.MOUSE_X);
        layeredInput.map(CAM_LOOK_Y, Axis.MOUSE_Y);
        layeredInput.map(CAM_FOCUS, Button.MOUSE_LEFT);
        layeredInput.map(CAM_MOVE, Input.Keys.KEY_W);
        layeredInput.map(CAM_MOVE, -1, Input.Keys.KEY_S);
        layeredInput.map(CAM_STRAFE, -1, Input.Keys.KEY_D);
        layeredInput.map(CAM_STRAFE, Input.Keys.KEY_A);
        layeredInput.map(CAM_ELEVATE, Input.Keys.KEY_SPACE);
        layeredInput.map(CAM_ELEVATE, -1, Input.Keys.KEY_LEFT_SHIFT);
        layeredInput.map(BASE_LEVEL, Input.Keys.KEY_RIGHT);
        layeredInput.map(BASE_LEVEL, -1, Input.Keys.KEY_LEFT);
        layeredInput.map(ENABLE_MIPMAPPING, Input.Keys.KEY_N);

        // Listen to the input layers to be able to receive state and value update.
        layeredInput.listenValue(this, CAM_LOOK_X, CAM_LOOK_Y, CAM_MOVE, CAM_STRAFE, CAM_ELEVATE);
        layeredInput.listenState(this, CAM_FOCUS, ENABLE_MIPMAPPING, BASE_LEVEL);

        // Load KTX 2.0 texture and set appropriate filtering mode to visualize the
        // mipmap levels.
        texture = assetManager.loadTexture2D("/textures/ktx2/ktx_app-u.ktx2")
                .setFilter(MinFilter.TRILINEAR, MagFilter.BILINEAR).setWrapMode(WrapMode.REPEAT, WrapMode.REPEAT);

        // Create a quad meshed geometry to visualize the texture.
        var quadMesh = new Quad(2, 2);
        this.quad = new PhysicaMundi("ktx_app-u", quadMesh);
        quad.setTranslation(0, 0, -2.2f);

        // Select the third material which is "Unlit" to render the quad using
        // the loaded texture with no fog.
        Material[] materials = assetManager.loadMaterial("/materials/unlit.json");
        var material = materials[2];
        material.getFirstShader();
        quad.setMaterial(material);
        quad.getMaterial().addVariable("texture_sampler", texture);

        // Disable face-culling to view the quad backface.
        scene.setRenderStates(new FaceCullingState().disable());

        // Attach the quad to the scene root.
        scene.attach(quad);
    }

    @Override
    @OpenGLCall
    protected void update(ReadableTimer timer) {
        // Update the cam control.
        camControl.update(timer);

        // Update the angle using the current tpf to rotate at a constant speed.
        angle += timer.getTimePerFrame() * 50;
        // Wrap the angle to keep it inside 0-360 range.
        angle %= 360;

        // Update the quad rotation with the angle value in radians.
        quad.setRotation(0, angle * MercuryMath.DEG_TO_RAD, 0);
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

        if (layer == CAM_MOVE) {
            camControl.move(value, 0, 0);
        }

        if (layer == CAM_STRAFE) {
            camControl.move(0, 0, value);
        }

        if (layer == CAM_ELEVATE) {
            camControl.move(0, value, 0);
        }
    }

    @Override
    public void trigger(InputLayer layer, InputState state) {
        var input = getService(DelegateInputProcessor.class);

        if (layer == CAM_FOCUS) {
            switch (state) {
            case OFF:
                input.setCursorVisible(true);
                break;
            case POSITIVE:
            case NEGATIVE:
                input.setCursorVisible(false);
                break;
            default:
                break;
            }
        }

        if (layer == ENABLE_MIPMAPPING) {
            switch (state) {
            case POSITIVE:
                var max = mipmappingEnabled ? 0 : -1;
                texture.setMaxLevel(max);
                this.mipmappingEnabled = !mipmappingEnabled;
                logger.info("Mipmapping " + (mipmappingEnabled ? "enabled" : "disabled"));
                break;
            default:
                break;
            }
        }

        if (layer == BASE_LEVEL) {
            switch (state) {
            case POSITIVE:
                incrementMipmapLevel(1);
                break;
            case NEGATIVE:
                incrementMipmapLevel(-1);
                break;
            default:
                break;
            }
        }
    }

    /**
     * Increment the mipmap base level by the given value.
     * 
     * @param inc The value to increment the base level by.
     */
    private void incrementMipmapLevel(int inc) {
        this.currentBaseLevel = (int) (currentBaseLevel + inc);
        if (currentBaseLevel > texture.getImage().mipmapsCount() - 1) {
            currentBaseLevel = 0;
        } else if (currentBaseLevel < 0) {
            currentBaseLevel = texture.getImage().mipmapsCount() - 1;
        }

        texture.setBaseLevel(currentBaseLevel);
        logger.info("Forcing mipmap base level to " + currentBaseLevel);
    }
}
