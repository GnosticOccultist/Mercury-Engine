package fr.mercury.nucleus.asset.locator;

import java.io.InputStream;
import java.util.Optional;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.asset.AssetDescriptor;
import fr.mercury.nucleus.asset.AssetManager;

/**
 * <code>AssetLocator</code> describes an interface which can be registered
 * within an {@link AssetManager} to locate assets inside a physical or virtual
 * filesystem. It uses the name provided within the {@link AssetDescriptor} to
 * resolve an entry point to the asset.
 * 
 * @author GnosticOccultist
 * 
 * @see AssetManager#registerLocator(AssetLocator)
 * @see AssetManager#unregisterLocator(AssetLocator)
 * @see ClasspathLocator
 */
public interface AssetLocator {

    /**
     * Locate an asset based on the provided {@link AssetDescriptor}, returning a
     * {@link LocatedAsset} or null if the asset couldn't be located.
     * 
     * @param asset The descriptor of the asset to locate (not null).
     * @return A located asset or null if the asset couldn't be located.
     */
    LocatedAsset locate(AssetDescriptor<?> asset);

    /**
     * Locate an asset based on the provided {@link AssetDescriptor}, returning an
     * optional {@link LocatedAsset}, which may be empty if the asset couldn't be
     * located.
     * 
     * @param asset The descriptor of the asset to locate (not null).
     * @return An optional located asset, empty if the asset couldn't be located.
     */
    default Optional<LocatedAsset> locateSafe(AssetDescriptor<?> asset) {
        return Optional.ofNullable(locate(asset));
    }

    /**
     * <code>LocatedAsset</code> represents a located asset by an
     * {@link AssetLocator} containing an {@link InputStream} to access the asset
     * data.
     * 
     * @author GnosticOccultist
     */
    public abstract class LocatedAsset {

        /**
         * The asset manager.
         */
        private final AssetManager assetManager;
        /**
         * The descriptor of the located asset.
         */
        private final AssetDescriptor<?> asset;

        /**
         * Instantiates a new <code>LocatedAsset</code>.
         * 
         * @param assetManager The asset manager (not null).
         * @param asset        The descriptor of the located asset (not null).
         */
        protected LocatedAsset(AssetManager assetManager, AssetDescriptor<?> asset) {
            Validator.nonNull(assetManager, "The asset manager can't be null!");
            Validator.nonNull(asset, "The asset descriptor can't be null!");
            this.assetManager = assetManager;
            this.asset = asset;
        }

        /**
         * Open an {@link InputStream} to access the data of the
         * <code>LocatedAsset</code>.
         * 
         * @return An new input stream (not null).
         */
        public abstract InputStream openStream();

        /**
         * Locate a sibling asset based on this <code>LocatedAsset</code>, returning a
         * {@link LocatedAsset} or null if the asset couldn't be located.
         * 
         * @param sibling The descriptor of the sibling asset to locate (not null).
         * @return A located asset or null if the asset couldn't be located.
         */
        public abstract LocatedAsset sibling(AssetDescriptor<?> sibling);

        /**
         * Return an array of bytes by reading the <code>LocatedAsset</code>. The array
         * may be empty or null if the couldn't be read properly.
         * 
         * @return An array of bytes, or null/empty if the data couldn't be read.
         */
        public abstract byte[] getBytes();

        /**
         * Return the {@link AssetManager}.
         * 
         * @return The asset manager (not null).
         */
        public AssetManager assetManager() {
            return assetManager;
        }

        /**
         * Return the {@link AssetDescriptor} to describe the <code>LocatedAsset</code>.
         * 
         * @return The descriptor of the located asset (not null).
         */
        public AssetDescriptor<?> asset() {
            return asset;
        }

        /**
         * Return the name of the <code>LocatedAsset</code>.
         * 
         * @return The name of the located asset (not null or empty).
         */
        public String getName() {
            return asset.getName();
        }

        @Override
        public String toString() {
            return "LocatedAsset [asset=" + asset + "]";
        }
    }
}
