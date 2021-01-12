package fr.mercury.nucleus.application;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import fr.alchemy.utilities.Instantiator;
import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.service.ApplicationService;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.input.InputProcessor;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.renderer.DefaultRenderer;
import fr.mercury.nucleus.renderer.Renderer;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.NucleusMundi;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;
import fr.mercury.nucleus.utils.SpeedableNanoTimer;
import fr.mercury.nucleus.utils.Timer;
import fr.mercury.nucleus.utils.data.Allocator;
import fr.mercury.nucleus.utils.gc.NativeObjectCleaner;

/**
 * <code>MercuryApplication</code> is an abstract implementation of a usable {@link Application} using the <code>Mercury-Engine</code>.
 * It implements the initialization, updating and cleaning methods as well as managing the {@link MercurySettings} and a set of {@link ApplicationModule}
 * extending its basic capabilities depending on the user needs.
 * <p>
 * Such application is capable of rendering a 3D scene described by a hierarchy of {@link AnimaMundi} extending from the {@link #scene root-node}
 * using its own {@link Camera} and {@link DefaultRenderer}.
 * <p>
 * An {@link AssetManager} and an {@link InputProcessor} are contained within the application in order to handle asset loading and being 
 * notified about inputs related events.
 * 
 * @see #getScene()
 * @see #getService(Class)
 * 
 * @author GnosticOccultist
 */
public abstract class MercuryApplication implements Application {

    /**
     * The logger for the application.
     */
    protected static final Logger logger = FactoryLogger.getLogger("mercury.app");

    /**
     * The current context of the application.
     */
    protected MercuryContext context;
    /**
     * The settings of the application.
     */
    protected MercurySettings settings;
    /**
     * The set of services linked to the application.
     */
    protected final Set<ApplicationService> services = new HashSet<>();
    /**
     * The timer of the application in nanoseconds.
     */
    protected Timer timer = new SpeedableNanoTimer();
    /**
     * The camera or null if doesn't support rendering.
     */
    protected Camera camera;
    /**
     * The renderer or null if doesn't support rendering.
     */
    protected Renderer renderer;
    /**
     * The root node for the scene or null if doesn't support rendering.
     */
    protected NucleusMundi scene = new NucleusMundi("root-nucleus");
    /**
     * The asset manager, will also be added to the services set.
     */
    protected AssetManager assetManager = new AssetManager();

    /**
     * Starts the <code>MercuryApplication</code> and creates the {@link MercuryContext}. 
     * While initializing the context, it will start the main application loop and show the configured window.
     * <p>
     * If no {@link MercurySettings} are set, it will use the default ones.
     */
    public void start() {
        if (settings == null) {
            settings = new MercurySettings(true);
        }

        // We need the asset manager before initialization for the icons.
        linkService(assetManager);

        logger.info("Starting the application: " + getClass().getSimpleName());

        context = MercuryContext.newContext(this, settings);
        context.initialize();
    }

    /**
     * <b>Don't call manually</b>
     * <p>
     * It is automatically called when the context is initialized.
     */
    @Override
    @OpenGLCall
    public void internalInitialize() {

        var renderable = context.getType().isRenderable();
        if (renderable) {

            // Initialize the camera.
            camera = new Camera(settings.getWidth(), settings.getHeight());
            camera.setLocation(0f, 0f, 0f);
            camera.setFrustumPerspective(45F, (float) camera.getWidth() / camera.getHeight(), 1f, 1000f);

            // Try initializing the renderer from settings.
            var type = settings.getRendererType();

            try {
                this.renderer = Instantiator.fromNameImplements(type, Renderer.class, null, camera);
            } catch (Exception ex) {
                logger.error("Unable to instantiate Renderer implementation from class '" + type
                        + "'! Switching to DefaultRenderer instead.", ex);

                this.renderer = new DefaultRenderer(camera);
            }

            linkService(renderer);
        }

        // Reset the timer before invoking anything else,
        // to ensure the first time per frame isn't too large...
        timer.reset();

        // Initialize application's services.
        services.stream().filter(module -> !module.isInitialized()).forEach(module -> module.initialize(settings));

        // Initialize the implementation.
        initialize();
    }

    /**
     * <b>Don't call manually</b>
     * <p>
     * It is automatically called internally in {@link #internalInitialize()}.
     * <p>
     * Initialize the implementation of <code>MercuryApplication</code>.
     * 
     * @see #internalInitialize()
     */
    @OpenGLCall
    protected abstract void initialize();

    @Override
    @OpenGLCall
    public void resize(int width, int height) {
        if (renderer != null) {
            renderer.resize(width, height);
        }
    }

    /**
     * <b>Don't call manually</b>
     * <p>
     * It is automatically called during the context updating logic.
     */
    @Override
    @OpenGLCall
    public void internalUpdate() {
        // Timer is paused, no need to continue...
        if (timer.isPaused()) {
            return;
        }

        timer.update();

        // Initialize services which haven't already.
        services.stream().filter(module -> !module.isInitialized()).forEach(module -> module.initialize(settings));
        // Update application's services.
        services.stream().forEach(module -> module.update(timer));

        // Update the implementation.
        update(timer);

        if (renderer != null) {
            // Update the geometric information of the scene and its hierarchy.
            scene.updateGeometricState(timer);

            // Perform rendering of the scene.
            renderer.renderScene(scene);
        }
    }

    /**
     * Performs some actions with the <code>MercuryApplication</code> once the front
     * and back buffer have been swapped, meaning the rendered frame is visible on
     * the window.
     */
    @Override
    @OpenGLCall
    public void postFrame() {
        var count = Allocator.stackFrameIndex();
        if (count > 0) {
            logger.debug(count + " pushed stack on the current frame. Consider " + "popping them when no longer used!");
        }

        NativeObjectCleaner.cleanUnused();
    }

    /**
     * <b>Don't call manually</b>
     * <p>
     * It is automatically called internally in {@link #internalUpdate()}.
     * <p>
     * Update the implementation of <code>MercuryApplication</code>.
     * 
     * @param timer The timer used by the application (not null).
     * 
     * @see #internalUpdate()
     */
    @OpenGLCall
    protected void update(ReadableTimer timer) {
    }

    @Override
    public void gainFocus() {
        timer.reset();
    }

    /**
     * <b>Don't call manually</b>
     * <p>
     * It is automatically called when closing the application, before the context
     * destruction.
     */
    @Override
    @OpenGLCall
    public void cleanup() {

        services.forEach(ApplicationService::cleanup);
        services.clear();

        timer.reset();
        NativeObjectCleaner.cleanAll();

        logger.info("Closing the application: " + getClass().getSimpleName());
    }

    /**
     * Restart the <code>MercuryApplication</code>, applying the new
     * {@link MercurySettings} to the {@link MercuryContext} and restarting it.
     */
    @Override
    public void restart() {
        NativeObjectCleaner.reset();

        context.setSettings(settings);
        context.restart();

        NativeObjectCleaner.restart();
    }

    /**
     * Return an optional value of an {@link ApplicationModule} matching the provided type linked 
     * to the <code>MercuryApplication</code>.
     * <p>
     * This function is supposed to be used to access the module, however it shouldn't be used to detach 
     * it from the application, use {@link #unlinkService(ApplicationModule)} instead.
     * 
     * @param type The type of module to return.
     * @return     An optional value containing either a module matching the given type,
     *             or nothing if none is linked to the application.
     * 
     * @see #getService(Class)
     */
    public <M extends AbstractApplicationService> Optional<M> getOptionalModule(Class<M> type) {
        return Optional.ofNullable(getService(type));
    }

    /**
     * Return an {@link ApplicationModule} matching the provided type linked to the <code>MercuryApplication</code>.
     * <p>
     * This function is supposed to be used to access the module, however it shouldn't be used to detach it 
     * from the application, use {@link #unlinkService(ApplicationModule)} instead.
     * 
     * @param type The type of module to return (not null).
     * @return     A module matching the given type, or null if none is linked to the
     *             application.
     * 
     * @see #unlinkService(ApplicationModule)
     * @see #getOptionalModule(Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <M extends ApplicationService> M getService(Class<M> type) {
        Validator.nonNull(type, "The module's type can't be null!");
        for (ApplicationService module : services) {
            if (module.getClass().isAssignableFrom(type)) {
                return (M) module;
            }
        }
        return null;
    }

    /**
     * Links the provided {@link ApplicationModule} to the <code>MercuryApplication</code>. 
     * Note that the module will be initialized during the next update cycle if it hasn't been yet.
     * 
     * @param module The module to be linked (not null).
     */
    @Override
    public void linkService(ApplicationService module) {
        Validator.nonNull(module, "The module can't be null!");
        this.services.add(module);
        module.setApplication(this);
    }

    /**
     * Unlinks the provided {@link ApplicationModule} from the <code>MercuryApplication</code> 
     * and terminate the module by calling the {@link ApplicationModule#cleanup()} method.
     * 
     * @param module The module which is to be cleaned up and removed (not null).
     */
    public void unlinkService(ApplicationService module) {
        Validator.nonNull(module, "The module can't be null!");
        if (services.remove(module)) {
            module.cleanup();
            module.setApplication(null);
        }
    }

    /**
     * Return the {@link NucleusMundi} representing the root-node of the scene meaning all scenegraph 
     * elements are expanding down from this one.
     * <p>
     * In order to attach an {@link AnimaMundi} to this one, call the method {@link NucleusMundi#attach(AnimaMundi)}.
     * 
     * @return The root-node of the scene.
     */
    public NucleusMundi getScene() {
        return scene;
    }

    /**
     * Return the {@link MercurySettings} of the <code>Application</code>.
     * 
     * @return The settings used to create the context (not null).
     */
    @Override
    public MercurySettings getSettings() {
        return settings;
    }

    /**
     * Set the {@link MercurySettings} for the <code>Application</code>.
     * <p>
     * You can change the display settings when the application is running but in
     * order to apply the changes you will need to call {@link #restart()}.
     * 
     * @param settings The new settings to apply.
     */
    public void setSettings(MercurySettings settings) {
        this.settings = settings;
    }
}
