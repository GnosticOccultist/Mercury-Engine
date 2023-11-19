package fr.mercury.nucleus.application.kernel;

/**
 * <code>OSArch</code> enumerates all recognized operating system specific
 * architecture that can run on a machine.
 * 
 * @author GnosticOccultist
 */
public enum OSArch {

    /**
     * Microsoft Windows 32-bits AMD/Intel.
     */
    WINDOWS_32(OS.WINDOWS),
    /**
     * Microsoft Windows 64-bits AMD/Intel.
     */
    WINDOWS_64(OS.WINDOWS, 64),
    /**
     * Microsoft Windows 32-bits ARM.
     */
    WINDOWS_ARM_32(OS.WINDOWS),
    /**
     * Microsoft Windows 64-bits ARM.
     */
    WINDOWS_ARM_64(OS.WINDOWS, 64),
    /**
     * Linux 32-bits Intel.
     */
    LINUX_32(OS.LINUX),
    /**
     * Linux 64-bits Intel.
     */
    LINUX_64(OS.LINUX, 64),
    /**
     * Linux 32-bits ARM.
     */
    LINUX_ARM_32(OS.LINUX),
    /**
     * Linux 64-bits ARM.
     */
    LINUX_ARM_64(OS.LINUX, 64),
    /**
     * Apple Mac OS X 32-bits Intel.
     */
    MAC_OSX_32(OS.MAC_OS),
    /**
     * Apple Mac OS X 64-bits Intel.
     */
    MAC_OSX_64(OS.MAC_OS, 64),
    /**
     * Apple Mac OS X 64-bits ARM, no 32-bits architecture support.
     */
    MAC_OSX_ARM_64(OS.MAC_OS, 64),
    /**
     * Generic web platform on unknown architecture, assume to be 64 bits.
     */
    WEB(OS.WEB, 64);

    /**
     * The underlying operating system.
     */
    private OS os;
    /**
     * The number of address space bits, either 32 or 64 bits.
     */
    private int bits;

    OSArch(OS os) {
        this.os = os;
        this.bits = 32;
    }

    OSArch(OS os, int bits) {
        assert bits == 32 || bits == 64 : "Address space must be either 32 or 64 bits";
        this.os = os;
        this.bits = bits;
    }

    /**
     * Return the underlying operating system for the architecture.
     * 
     * @return The underlying operating system (not null).
     */
    public OS os() {
        return os;
    }

    /**
     * Return the number of address space bits, either 32 or 64 bits.
     * 
     * @return The number of address space bits.
     */
    public int bits() {
        assert bits == 32 || bits == 64 : "Address space must be either 32 or 64 bits";
        return bits;
    }

    /**
     * Try to resolve <code>OSArch</code> onto which the JVM is currenlty running
     * using {@link System#getProperty()}.
     * <p>
     * A reference is kept in {@link MercuryContext}, use {@link MercuryContext#getOSArch()} to access it.
     * 
     * @return The operating system architecture (not null).
     */
    public static OSArch resolveFromJavaProperty() {
        var os = System.getProperty("os.name").toLowerCase();
        var arch = System.getProperty("os.arch").toLowerCase();
        var is64 = arch.contains("64");
        var armArch = arch.startsWith("arm") || arch.startsWith("aarch");

        if (os.contains("windows")) {
            if (armArch) {
                return is64 ? OSArch.WINDOWS_ARM_64 : OSArch.WINDOWS_ARM_32;
            } else {
                return is64 ? OSArch.WINDOWS_64 : OSArch.WINDOWS_32;
            }
        } else if (os.contains("linux") || os.contains("freebsd") || os.contains("sunos") || os.contains("unix")) {
            if (armArch) {
                return is64 ? OSArch.LINUX_ARM_64 : OSArch.LINUX_ARM_32;
            } else {
                return is64 ? OSArch.LINUX_64 : OSArch.LINUX_32;
            }
        } else if (os.contains("mac os x") || os.contains("darwin")) {
            if (armArch) {
                return OSArch.MAC_OSX_ARM_64;
            } else {
                return is64 ? OSArch.MAC_OSX_64 : OSArch.MAC_OSX_32;
            }
        }

        throw new UnsupportedOperationException("Unrecognized OS Architecture " + os + " - " + arch + "!");
    }
}
