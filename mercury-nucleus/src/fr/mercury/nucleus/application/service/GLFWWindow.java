package fr.mercury.nucleus.application.service;

import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_SRGB_CAPABLE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_REFRESH_RATE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;

import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.Application;
import fr.mercury.nucleus.application.MercuryContext;
import fr.mercury.nucleus.application.MercurySettings;
import fr.mercury.nucleus.asset.AssetManager;
import fr.mercury.nucleus.asset.loader.image.STBImageReader;
import fr.mercury.nucleus.texture.Image;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;
import fr.mercury.nucleus.utils.data.Allocator;

/**
 * <code>GLFWWindow</code> is an implementation of {@link Window} which extends
 * {@link AbstractApplicationService}. It uses the GLFW bindings provided by
 * LWJGL3 to create a window based on an OpenGL context.
 * 
 * @author GnosticOccultist
 */
public class GLFWWindow extends AbstractApplicationService implements Window {

    /**
     * The application logger.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.glfw");

    /**
     * The window handle value.
     */
    private long window = NULL;
    /**
     * Whether the context window is focused or maximized/minimized.
     */
    private boolean focused;
    /**
     * The previous time for fps computation.
     */
    private Long previous = null;
    /**
     * The frame count for fps computation.
     */
    private int frameCount;

    @Override
    @OpenGLCall
    public void initialize(MercurySettings settings) {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(
                (error, description) -> logger.error("An GLFW error has occured '" + error + "': " + description));

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW context!");
        }

        // Optional, the current window hints are already the default.
        glfwDefaultWindowHints();

        // Choose the OpenGL API for the client.
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
        // The window will stay hidden after creation.
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        // Whether the window is going to be resizable or not based on the configs.
        glfwWindowHint(GLFW_RESIZABLE, settings.isResizable() ? GL_TRUE : GL_FALSE);
        // Set the refresh rate of the window (frequency).
        glfwWindowHint(GLFW_REFRESH_RATE, settings.getFrequency());
        // Sets the number of samples for the window framebuffer.
        glfwWindowHint(GLFW_SAMPLES, settings.getSamples());
        // Sets the sRGB for the window framebuffer.
        glfwWindowHint(GLFW_SRGB_CAPABLE, settings.isGammaCorrection() ? GL_TRUE : GL_FALSE);

        if (settings.isGraphicsDebugOutput()) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GL_TRUE);
        }

        var version = settings.getGraphicsAPI();
        var hints = getVersionHints(version);
        // No found any matching version so default to the minimum required.
        if (hints[0] == 0) {
            version = settings.getMinGraphicsVersion();
            hints = getVersionHints(version);
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, hints[0]);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, hints[1]);

        // This allow to use OpenGL 3.x and 4.x contexts on OSX.
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        long monitor = NULL;

        if (settings.isFullscreen()) {
            monitor = glfwGetPrimaryMonitor();
        }

        // Getting the resolution of the primary monitor.
        final GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        // Make sure the width and the height is superior than 0.
        if (settings.getWidth() <= 0 || settings.getHeight() <= 0) {
            settings.setResolution(videoMode.width(), videoMode.height());
        }

        // Create the window.
        window = glfwCreateWindow(settings.getWidth(), settings.getHeight(), settings.getTitle(), monitor, NULL);

        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup window size callback to update framebuffers resolutions, view or
        // projection matrix.
        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            /*
             * As this is the window size, we don't delegate to the application as OpenGL
             * uses pixel coordinates (for example in glViewport) rather than screen
             * coordinates as this callback does.
             */
            settings.setResolution(width, height);
        });
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            Allocator.stackSafe(stack -> {
                /*
                 * The window size might also be changed, but sometimes window callback won't
                 * trigger. Probably a LWJGL3 bug or graphics driver related, anyway make sure
                 * the new resolution is set.
                 */
                IntBuffer windowWidth = stack.mallocInt(1);
                IntBuffer windowHeight = stack.mallocInt(1);
                glfwGetWindowSize(window, windowWidth, windowHeight);

                settings.setResolution(windowWidth.get(), windowHeight.get());
                application.resize(width, height);
            });
        });

        // Setup window focus callback to stop updating or rendering when minimized.
        glfwSetWindowFocusCallback(window, (window, focus) -> {
            if (focused != focus) {
                if (!focused) {
                    application.gainFocus();
                } else {
                    application.looseFocus();
                }
                focused = !focused;
            }
        });

        // Center the window.
        if (!settings.isFullscreen()) {
            moveWindow((videoMode.width() - settings.getWidth()) / 2, (videoMode.height() - settings.getHeight()) / 2);
        }

        var iconPath = settings.getString("Icons");
        // TODO: Support config to read and convert image into a given format.
        var icon = application.getService(AssetManager.class).loadImage(iconPath, STBImageReader.DESCRIPTOR);
        setIcon(icon);

        logger.info("Created LWJGL context " + Version.getVersion() + " and running on thread "
                + Thread.currentThread().getName() + "\n* Graphics Adapter: GLFW " + GLFW.glfwGetVersionString());

        super.initialize(settings);
    }

    /**
     * Return the version hints as an array of two integer, the first one being the
     * major and second the minor.
     * 
     * @param version The version string representation.
     * @return An array of two version integer (not null).
     */
    private int[] getVersionHints(String version) {
        var result = new int[2];
        switch (version) {
        case MercurySettings.OPENGL_32:
            result[0] = 3;
            result[1] = 2;
            break;
        case MercurySettings.OPENGL_33:
            result[0] = 3;
            result[1] = 3;
            break;
        case MercurySettings.OPENGL_40:
            result[0] = 4;
            result[1] = 0;
            break;
        case MercurySettings.OPENGL_41:
            result[0] = 4;
            result[1] = 1;
            break;
        case MercurySettings.OPENGL_42:
            result[0] = 4;
            result[1] = 2;
            break;
        case MercurySettings.OPENGL_43:
            result[0] = 4;
            result[1] = 3;
            break;
        case MercurySettings.OPENGL_44:
            result[0] = 4;
            result[1] = 4;
            break;
        case MercurySettings.OPENGL_45:
            result[0] = 4;
            result[1] = 5;
            break;
        case MercurySettings.OPENGL_46:
            result[0] = 4;
            result[1] = 6;
            break;
        }
        return result;
    }

    @Override
    @OpenGLCall
    public void update(ReadableTimer timer) {
        super.update(timer);
    }

    /**
     * Sets the position of the <code>GLFWWindow</code> upper left corner area to
     * the given screen coordinates.
     * <p>
     * The method can only be called if the window isn't in fullscreen mode and in
     * the main {@link Thread}.
     * 
     * @param x The X coordinate of the upper-left corner area in screen coordinates
     *          (&ge;0).
     * @param y The Y coordinate of the upper-left corner area in screen coordinates
     *          (&ge;0).
     */
    @OpenGLCall
    public void moveWindow(int x, int y) {
        Validator.nonNegative(x, "The X coordinate of the window can't be negative!");
        Validator.nonNegative(y, "The Y coordinate of the window can't be negative!");

        MercuryContext.checkMainThread();
        assert getContext().getType().isRenderable();

        if (getContext().getType().canShowWindow()) {
            assert window != NULL;
            glfwSetWindowPos(window, x, y);
        }
    }

    /**
     * Set the title of the <code>GLFWWindow</code>. Note that it is only visible in
     * windowed mode.
     * <p>
     * The method can only be called if the window isn't in fullscreen mode and in
     * the main {@link Thread}.
     * 
     * @param title The title of the window.
     */
    @OpenGLCall
    public void setTitle(String title) {
        MercuryContext.checkMainThread();
        assert getContext().getType().isRenderable();

        if (getContext().getType().canShowWindow()) {
            assert window != NULL;
            glfwSetWindowTitle(window, title);
        }
    }

    /**
     * Sets the icon of the <code>GLFWWindow</code> to the provided {@link Image}.
     * 
     * @param image The image to use as an icon for the window (not null).
     */
    @OpenGLCall
    public void setIcon(Image image) {
        Validator.nonNull(image, "The image icon can't be null!");

        var iconData = image.getData();
        iconData.rewind();

        var img = GLFWImage.malloc();
        var imageBuff = GLFWImage.malloc(1);

        img.set(image.getWidth(), image.getHeight(), iconData);
        imageBuff.put(0, img);
        glfwSetWindowIcon(window, imageBuff);
        img.free();
        imageBuff.free();
    }

    /**
     * Return the window width in screen coordinates.
     * 
     * @return The window width.
     */
    public int getWidth() {
        return application.getSettings().getWidth();
    }

    /**
     * Return the window height in screen coordinates.
     * 
     * @return The window height.
     */
    public int getHeight() {
        return application.getSettings().getHeight();
    }

    /**
     * Return the aspect ratio of the window framebuffer.
     * 
     * @return The width divided by the height (&gt;0).
     */
    public float getAspect() {
        var result = (float) getWidth() / (float) getHeight();
        return result;
    }

    /**
     * Return the identifier of the <code>GLFWWindow</code>.
     * 
     * @return The native identifier of the window.
     */
    @Override
    public long getID() {
        return window;
    }

    /**
     * Show the <code>GLFWWindow</code> on screen by making it visible.
     */
    @Override
    @OpenGLCall
    public void show() {
        assert getContext().getType().isRenderable();

        if (getContext().getType().canShowWindow()) {
            assert window != NULL;
            glfwShowWindow(window);
        }
    }

    /**
     * Return whether the <code>GLFWWindow</code> should close itself.
     * 
     * @return Whether the window should close.
     */
    @Override
    public boolean shouldClose() {
        assert window != NULL;
        return glfwWindowShouldClose(window);
    }

    /**
     * Make the OpenGL context of the <code>GLFWWindow</code> current on the calling
     * {@link Thread}.
     */
    @Override
    public void makeContextCurrent() {
        assert window != NULL;
        assert getContext().getType().isRenderable();

        glfwMakeContextCurrent(window);
    }

    /**
     * Set whether the <code>GLFWWindow</code> should use vertical synchronization
     * by changing the swap interval of the buffers to 1.
     * 
     * @param vSync Whether to use V-Sync for the window.
     */
    @Override
    @OpenGLCall
    public void useVSync(boolean vSync) {
        assert getContext().getType().isRenderable();
        glfwSwapInterval(vSync ? 1 : 0);
    }

    /**
     * Finish the frame when rendering has been done on the back buffer of the
     * <code>GLFWWindow</code>. Also process all pending GLFW events causing
     * associated callbacks to be called.
     * <p>
     * If {@link MercurySettings#getBoolean(ShowFPS)} is enabled, then the frame per
     * second and time per frame statistics will be updated and displayed on the
     * <code>GLFWWindow</code>'s title bar.
     */
    @Override
    @OpenGLCall
    public void finishFrame() {

        var settings = application.getSettings();
        if (settings.getBoolean("ShowFPS")) {
            updateFrameTime(settings);
        }

        assert window != NULL;
        assert getContext().getType().isRenderable();

        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    /**
     * Update the frame per second and time per frame statistics and display them to
     * the <code>GLFWWindow</code>'s title bar.
     * 
     * @param settings The application settings (not null).
     */
    private void updateFrameTime(MercurySettings settings) {
        var now = System.nanoTime();
        if (previous == null) {
            // First update.
            previous = now;

        } else {
            ++frameCount;
            var nanoseconds = now - previous;
            var milliseconds = 1e-6 * (double) nanoseconds;
            if (milliseconds > 200.0) {
                // Every 200 ms, update the fps and tpf statistics in window's title.
                var fps = (int) Math.round(1000. * frameCount / milliseconds);
                var tpf = milliseconds / frameCount;
                var windowTitle = String.format("%s  |  %d FPS %.2f ms", settings.getTitle(), fps, tpf);
                setTitle(windowTitle);

                frameCount = 0;
                previous = now;
            }
        }
    }

    /**
     * Destroy the <code>GLFWWindow</code> and its associated context when the
     * {@link Application} is closing or restarting.
     * <p>
     * The method also restores any modified gamma ramps and frees any other
     * allocated resources.
     */
    @Override
    @OpenGLCall
    public void destroy() {
        try {
            if (window != NULL) {
                Callbacks.glfwFreeCallbacks(window);
                glfwDestroyWindow(window);
                window = NULL;
            }
        } catch (Exception ex) {
            logger.error("Failed to destroy window!", ex);
        }

        // Terminate GLFW, even if we have no window created.
        glfwTerminate();

        cleanup();
    }
}
