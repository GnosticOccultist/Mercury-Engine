package fr.mercury.nucleus.renderer.queue;

import java.util.HashMap;
import java.util.Map;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.Renderer;
import fr.mercury.nucleus.scenegraph.AnimaMundi;

/**
 * <code>BucketType</code> describes a specific type of {@link RenderBucket} a {@link AnimaMundi} 
 * can use to be rendered on the screen. 
 * <p>
 * In order to do this the {@link Renderer} will have {@link Renderer#registerBucket(BucketType) to create} 
 * a {@link RenderBucket} for the type, so each anima-mundi which uses this type of bucket can be 
 * submitted to the bucket, sorted and finally rendered. 
 * 
 * @author GnosticOccultist
 */
public final class BucketType {
	
	/**
	 * The table mapping the rendering bucket with its name.
	 */
	private static final Map<String, BucketType> BUCKET_TYPES = new HashMap<String, BucketType>();
	
	/**
	 * Bucket for opaque surfaces, which can't be seen through. 
	 * The process is to render object front to back to prevent redrawing of pixels.
	 */
	public static final BucketType OPAQUE = get("Opaque");
	/**
	 * Use the {@link AnimaMundi}'s parent bucket, or default to {@link #OPAQUE} if it is orphan.
	 */
	public static final BucketType LEGACY = get("Legacy");
	/**
	 * Disable bucketing and render immediately the {@link AnimaMundi} to the back buffer.
	 */
	public static final BucketType NONE = get("None");
	
	/**
	 * Return the <code>BucketType</code> with the specified name.
	 * <p>
	 * If the bucket doesn't already exists it will instantiates a new one with this
	 * name and finally return it.
	 * 
	 * @param name The name of the bucket to retrieve or create.
	 * @return	   The bucket matching the name or a new one.
	 */
	public static BucketType get(String name) {
		Validator.nonEmpty(name, "The name of the bucket can't be null or empty!");
		
		BucketType bucket = BUCKET_TYPES.get(name);
		if(bucket == null) {
			bucket = new BucketType(name);
			BUCKET_TYPES.put(name, bucket);
		}
		
		return bucket;
	}
	
	/**
	 * The name of the bucket.
	 */
	private final String name;
	
	/**
	 * Instantiates a new <code>BucketType</code> with the provided name.
	 * <p>
	 * The access of this constructor is limited, you should use
	 * {@link #getBucket(String)} to retrieve or create a new <code>BucketType</code>.
	 * 
	 * @param name The name of the bucket.
	 */
	private BucketType(String name) {
		this.name = name;
	}
	
	/**
	 * Return the name of the <code>BucketType</code>.
	 * 
	 * @return The name of the bucket.
	 */
	@Override
	public String toString() {
		return name;
	}
}
