package fr.mercury.nucleus.application.kernel.device;

import fr.alchemy.utilities.Validator;

/**
 * <code>Vendor</code> enumerates all possible device vendor which provides a graphics API implementation.
 * 
 * @author GnosticOccultist
 */
public enum Vendor {

    /**
     * Advanced Micro Device (AMD) Vendor.
     */
    AMD,
    /**
     * Intel Corporation Vendor.
     */
    INTEL,
    /**
     * Nvidia Corporation Vendor.
     */
    NVIDIA,
    /**
     * Advanced RISC Machines (ARM) Vendor.
     */
    ARM,
    /**
     * Imagination Technologies Group (ImgTec) Vendor.
     */
    IMGTEC,
    /**
     * Qualcomm Incorporated Vendor.
     */
    QUALCOMM,
    /**
     * Google Inc. Swift Shader Vendor.
     */
    SWIFT_SHADER,
    /**
     * The generic value for an unknown vendor.
     */
    GENERIC_UNKNOWN_VENDOR;

    /**
     * Return the corresponding {@link Vendor} to the given OpenGL equivalent.
     * 
     * @param vendor The OpenGL vendor to convert (not null, not empty).
     * @return       The enumeration equivalent of the OpenGL vendor (not null), or
     *               {@link #GENERIC_UNKNOWN_VENDOR} if no similarity.
     */
    public static Vendor fromGLVendor(String vendor) {
        Validator.nonEmpty(vendor, "The OpenGL vendor can't be empty or null!");

        if (vendor.contains("AMD")) {
            return Vendor.AMD;
        } else if (vendor.contains("Intel")) {
            return Vendor.INTEL;
        } else if (vendor.contains("NVIDIA")) {
            return Vendor.NVIDIA;
        } else if (vendor.contains("ARM")) {
            return Vendor.ARM;
        } else if (vendor.contains("ImgTec")) {
            return Vendor.IMGTEC;
        } else if (vendor.contains("Qualcomm")) {
            return Vendor.QUALCOMM;
        } else if (vendor.contains("SwiftShader") || vendor.contains("Google Inc.")) {
            return Vendor.SWIFT_SHADER;
        }

        return Vendor.GENERIC_UNKNOWN_VENDOR;
    }
}
