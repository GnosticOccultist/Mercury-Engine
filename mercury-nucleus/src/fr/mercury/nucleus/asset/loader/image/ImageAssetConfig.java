package fr.mercury.nucleus.asset.loader.image;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.asset.loader.AssetLoader;
import fr.mercury.nucleus.asset.loader.AssetLoader.Config;
import fr.mercury.nucleus.texture.Image;

/**
 * <code>ImageAssetConfig</code> is an implementation of {@link Config} to
 * describe the generic available options to load an {@link Image} from an
 * {@link AssetLoader}.
 * <p>
 * {@link FlipMode} parameter allow the user to flip the pixel data of an image
 * along an axis, by default it is set to {@link FlipMode#NO_FLIP}.
 * 
 * @author GnosticOccultist
 */
public class ImageAssetConfig implements Config {

    /**
     * The flip mode for the image data.
     */
    private FlipMode flipMode;

    /**
     * Instantiates a new <code>ImageAssetConfig</code> with
     * {@link FlipMode#NO_FLIP}.
     */
    public ImageAssetConfig() {
        this(FlipMode.NO_FLIP);
    }

    /**
     * Instantiates a new <code>ImageAssetConfig</code> with the provided
     * {@link FlipMode}.
     * 
     * @param flipMode The flip mode to use (not null).
     */
    public ImageAssetConfig(FlipMode flipMode) {
        this.flipMode = flipMode;
    }

    /**
     * Return the {@link FlipMode} of the <code>ImageAssetConfig</code>.
     * 
     * @return The flip mode of the config (not null).
     */
    public FlipMode flipMode() {
        return flipMode;
    }

    /**
     * Sets the {@link FlipMode} of the <code>ImageAssetConfig</code>, to use when
     * loading an {@link Image}.
     * 
     * @param flipMode The flip mode to use (not null).
     * @return The image config for chaining purposes (not null).
     */
    public ImageAssetConfig setFlipMode(FlipMode flipMode) {
        Validator.nonNull(flipMode, "The flip mode can't be null!");
        this.flipMode = flipMode;
        return this;
    }

    /**
     * <code>FlipMode</code> enumerates options for flipping axes of an image data
     * while loading.
     * 
     * @author GnosticOccultist
     */
    public enum FlipMode {

        /**
         * No flipping is applied, origin of the image is set to the top-left corner
         * expanding right and down.
         */
        NO_FLIP,
        /**
         * Flip the image along the Y-axis, origin of the image is set to the
         * bottom-left corner expanding right and up.
         */
        FLIP_Y;
    }
}
