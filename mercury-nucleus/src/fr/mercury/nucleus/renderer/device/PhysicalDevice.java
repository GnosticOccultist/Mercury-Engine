package fr.mercury.nucleus.renderer.device;

import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.lwjgl.opengl.GLCapabilities;

import fr.alchemy.utilities.array.ArrayUtil;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.MercurySettings;

public class PhysicalDevice {
	
	/**
	 * The application logger.
	 */
	private static final Logger logger = FactoryLogger.getLogger("mercury.app");

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
	 * The array of available extensions.
	 */
	private final String[] extensions;
	/**
	 * The OpenGL context capabilities.
	 */
	private final GLCapabilities capabilities;
	
	public PhysicalDevice(Vendor vendor, String deviceName, String version, String[] extensions, GLCapabilities capabilities) {
		this.vendor = vendor;
		this.deviceName = deviceName;
		this.version = version;
		this.extensions = extensions;
		this.capabilities = capabilities;
	}
	
	public void check(MercurySettings settings) {
		String[] requiredExtensions = settings.getRequiredExtensions();
		if(requiredExtensions == null || requiredExtensions.length == 0) {
			return;
		}
		
		List<String> required = ArrayUtil.toList(requiredExtensions);
		boolean hasRequired = ArrayUtil.toList(extensions).containsAll(required);
		if(!hasRequired) {
			required.removeAll(ArrayUtil.toList(extensions));
			JOptionPane.showMessageDialog(null, "Unfortunately your video card doesn't support the required OpenGL extensions " 
					+ Arrays.toString(required.toArray()) + "! \n\n"
					+ "To run correctly " + settings.getTitle() + " you may need to upgrade your card, or select an "
					+ "appropriate video card if your laptop has switchable graphics. Try also to update your drivers!", 
					"Unsupported video card configuration", JOptionPane.ERROR_MESSAGE);
			
			System.exit(0);
		}
		
		logger.debug("The used device '" + deviceName  + "' has all required extensions for the application to run correctly: \n " 
				+ "* REQUIRED_EXTENSIONS = " + Arrays.toString(requiredExtensions));
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
	
	private String versionOnly() {
		return version.split(" ")[0];
	}
	
	public int majorVersion() {
		String versionOnly = versionOnly();
		return Integer.parseInt(versionOnly.split("\\.")[0]);
	}
	
	public int minorVersion() {
		String versionOnly = versionOnly();
		return Integer.parseInt(versionOnly.split("\\.")[1]);
	}
	
	public int releaseNumberVersion() {
		String versionOnly = versionOnly();
		String[] splitVersion = versionOnly.split("\\.");
		if(splitVersion.length > 2) {
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
		return "* VENDOR = " + vendor + "\n" 
			 + "* DEVICE_NAME = " + deviceName + "\n"
			 + "* GRAPHICS_API_VERSION = " + version + "\n"
			 + "* EXTENSIONS_COUNT = " + extensions.length + "\n"
		 	 + "* EXTENSIONS = " + Arrays.toString(extensions) + "\n";
	}
}
