package fr.mercury.nucleus.renderer.opengl;

import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GLDebugMessageARBCallbackI;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;

/**
 * <code>OpenGLDebugOutputCallback</code> is an implementation of {@link GLDebugMessageARBCallbackI} to pretty-print
 * OpenGL debug messages.
 * 
 * @author GnosticOccultist
 */
public class OpenGLDebugOutputCallback implements GLDebugMessageARBCallbackI {

    /**
     * The OpenGL debug logger.
     */
    private static final Logger logger = FactoryLogger.getLogger("mercury.opengl.debug");

    @Override
    public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
        var sb = new StringBuilder();
        sb.append("GL:");

        switch (source) {
        case ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB:
            sb.append("API");
            break;
        case ARBDebugOutput.GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB:
            sb.append("WINDOW SYSTEM");
            break;
        case ARBDebugOutput.GL_DEBUG_SOURCE_SHADER_COMPILER_ARB:
            sb.append("SHADER COMPILER");
            break;
        case ARBDebugOutput.GL_DEBUG_SOURCE_THIRD_PARTY_ARB:
            sb.append("THIRD PARTY");
            break;
        case ARBDebugOutput.GL_DEBUG_SOURCE_APPLICATION_ARB:
            sb.append("APPLICATION");
            break;
        case ARBDebugOutput.GL_DEBUG_SOURCE_OTHER_ARB:
            sb.append("OTHER");
            break;
        default:
            // Unknown source.
            sb.append("Unknown (0x" + Integer.toHexString(source).toUpperCase() + ")");
        }

        sb.append(":");

        switch (type) {
        case ARBDebugOutput.GL_DEBUG_TYPE_ERROR_ARB:
            sb.append("ERROR");
            break;
        case ARBDebugOutput.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB:
            sb.append("DEPRECATED BEHAVIOR");
            break;
        case ARBDebugOutput.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB:
            sb.append("UNDEFINED BEHAVIOR");
            break;
        case ARBDebugOutput.GL_DEBUG_TYPE_PORTABILITY_ARB:
            sb.append("PORTABILITY");
            break;
        case ARBDebugOutput.GL_DEBUG_TYPE_PERFORMANCE_ARB:
            sb.append("PERFORMANCE");
            break;
        case ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB:
            sb.append("OTHER");
            break;
        default:
            // Unknown type.
            sb.append("Unknown (0x" + Integer.toHexString(type).toUpperCase() + ")");
        }

        sb.append(":");

        switch (severity) {
        case ARBDebugOutput.GL_DEBUG_SEVERITY_HIGH_ARB:
            sb.append("HIGH");
            break;
        case ARBDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_ARB:
            sb.append("MEDIUM");
            break;
        case ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB:
            sb.append("LOW");
            break;
        default:
            // Unknown source.
            sb.append("Unknown (0x" + Integer.toHexString(severity).toUpperCase() + ")");
        }

        var mess = MemoryUtil.memUTF8(message);
        sb.append(mess);
        
        if (type == ARBDebugOutput.GL_DEBUG_TYPE_ERROR_ARB) {
            logger.error(sb.toString());
        } else {
            logger.info(sb.toString());
        }
    }
}
