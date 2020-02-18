package fr.mercury.nucleus.renderer.queue;

import java.util.Comparator;

import fr.alchemy.utilities.SortUtil;
import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.renderer.AbstractRenderer;
import fr.mercury.nucleus.renderer.Camera;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;

/**
 * <code>RenderBucket</code> represents a temporary storage for {@link AnimaMundi} that are
 * about to be rendered.
 * <p>
 * It is in charge of sorting these animae based on the provided {@link Comparator}, to increase performance and quality
 * of the rendering process by limiting, for example, <code>OpenGL</code> state changes, redrawn pixel or drawing 
 * background object prior to a transparent one.
 * 
 * @author GnosticOccultist
 */
public class RenderBucket {
	
	/**
	 * The logger for the Mercury Renderer.
	 */
	protected static final Logger logger = FactoryLogger.getLogger("mercury.renderer");
	/**
	 * The initial size of the rendering bucket, specified as 16.
	 */
	protected static final int INITIAL_SIZE = 16;
	
	/**
	 * The distance comparator from the {@link Camera} to the {@link AnimaMundi}.
	 */
	protected final Comparator<AnimaMundi> DISTANCE_COMPARATOR = new Comparator<AnimaMundi>() {
		
		@Override
		public int compare(AnimaMundi anima1, AnimaMundi anima2) {
			
			double d1 = computeDistance(anima1);
			double d2 = computeDistance(anima2);
			
			if(d1 > d2) {
				return 1;
			} else if(d1 < d2) {
				return -1;
			}
			
			return 0;
		}
	};
	
	/**
	 * The comparator used to sort the animae.
	 */
	protected Comparator<AnimaMundi> comparator;
	/**
	 * The camera used to compute the distance.
	 */
	protected Camera camera;
	/**
	 * The array of stored animae in the bucket.
	 */
	protected AnimaMundi[] array;
	/**
	 * The current size of the bucket.
	 */
	protected int size;
	
	/**
	 * Instantiates a new <code>RenderBucket</code> with an initial size of {@link #INITIAL_SIZE} 
	 * and the given {@link Camera}.
	 * 
	 * @param camera The camera used to compute distances (not null).
	 */
	public RenderBucket(Camera camera) {
		this(INITIAL_SIZE, camera);
	}
	
	/**
	 * Instantiates a new <code>RenderBucket</code> with the specified initial size and {@link Camera}.
	 * 
	 * @param size   The initial size of the bucket (&ge;0).
	 * @param camera The camera used to compute distances (not null).
	 */
	public RenderBucket(int size, Camera camera) {
		Validator.nonNegative(size, "The size of the bucket can't be negative!");
		Validator.nonNull(camera, "The camera can't be null!");
		this.array = new AnimaMundi[size];
		this.camera = camera;
		this.comparator = DISTANCE_COMPARATOR;
	}
	
	/**
	 * Instantiates a new <code>RenderBucket</code> with an initial size of {@link #INITIAL_SIZE}, the {@link Camera}
	 * and {@link Comparator} to use to sort the {@link AnimaMundi} in the bucket. Usually the comparator
	 * uses the distance of the object to render from the camera.
	 * 
	 * @param camera 	 The camera used to compute distances (not null).
	 * @param comparator The comparator to sort the bucket, usually a distance factor (not null).
	 */
	public RenderBucket(Camera camera, Comparator<AnimaMundi> comparator) {
		this(INITIAL_SIZE, camera, comparator);
	}
	
	/**
	 * Instantiates a new <code>RenderBucket</code> with the specified initial size, the {@link Camera}
	 * and {@link Comparator} to use to sort the {@link AnimaMundi} in the bucket. Usually the comparator
	 * uses the distance of the object to render from the camera.
	 * 
	 * @param size   	 The initial size of the bucket (&ge;0).
	 * @param camera 	 The camera used to compute distances (not null).
	 * @param comparator The comparator to sort the bucket, usually a distance factor (not null).
	 */
	public RenderBucket(int size, Camera camera, Comparator<AnimaMundi> comparator) {
		Validator.nonNegative(size, "The size of the bucket can't be negative!");
		Validator.nonNull(camera, "The camera can't be null!");
		Validator.nonNull(comparator, "The comparator can't be null!");
		this.array = new AnimaMundi[size];
		this.camera = camera;
		this.comparator = comparator;
	}
	
	/**
	 * Add a new {@link AnimaMundi} to be rendered in the <code>RenderBucket</code>.
	 * The queue distance field of the anima is set to {@link Double#NEGATIVE_INFINITY} to be recomputed.
	 * <br>
	 * Note that if the bucket is already filled entirely, it will automatically double its size.
	 * 
	 * @param anima The anima-mundi to add to the bucket.
	 */
	public void add(AnimaMundi anima) {
		// Make sure to reset the distance, so it can be recomputed.
		anima.queueDistance = Double.NEGATIVE_INFINITY;
		
		// We've reached the end of the array, increase the size.
		if(size >= array.length) {
			AnimaMundi[] tmp = new AnimaMundi[array.length * 2];
			System.arraycopy(array, 0, tmp, 0, size);
			array = tmp;
		}
		
		// Add the anima to the next index.
		array[size++] = anima;
	}
	
	/**
	 * Sort the <code>RenderBucket</code> using the {@link Comparator} and a 
	 * <code>shellsort</code> sorter.
	 * Note that the comparator cannot be null.
	 */
	public void sort() {
		// Perform the sort only is there is more than one anima in the bucket.
		if(size > 1) {
			// Shell sorting the array.
			SortUtil.shellSort(array, 0, size - 1, comparator);
		}
	}
	
	/**
	 * Render the <code>RenderBucket</code> by calling {@link AbstractRenderer#render(PhysicaMundi)}
	 * for each {@link PhysicaMundi}, and resetting the queue distance field.
	 * 
	 * @param renderer The renderer used to render each animae independently.
	 */
	public void render(AbstractRenderer renderer) {
		for(int i = 0; i < size; i++) {
			var anima = array[i];
			
			// Delegates the rendering of the physica-mundi to the renderer. 
			if(anima instanceof PhysicaMundi) {
				renderer.render((PhysicaMundi) anima);
			}
			
			// Make sure to reset the distance, so it can be recomputed.
			// This is only if the bucket isn't flushed every frame.
			anima.queueDistance = Double.NEGATIVE_INFINITY;
		}
	}
	
	/**
	 * Compute the distance between the provided {@link AnimaMundi} and
	 * the registered {@link Camera}.
	 * Note that the camera cannot be null.
	 * 
	 * @param anima The anima to compute distance to.
	 * @return		The distance between the camera and the anima translation.
	 */
	protected double computeDistance(AnimaMundi anima) {
		Validator.nonNull(camera);
		
		// Distance has been already computed, return it.
		if(anima.queueDistance != Double.NEGATIVE_INFINITY) {
			return anima.queueDistance;
		}
		
		// Get the distance between the two vectors.
		var translation = anima.getWorldTransform().getTranslation();
		anima.queueDistance = camera.getLocation().distance(translation);
		
		return anima.queueDistance;
	}
	
	/**
	 * Merges the <code>RenderBucket</code>'s content with the specified bucket's one,
	 * by first adding the content of this bucket and then the content of the other bucket.
	 * <p>
	 * It returns this <code>RenderBucket</code> with the new content.
	 * 
	 * @param other The other bucket to merge the content with.
	 * @return		The bucket with the merged content.
	 */
	public RenderBucket merge(RenderBucket other) {
		// No need to merge an empty bucket.
		if(other.size() < 1) {
			return this;
		}
		
		AnimaMundi[] tmp = new AnimaMundi[array.length + other.array.length];
		// Adding content of this render bucket.
		for(int i = 0; i < array.length; i++) {
        	tmp[i] = array[i];
        }
		
		// Adding content of the other render bucket.
        for(int i = 0; i < array.length; i++) {
        	tmp[i] = other.array[i];
        }
        
        array = tmp;
        return this;
	}
	
	/**
	 * Flushes the <code>RenderBucket</code> and all the contained {@link AnimaMundi}, if any.
	 * The size of the bucket is reset to 0.
	 */
	public void flush() {
		// Check the size of the bucket first.
		if(size > 0) {
			// Set all the value to null and the size to 0.
			for(int i = 0; i < array.length; i++) {
				array[i] = null;
			}
			size = 0;
		}
	}
	
	/**
	 * Sets the {@link Camera} used by the <code>RenderBucket</code>.
	 * 
	 * @param camera The camera used by the bucket (not null).
	 */
	public void setCamera(Camera camera) {
		Validator.nonNull(camera, "The camera can't be null!");
		this.camera = camera;
	}
	
	/**
	 * Return the array of the <code>RenderBucket</code>.
	 * 
	 * @return The array of the bucket.
	 */
	public AnimaMundi[] array() {
		return array;
	}
	
	/**
	 * Return the size of the <code>RenderBucket</code>.
	 * 
	 * @return The size of the bucket.
	 */
	public int size() {
		return size;
	}
	
	public class TransparentComparator implements Comparator<AnimaMundi> {

		@Override
		public int compare(AnimaMundi anima1, AnimaMundi anima2) {
			double d1 = computeDistance(anima1);
			double d2 = computeDistance(anima2);
			
			if(d1 > d2) {
				return 1;
			} else if(d1 < d2) {
				return -1;
			}
			
			return 0;
		}
	}
}
