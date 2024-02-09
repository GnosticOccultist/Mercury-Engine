package fr.mercury.nucleus.renderer.opengl;

import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_CATEGORY_API_ERROR_AMD;
import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_CATEGORY_APPLICATION_AMD;
import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_CATEGORY_DEPRECATION_AMD;
import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_CATEGORY_OTHER_AMD;
import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_CATEGORY_PERFORMANCE_AMD;
import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_CATEGORY_SHADER_COMPILER_AMD;
import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_CATEGORY_UNDEFINED_BEHAVIOR_AMD;
import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_CATEGORY_WINDOW_SYSTEM_AMD;
import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_SEVERITY_HIGH_AMD;
import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_SEVERITY_LOW_AMD;
import static org.lwjgl.opengl.AMDDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_AMD;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SEVERITY_HIGH_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SOURCE_APPLICATION_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SOURCE_OTHER_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SOURCE_SHADER_COMPILER_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SOURCE_THIRD_PARTY_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_TYPE_ERROR_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_TYPE_PERFORMANCE_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_TYPE_PORTABILITY_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SEVERITY_HIGH;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SEVERITY_LOW;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SEVERITY_MEDIUM;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SEVERITY_NOTIFICATION;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SOURCE_API;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SOURCE_APPLICATION;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SOURCE_OTHER;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SOURCE_SHADER_COMPILER;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SOURCE_THIRD_PARTY;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SOURCE_WINDOW_SYSTEM;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_ERROR;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_MARKER;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_OTHER;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_PERFORMANCE;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_PORTABILITY;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR;
import static org.lwjgl.system.APIUtil.apiUnknownToken;

import org.lwjgl.opengl.AMDDebugOutput;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageAMDCallback;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.alchemy.utilities.logging.LoggerLevel;

/**
 * <code>OpenGLDebugOutputCallback</code> is used to pretty-print OpenGL debug
 * messages using the appropriate extension based on context capabilities.
 * 
 * @author GnosticOccultist
 */
public class OpenGLDebugOutputCallback {

    /**
     * The OpenGL debug logger.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.opengl.debug");

    /**
     * Inhibit instantiation of <code>OpenGLDebugOutputCallback</code>.
     */
    private OpenGLDebugOutputCallback() {
        super();
    }

    public static Callback create(GLCapabilities capabilities) {
        logger.setActive(LoggerLevel.DEBUG, true);

        if (capabilities.OpenGL43) {
            logger.debug("Using OpenGL 4.3 for error logging.");
            var proc = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
                logger.debug("OpenGL debug message");
                logger.debug(String.format("\t%s: %s\n", "ID", String.format("0x%X", id)));
                logger.debug(String.format("\t%s: %s\n", "Source", getDebugSource(source)));
                logger.debug(String.format("\t%s: %s\n", "Type", getDebugType(type)));
                logger.debug(String.format("\t%s: %s\n", "Severity", getDebugSeverity(severity)));
                logger.debug(
                        String.format("\t%s: %s\n", "Message", GLDebugMessageCallback.getMessage(length, message)));
            });
            GL43C.glDebugMessageCallback(proc, MemoryUtil.NULL);
            if ((GL11C.glGetInteger(GL30C.GL_CONTEXT_FLAGS) & GL43C.GL_CONTEXT_FLAG_DEBUG_BIT) == 0) {
                logger.warning("A non-debug context may not produce any debug output.");
                logger.warning("Enable debug output on context");
                GL11C.glEnable(GL43C.GL_DEBUG_OUTPUT);
            }
            return proc;
        }

        if (capabilities.GL_KHR_debug) {
            logger.debug("Using KHR_debug for error logging.");
            var proc = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
                logger.debug("OpenGL debug message");
                logger.debug(String.format("\t%s: %s\n", "ID", String.format("0x%X", id)));
                logger.debug(String.format("\t%s: %s\n", "Source", getDebugSource(source)));
                logger.debug(String.format("\t%s: %s\n", "Type", getDebugType(type)));
                logger.debug(String.format("\t%s: %s\n", "Severity", getDebugSeverity(severity)));
                logger.debug(
                        String.format("\t%s: %s\n", "Message", GLDebugMessageCallback.getMessage(length, message)));
            });
            KHRDebug.glDebugMessageCallback(proc, MemoryUtil.NULL);
            if (capabilities.OpenGL30
                    && (GL11C.glGetInteger(GL30C.GL_CONTEXT_FLAGS) & GL43C.GL_CONTEXT_FLAG_DEBUG_BIT) == 0) {
                logger.warning("A non-debug context may not produce any debug output.");
                logger.warning("Enable debug output on context");
                GL11C.glEnable(GL43C.GL_DEBUG_OUTPUT);
            }
            return proc;
        }

        if (capabilities.GL_ARB_debug_output) {
            logger.debug("Using ARB_debug_output for error logging.");
            var proc = GLDebugMessageARBCallback.create((source, type, id, severity, length, message, userParam) -> {
                logger.debug("ARB_debug_output message");
                logger.debug(String.format("\t%s: %s\n", "ID", String.format("0x%X", id)));
                logger.debug(String.format("\t%s: %s\n", "Source", getSourceARB(source)));
                logger.debug(String.format("\t%s: %s\n", "Type", getTypeARB(type)));
                logger.debug(String.format("\t%s: %s\n", "Severity", getSeverityARB(severity)));
                logger.debug(
                        String.format("\t%s: %s\n", "Message", GLDebugMessageARBCallback.getMessage(length, message)));
            });
            ARBDebugOutput.glDebugMessageCallbackARB(proc, MemoryUtil.NULL);
            return proc;
        }

        if (capabilities.GL_AMD_debug_output) {
            logger.debug("Using AMD_debug_output for error logging.");
            var proc = GLDebugMessageAMDCallback.create((id, category, severity, length, message, userParam) -> {
                logger.debug("AMD_debug_output message");
                logger.debug(String.format("\t%s: %s\n", "ID", String.format("0x%X", id)));
                logger.debug(String.format("\t%s: %s\n", "Source", getCategoryAMD(category)));
                logger.debug(String.format("\t%s: %s\n", "Severity", getSeverityAMD(severity)));
                logger.debug(
                        String.format("\t%s: %s\n", "Message", GLDebugMessageAMDCallback.getMessage(length, message)));
            });
            AMDDebugOutput.glDebugMessageCallbackAMD(proc, MemoryUtil.NULL);
            return proc;
        }

        logger.warning("No debug output implementation is available.");
        return null;
    }

    private static String getDebugSource(int source) {
        switch (source) {
        case GL_DEBUG_SOURCE_API:
            return "API";
        case GL_DEBUG_SOURCE_WINDOW_SYSTEM:
            return "WINDOW SYSTEM";
        case GL_DEBUG_SOURCE_SHADER_COMPILER:
            return "SHADER COMPILER";
        case GL_DEBUG_SOURCE_THIRD_PARTY:
            return "THIRD PARTY";
        case GL_DEBUG_SOURCE_APPLICATION:
            return "APPLICATION";
        case GL_DEBUG_SOURCE_OTHER:
            return "OTHER";
        default:
            return unknownFlag(source);
        }
    }

    private static String getDebugType(int type) {
        switch (type) {
        case GL_DEBUG_TYPE_ERROR:
            return "ERROR";
        case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
            return "DEPRECATED BEHAVIOR";
        case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
            return "UNDEFINED BEHAVIOR";
        case GL_DEBUG_TYPE_PORTABILITY:
            return "PORTABILITY";
        case GL_DEBUG_TYPE_PERFORMANCE:
            return "PERFORMANCE";
        case GL_DEBUG_TYPE_OTHER:
            return "OTHER";
        case GL_DEBUG_TYPE_MARKER:
            return "MARKER";
        default:
            return apiUnknownToken(type);
        }
    }

    private static String getDebugSeverity(int severity) {
        switch (severity) {
        case GL_DEBUG_SEVERITY_HIGH:
            return "HIGH";
        case GL_DEBUG_SEVERITY_MEDIUM:
            return "MEDIUM";
        case GL_DEBUG_SEVERITY_LOW:
            return "LOW";
        case GL_DEBUG_SEVERITY_NOTIFICATION:
            return "NOTIFICATION";
        default:
            return apiUnknownToken(severity);
        }
    }

    private static String getSourceARB(int source) {
        switch (source) {
        case GL_DEBUG_SOURCE_API_ARB:
            return "API";
        case GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB:
            return "WINDOW SYSTEM";
        case GL_DEBUG_SOURCE_SHADER_COMPILER_ARB:
            return "SHADER COMPILER";
        case GL_DEBUG_SOURCE_THIRD_PARTY_ARB:
            return "THIRD PARTY";
        case GL_DEBUG_SOURCE_APPLICATION_ARB:
            return "APPLICATION";
        case GL_DEBUG_SOURCE_OTHER_ARB:
            return "OTHER";
        default:
            return apiUnknownToken(source);
        }
    }

    private static String getTypeARB(int type) {
        switch (type) {
        case GL_DEBUG_TYPE_ERROR_ARB:
            return "ERROR";
        case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB:
            return "DEPRECATED BEHAVIOR";
        case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB:
            return "UNDEFINED BEHAVIOR";
        case GL_DEBUG_TYPE_PORTABILITY_ARB:
            return "PORTABILITY";
        case GL_DEBUG_TYPE_PERFORMANCE_ARB:
            return "PERFORMANCE";
        case GL_DEBUG_TYPE_OTHER_ARB:
            return "OTHER";
        default:
            return apiUnknownToken(type);
        }
    }

    private static String getSeverityARB(int severity) {
        switch (severity) {
        case GL_DEBUG_SEVERITY_HIGH_ARB:
            return "HIGH";
        case GL_DEBUG_SEVERITY_MEDIUM_ARB:
            return "MEDIUM";
        case GL_DEBUG_SEVERITY_LOW_ARB:
            return "LOW";
        default:
            return apiUnknownToken(severity);
        }
    }

    private static String getCategoryAMD(int category) {
        switch (category) {
        case GL_DEBUG_CATEGORY_API_ERROR_AMD:
            return "API ERROR";
        case GL_DEBUG_CATEGORY_WINDOW_SYSTEM_AMD:
            return "WINDOW SYSTEM";
        case GL_DEBUG_CATEGORY_DEPRECATION_AMD:
            return "DEPRECATION";
        case GL_DEBUG_CATEGORY_UNDEFINED_BEHAVIOR_AMD:
            return "UNDEFINED BEHAVIOR";
        case GL_DEBUG_CATEGORY_PERFORMANCE_AMD:
            return "PERFORMANCE";
        case GL_DEBUG_CATEGORY_SHADER_COMPILER_AMD:
            return "SHADER COMPILER";
        case GL_DEBUG_CATEGORY_APPLICATION_AMD:
            return "APPLICATION";
        case GL_DEBUG_CATEGORY_OTHER_AMD:
            return "OTHER";
        default:
            return apiUnknownToken(category);
        }
    }

    private static String getSeverityAMD(int severity) {
        switch (severity) {
        case GL_DEBUG_SEVERITY_HIGH_AMD:
            return "HIGH";
        case GL_DEBUG_SEVERITY_MEDIUM_AMD:
            return "MEDIUM";
        case GL_DEBUG_SEVERITY_LOW_AMD:
            return "LOW";
        default:
            return apiUnknownToken(severity);
        }
    }

    private static String unknownFlag(int flag) {
        return String.format("%s [0x%X]", "Unknown", flag);
    }
}
