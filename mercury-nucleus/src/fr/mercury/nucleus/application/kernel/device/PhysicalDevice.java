package fr.mercury.nucleus.application.kernel.device;

import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Callback;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.MercurySettings;
import fr.mercury.nucleus.application.service.AbstractApplicationService;
import fr.mercury.nucleus.renderer.opengl.OpenGLDebugOutputCallback;

public class PhysicalDevice extends AbstractApplicationService {

    /**
     * The application logger.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.app");

    /**
     * The extension for OpenGL debug output callback.
     */
    public static final String GL_DEBUG_OUTPUT_EXT = "GL_ARB_debug_output";

    /**
     * The company responsible for the graphics API implementation.
     */
    private final Vendor vendor;
    /**
     * The name of the rendering device.
     */
    private final String deviceName;
    /**
     * The current version of the graphics API.
     */
    private final String version;
    /**
     * The current version of the shading language.
     */
    private final String shadingLangVersion;
    /**
     * The array of available extensions.
     */
    private final String[] extensions;
    /**
     * The OpenGL context capabilities.
     */
    private final GLCapabilities capabilities;

    public PhysicalDevice(Vendor vendor, String deviceName, String version, String shadingLangVersion,
            String[] extensions, GLCapabilities capabilities) {
        this.vendor = vendor;
        this.deviceName = deviceName;
        this.version = version;
        this.shadingLangVersion = shadingLangVersion;
        this.extensions = extensions;
        this.capabilities = capabilities;
    }

    public void check(MercurySettings settings) {
        String[] requiredExtensions = settings.getRequiredExtensions();
        if (requiredExtensions == null || requiredExtensions.length == 0) {
            return;
        }

        List<String> required = Arrays.asList(requiredExtensions);
        boolean hasRequired = Arrays.asList(extensions).containsAll(required);
        if (!hasRequired) {
            required.removeAll(Arrays.asList(extensions));
            JOptionPane.showMessageDialog(null,
                    "Unfortunately your video card doesn't support the required OpenGL extensions "
                            + Arrays.toString(required.toArray()) + "! \n\n" + "To run correctly " + settings.getTitle()
                            + " you may need to upgrade your card, or select an "
                            + "appropriate video card if your laptop has switchable graphics. Try also to update your drivers!",
                    "Unsupported video card configuration", JOptionPane.ERROR_MESSAGE);

            System.exit(0);
        }

        logger.debug("The used device '" + deviceName
                + "' has all required extensions for the application to run correctly: \n " + "* REQUIRED_EXTENSIONS = "
                + Arrays.toString(requiredExtensions));
    }

    public Vendor getVendor() {
        return vendor;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getVersion() {
        return version;
    }

    public String getShadingLangVersion() {
        return shadingLangVersion;
    }

    private String versionOnly() {
        return version.split(" ")[0];
    }

    public boolean supportsGraphicsDebug() {
        return capabilities.OpenGL43 || capabilities.GL_KHR_debug || capabilities.GL_ARB_debug_output
                || capabilities.GL_AMD_debug_output;
    }

    public Callback setupGraphicsDebugMessageCallback() {
        return OpenGLDebugOutputCallback.create(capabilities);
    }

    public boolean hasExtension(String extension) {
        return Arrays.asList(extensions).contains(extension);
    }

    public int majorVersion() {
        var versionOnly = versionOnly();
        return Integer.parseInt(versionOnly.split("\\.")[0]);
    }

    public int minorVersion() {
        var versionOnly = versionOnly();
        return Integer.parseInt(versionOnly.split("\\.")[1]);
    }

    public int releaseNumberVersion() {
        var versionOnly = versionOnly();
        var splitVersion = versionOnly.split("\\.");
        if (splitVersion.length > 2) {
            return Integer.parseInt(splitVersion[2]);
        }
        return 0;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public GLCapabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public String toString() {
        return "* VENDOR = " + vendor + "\n" + "* DEVICE_NAME = " + deviceName + "\n" + "* GRAPHICS_API_VERSION = "
                + version + "\n" + "* SHADING_LANGUAGE_VERSION = " + shadingLangVersion + "\n" + "* EXTENSIONS_COUNT = "
                + extensions.length;
    }
}
