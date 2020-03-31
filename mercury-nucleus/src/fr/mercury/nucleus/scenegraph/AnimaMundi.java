package fr.mercury.nucleus.scenegraph;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Stack;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.collections.array.ReadOnlyArray;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.math.objects.Matrix3f;
import fr.mercury.nucleus.math.objects.Quaternion;
import fr.mercury.nucleus.math.objects.Transform;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.math.readable.ReadableMatrix3f;
import fr.mercury.nucleus.math.readable.ReadableQuaternion;
import fr.mercury.nucleus.math.readable.ReadableTransform;
import fr.mercury.nucleus.math.readable.ReadableVector3f;
import fr.mercury.nucleus.renderer.logic.state.RenderState;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Type;
import fr.mercury.nucleus.renderer.queue.BucketType;
import fr.mercury.nucleus.renderer.queue.RenderBucket;
import fr.mercury.nucleus.renderer.queue.RenderLayer;
import fr.mercury.nucleus.scenegraph.environment.EnvironmentElement;
import fr.mercury.nucleus.scenegraph.environment.EnvironmentMode;
import fr.mercury.nucleus.scenegraph.visitor.AbstractVisitor;
import fr.mercury.nucleus.scenegraph.visitor.DirtyType;
import fr.mercury.nucleus.scenegraph.visitor.VisitType;
import fr.mercury.nucleus.scenegraph.visitor.Visitor;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>AnimaMundi</code> is an abstraction layer for the <code>Tree-Data-Structure</code> representing 
 * an element inside a 3D scene in the <code>Mercury-Engine</code>. 
 * <p>
 * The engine provides two types of element: 
 * <li>{@link PhysicaMundi}, which represents a geometric element capable of being rendered on the screen.</li>
 * <li>{@link NucleusMundi}, which represents a node element capable of having one or multiple children (either node or geometric element).</li>
 * <p>
 * This technique allows for recursive traversal over each elements, by searching for the children of each one if they
 * can possess some and process the rendering of some of them.
 * <li><i>Note: A tree hierarchy can't have any cycle and a structure with no nodes is called <b>null</b> or <b>empty</b> tree.</i></li>
 * <p>
 * Each of these anima must have a parent except for the root element of the tree. They are also defined by a {@link Transform} representing 
 * their position/rotation/scale inside the global or local (node) space. A set of {@link EnvironmentElement} can be defined for the anima-mundi
 * to alter the visual property of local or subsequent nodes.
 * 
 * @author GnosticOccultist
 */
public abstract class AnimaMundi {
	
	private static final Map<RenderState.Type, Stack<RenderState>> states = new HashMap<>();
	
	/**
	 * The logger for the scene-graph.
	 */
	protected static final Logger logger = FactoryLogger.getLogger("mercury.scenegraph");
	
	/**
	 * An implementation of a visitor to update the transform of a hierarchy of anima-mundi.
	 */
	protected static final AbstractVisitor TRANSFORM_UPDATER = new AbstractVisitor() {
		
		@Override
		public void onVisit(AnimaMundi anima) {
			anima.updateWorldTransform();
		}
	};
	
	/**
	 * An implementation of a visitor to update the render states of a hierarchy of anima-mundi.
	 */
	protected static final AbstractVisitor RENDER_STATE_UPDATER = new AbstractVisitor() {
		
		@Override
		public void onVisit(AnimaMundi anima) {
			anima.collectState(states);
			anima.applyState(states);
		}
	};
	
	/**
	 * The name of the spatial, mainly used for debugging.
	 */
	protected String name;
	/**
	 * The transform of the anima-mundi relative to its parent.
	 */
	protected final Transform localTransform;
	/**
	 * The transform of the anima-mundi in the global scene-graph.
	 */
	protected final Transform worldTransform;
	/**
	 * The parent of the anima-mundi.
	 */
	protected NucleusMundi parent = null;
	/**
	 * The bucket used for queueing and rendering the anima-mundi.
	 */
	protected BucketType bucket = BucketType.LEGACY;
	/**
	 * The layer the anima-mundi is present on.
	 */
	protected RenderLayer layer = RenderLayer.LEGACY;
	/**
	 * The environment mode describing how environmental elements should be
	 * passed through the scene-graph.
	 */
	protected EnvironmentMode envMode = EnvironmentMode.LOCAL_PRIORITY;
	/**
	 * The array of environmental elements which are locally present on this anima-mundi.
	 */
	protected final Array<EnvironmentElement> envElements = Array.ofType(EnvironmentElement.class);
	/**
	 * The accumulated dirty marks by the anima-mundi. At instantiation
	 * it will contain {@link DirtyType#TRANSFORM}.
	 */
	protected final EnumSet<DirtyType> dirtyMarks = EnumSet.of(DirtyType.TRANSFORM, DirtyType.RENDER_STATE);
	/**
	 * The render states to be applied locally to the anima-mundi.
	 */
	protected final EnumMap<RenderState.Type, RenderState> renderStates = new EnumMap<>(Type.class);
	/**
	 * The queue distance computed by the {@link RenderBucket}.
	 */
	public transient double queueDistance = Double.NEGATIVE_INFINITY;
	
	/**
	 * Instantiates a new <code>AnimaMundi</code> with the {@link Transform}
	 * set to identity values.
	 */
	protected AnimaMundi() {
		this.localTransform = new Transform();
		this.worldTransform = new Transform();
	}
	
	/**
	 * Instantiates a new <code>AnimaMundi</code> with the {@link Transform}
	 * set to identity values and the given name.
	 * 
	 * @param name The name of the anima-mundi (not null).
	 */
	protected AnimaMundi(String name) {
		this();
		setName(name);
	}
	
	/**
	 * Update all geometric informations about the <code>AnimaMundi</code>.
	 * <p>
	 * If the implementation calling this method is a {@link NucleusMundi}, it will
	 * also update the geometric state of its children.
	 * 
	 * @param timer The timer used by the application (not null).
	 */
	public void updateGeometricState(ReadableTimer timer) {
		
		if(dirtyMarks.isEmpty()) {
			updateChildren(timer);
		} else {
			if(isDirty(DirtyType.TRANSFORM)) {
				visit(TRANSFORM_UPDATER, VisitType.PRE_ORDER);
			}
			
			if(isDirty(DirtyType.RENDER_STATE)) {
				visit(RENDER_STATE_UPDATER, VisitType.PRE_ORDER);
			}
			
			updateChildren(timer);
		}
	}
	
	/**
	 * Updates the children of the <code>AnimaMundi</code>. The method should be implemented for implementation
	 * such as {@link NucleusMundi} to cycle through its children and update their state.
	 * 
	 * @param timer The timer used by the application (not null).
	 */
	protected void updateChildren(ReadableTimer timer) {}
	
	/**
	 * Update the world {@link Transform} by combining the local transform
	 * of this <code>AnimaMundi</code> with the world one of its parent.
	 * <p>
	 * If the anima-mundi is {@link #isOrphan() orphan} it will just set 
	 * the local transform as the world transform.
	 */
	protected void updateWorldTransform() {
		logger.debug("Update world transform for " + name);
        if(parent != null) {
        	if(parent.isDirty(DirtyType.TRANSFORM)) {
        		throw new IllegalStateException("Invalid update of the world transform for " + 
        				toString() + ", parent " + parent.toString() + " is dirty!");
        	}
        	parent.worldTransform.worldTransform(localTransform, worldTransform);
        } else {
            worldTransform.set(localTransform);
        }
        
        dirtyMarks.remove(DirtyType.TRANSFORM);
	}
	
	/**
	 * Return whether the <code>AnimaMundi</code> contains the specified
	 * {@link DirtyType} mark, meaning it has to refresh this type of data.
	 * 
	 * @param type The data type to check for dirtiness (not null).
	 * @return	   Whether the specified data type is dirty.
	 */
	protected boolean isDirty(DirtyType type) {
		Validator.nonNull(type);
		return dirtyMarks.contains(type);
	}
	
	/**
	 * Sets the dirty mark for the specified {@link DirtyType} to this
	 * <code>AnimaMundi</code> and all of its hierarchy.
	 * 
	 * @param type The dirty mark to apply (not null).
	 */
	protected void dirty(DirtyType type) {
		Validator.nonNull(type);
		
		switch (type) {
			case TRANSFORM:
				propagateDown(type);
                
                if(parent != null) {
                	parent.dirty(type);
                }
                break;
			case RENDER_STATE:
				propagateDown(type);
				break;
		}
	}
	
	/**
	 * Propagates the {@link DirtyType} down the scenegraph starting from the <code>AnimaMundi</code>.
	 * If an implementation of this class handles children, it must propagate the dirty marks to its descendants.
	 * 
	 * @param type The dirty type to propagate down the scenegraph (not null).
	 */
	protected void propagateDown(DirtyType type) {
		Validator.nonNull(type, "The dirty type can't be null!");
		dirtyMarks.add(type);
	}
	
	/**
	 * Return the local {@link Transform} used by the <code>AnimaMundi</code>.
	 * This transform is relative to the parent of this anima-mundi.
	 * 
	 * @return The local transform of the anima-mundi.
	 * 
	 * @see #getWorldTransform
	 */
	public ReadableTransform getLocalTransform() {
		return localTransform;
	}
	
	/**
	 * Return the world {@link Transform} used by the <code>AnimaMundi</code>.
	 * This transform is relative to the whole scene-graph.
	 * 
	 * @return The world transform of the anima-mundi.
	 * @throws IllegalStateException Thrown if the world transform is dirty and 
	 * 								 should be first refresh.
	 * 
	 * @see #getLocalTransform() 
	 */
	public ReadableTransform getWorldTransform() {
		if(isDirty(DirtyType.TRANSFORM)) {
			throw new IllegalStateException("The world transform hasn't been computed yet!");
		}
		
		return worldTransform;
	}
	
	/**
	 * Sets the translation of this <code>AnimaMundi</code> to the provided {@link Vector3f} in
	 * the local coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param translation The translation vector to set.
	 * @return			  The changed anima-mundi.
	 */
	public AnimaMundi setTranslation(ReadableVector3f translation) {
		localTransform.setTranslation(translation);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
	
	/**
	 * Sets the translation of this <code>AnimaMundi</code> to the provided components in
	 * the local coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * 
	 * @param x The X-axis component of the translation.
	 * @param y The Y-axis component of the translation.
	 * @param z The Z-axis component of the translation.
	 * @return	The changed anima-mundi.
	 */
	public AnimaMundi setTranslation(float x, float y, float z) {
		localTransform.setTranslation(x, y, z);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
	
	/**
	 * Translates the <code>AnimaMundi</code> by the provided {@link Vector3f} in the local 
	 * coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param translation The translation vector.
	 * @return			  The changed anima-mundi.
	 */
	public AnimaMundi translate(ReadableVector3f translate) {
		localTransform.translate(translate);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
	
	/**
	 * Translates the <code>AnimaMundi</code> by the provided components in the local 
	 * coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * 
	 * @param x The X-axis component of the translation.
	 * @param y The Y-axis component of the translation.
	 * @param z The Z-axis component of the translation.
	 * @return	The changed anima-mundi.
	 */
	public AnimaMundi translate(float x, float y, float z) {
		localTransform.translate(x, y, z);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
	
	/**
	 * Sets the rotation of this <code>AnimaMundi</code> to the provided {@link Quaternion} in
	 * the local coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * <p>
	 * The provided quaternion cannot be null.
	 * 
	 * @param translation The rotation quaternion to set.
	 * @return			  The changed anima-mundi.
	 */
	public AnimaMundi setRotation(ReadableQuaternion rotation) {
		localTransform.setRotation(rotation);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
	
	/**
	 * Sets the rotation of this <code>AnimaMundi</code> to the provided {@link Matrix3f} in
	 * the local coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * <p>
	 * The provided matrix cannot be null.
	 * 
	 * @param translation The rotation matrix to set.
	 * @return			  The changed anima-mundi.
	 */
	public AnimaMundi setRotation(ReadableMatrix3f rotation) {
		localTransform.setRotation(rotation);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
	
	/**
	 * Sets the rotation of this <code>AnimaMundi</code> to the provided components in
	 * the local coordinate space, the W-component is leaved untouched. Note that the 
	 * {@link #getWorldTransform() world transform} won't be updated until {@link #updateGeometricState()} has been called.
	 * 
	 * @param x The X-axis rotation value aka pitch.
	 * @param y The Y-axis rotation value aka yaw.
	 * @param z The Z-axis rotation value aka roll.
	 * @return	The changed anima-mundi.
	 */
	public AnimaMundi setRotation(float x, float y, float z) {
		localTransform.setRotation(x, y, z);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
	
	/**
	 * Rotates the <code>AnimaMundi</code> by the provided {@link Quaternion} in the local 
	 * coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * <p>
	 * The provided quaternion cannot be null.
	 * 
	 * @param rotation The rotation quaternion.
	 * @return		   The changed anima-mundi.
	 */
	public AnimaMundi rotate(ReadableQuaternion rotation) {
		localTransform.rotate(rotation.x(), rotation.y(), rotation.z());
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
	
	/**
	 * Rotates the <code>AnimaMundi</code> by the provided x, y and z angles in radians in 
	 * the local coordinate space. Note that the {@link #getWorldTransform() world transform} 
	 * won't  be updated until {@link #updateGeometricState()} has been called.
	 * 
	 * @param x The X-axis angle aka pitch (in radians).
	 * @param y The Y-axis angle aka yaw (in radians).
	 * @param z The Z-axis angle aka roll (in radians).
	 * 
	 * @return	The rotated anima-mundi.
	 */
    public AnimaMundi rotate(float x, float y, float z) {
        localTransform.rotate(x, y, z);
        dirty(DirtyType.TRANSFORM);
        
        return this;
    }
    
    /**
	 * Sets the scale of this <code>AnimaMundi</code> to the provided {@link Vector3f} in
	 * the local coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param translation The scaling vector to set.
	 * @return			  The changed anima-mundi.
	 */
    public AnimaMundi setScale(Vector3f scale) {
		localTransform.setScale(scale);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
    
    /**
	 * Sets the scale of this <code>AnimaMundi</code> to the provided components in
	 * the local coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * 
	 * @param x The X-axis component of the scale.
	 * @param y The Y-axis component of the scale.
	 * @param z The Z-axis component of the scale.
	 * @return	The changed anima-mundi.
	 */
    public AnimaMundi setScale(float x, float y, float z) {
		localTransform.setScale(x, y, z);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
    
	/**
	 * Scales the <code>AnimaMundi</code> by the provided {@link Vector3f} in the local 
	 * coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * <p>
	 * The provided vector cannot be null.
	 * 
	 * @param scale The scaling vector.
	 * @return		The changed anima-mundi.
	 */
	public AnimaMundi scale(Vector3f scale) {
		localTransform.scale(scale);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
	
	/**
	 * Scales the <code>AnimaMundi</code> by the provided components in the local 
	 * coordinate space. Note that the {@link #getWorldTransform() world transform} won't 
	 * be updated until {@link #updateGeometricState()} has been called.
	 * 
	 * @param x The X-axis component of the scaling.
	 * @param y The Y-axis component of the scaling.
	 * @param z The Z-axis component of the scaling.
	 * @return	The changed anima-mundi.
	 */
	public AnimaMundi scale(float x, float y, float z) {
		localTransform.scale(x, y, z);
		dirty(DirtyType.TRANSFORM);
		
		return this;
	}
	
	/**
	 * Return the name of the <code>AnimaMundi</code>.
	 * 
	 * @return The name of the anima-mundi.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the <code>AnimaMundi</code>.
	 * 
	 * @param name The name of the anima-mundi (not null or empty).
	 */
	public void setName(String name) {
		Validator.nonEmpty(name, "The name can't be null");
		this.name = name;
	}
	
	/**
	 * Return the parent of the <code>AnimaMundi</code>.
	 * 
	 * @return The anima-mundi's parent.
	 */
	public NucleusMundi getParent() {
		return parent;
	}
	
	/**
	 * Sets the parent of the <code>AnimaMundi</code>. The method should be
	 * called internally by the {@link NucleusMundi} implementation.
	 * 
	 * @param parent The parent of the anima-mundi.
	 */
	protected void setParent(NucleusMundi parent) {
		this.parent = parent;
	}
	
	/**
	 * Return whether the <code>AnimaMundi</code> is orphan, 
	 * meaning it has no parent.
	 * 
	 * @return Whether the anima-mundi has no parent.
	 */
	public boolean isOrphan() {
		return parent == null;
	}
	
	/**
	 * Return whether the <code>AnimaMundi</code> has the provided {@link NucleusMundi}
	 * for ancestor (parent, parent's parent, etc.).
	 * 
	 * @param ancestor The nucleus-mundi to test as an ancestor.
	 * 
	 * @return Whether the provided nucleus-mundi is an ancestor of this anima-mundi.
	 */
	public boolean hasAncestor(NucleusMundi ancestor) {
		return parent != null && (parent.equals(ancestor) || parent.hasAncestor(ancestor));
	}
	
	/**
	 * Return the used {@link BucketType} for the rendering of this <code>AnimaMundi</code>.
	 * It will try to find the ancestor's bucket type if its type is {@link BucketType#LEGACY}.
	 * 
	 * @return The bucket type used to render the anima-mundi.
	 */
	public BucketType getBucket() {
		return getBucket(true);
	}
	
	/**
	 * Return the used {@link BucketType} for the rendering of this <code>AnimaMundi</code>.
	 * If the <code>checkLegacy</code> is set to true, it will search the parent's bucket type until
	 * it founds something different than {@link BucketType#LEGACY} or until the ancestor is orphan.
	 * 
	 * @param checkLegacy Whether to return the legacy bucket's type or the {@link BucketType#LEGACY}.
	 * 
	 * @return The bucket type used to render the anima-mundi.
	 */
	public BucketType getBucket(boolean checkLegacy) {
		var result = bucket;
		if(checkLegacy && parent != null && bucket.equals(BucketType.LEGACY)) {
			result = parent.getBucket(true);
		}
		
		// If no other bucket type is specified in the hierarchy, default to the OPAQUE bucket.
		if(checkLegacy && result.equals(BucketType.LEGACY)) {
			result = BucketType.OPAQUE;
		}
		
		return result;
	}
	

	/**
	 * Sets the used {@link BucketType} for the rendering of this <code>AnimaMundi</code>.
	 * 
	 * @param bucket The bucket type used to render the anima-mundi (not null).
	 */
	public void setBucket(BucketType bucket) {
		Validator.nonNull(bucket, "The bucket type can't be null!");
		this.bucket = bucket;
	}
	
	/**
	 * Return the {@link RenderLayer} on which the <code>AnimaMundi</code> is present.
	 * It will try to find the ancestor's render layer if its type is {@link RenderLayer#LEGACY}.
	 * 
	 * @return The render layer on which the anima-mundi is present.
	 */
	public RenderLayer getRenderLayer() {
		return getRenderLayer(true);
	}
	
	/**
	 * Return the {@link RenderLayer} on which the <code>AnimaMundi</code> is present.
	 * If the <code>checkLegacy</code> is set to true, it will search the parent's render layer until
	 * it founds something different than {@link RenderLayer#LEGACY} or until the ancestor is orphan.
	 * 
	 * @param checkLegacy Whether to return the legacy render layer or the {@link RenderLayer#LEGACY}.
	 * 
	 * @return The render layer on which the anima-mundi is present.
	 */
	public RenderLayer getRenderLayer(boolean checkLegacy) {
		var result = layer;
		if(checkLegacy && parent != null && layer.equals(RenderLayer.LEGACY)) {
			result = parent.getRenderLayer(true);
		}
		
		// If no other render layer is specified in the hierarchy, default to the DEFAULT layer.
		if(checkLegacy && result.equals(RenderLayer.LEGACY)) {
			result = RenderLayer.DEFAULT;
		}
		
		return result;
	}
	
	/**
	 * Sets the used {@link RenderLayer} on which the <code>AnimaMundi</code> should be present.
	 * 
	 * @param bucket The render layer on which the anima-mundi is present. (not null).
	 */
	public void setRenderLayer(RenderLayer layer) {
		Validator.nonNull(layer, "The render layer can't be null!");
		this.layer = layer;
	}
	
	/**
	 * Adds the provided {@link EnvironmentElement} locally to the <code>AnimaMundi</code>.
	 * 
	 * @param element The element to add locally to this anima-mundi (not null).
	 * 
	 * @throws IllegalArgumentException Throw if the element added is already present on this anima-mundi,
	 * 									and only one can be present.
	 */
	public void addEnvironmentElement(EnvironmentElement element) {
		Validator.nonNull(element, "The provided environment element can't be null!");
		if(element.isSingleton() && containsLocal(element.name())) {
			throw new IllegalArgumentException("The provided environment element is already defined for '" + getName() + "' !");
		}
		
		envElements.add(element);
	}
	
	/**
	 * Removes the provided {@link EnvironmentElement} locally from the <code>AnimaMundi</code>.
	 * 
	 * @param element The element to remove locally from this anima-mundi (not null).
	 * @return 		  Whether the element was removed from this anima-mundi.
	 */
	public boolean removeEnvironmentElement(EnvironmentElement element) {
		Validator.nonNull(element, "The provided environment element can't be null!");
		return envElements.remove(element);
	}
	
	/**
	 * Return whether an {@link EnvironmentElement} matching the provided name is present
	 * locally on this <code>AnimaMundi</code>.
	 * 
	 * @param name The name of the environment element to check.
	 * @return	   Whether an element matching the name is present locally.
	 */
	public boolean containsLocal(String name) {
		return getLocalEnvironmentElementOpt(name).isPresent();
	}
	
	/**
	 * Return an {@link EnvironmentElement} matching the provided name by searching locally and 
	 * through upper-hierarchy depending on the {@link EnvironmentMode} of the <code>AnimaMundi</code>.
	 * 
	 * @param name The name of the enviromnent element to get.
	 * @return	   The environment element matching the name.
	 */
	public EnvironmentElement getEnvironmentElement(String name) {
		// Search on the local environment only.
		if(envMode.equals(EnvironmentMode.LOCAL_ONLY)) {
			return getLocalEnvironmentElement(name);
		}
		
		var parent = getParent();
		if(envMode.equals(EnvironmentMode.LOCAL_PRIORITY)) {
			var property = containsLocal(name) ? getLocalEnvironmentElementOpt(name).get() : null;
			if(property == null && parent != null) {
				property = parent.getEnvironmentElement(name);
			}
			return property;
		}
		
		if(envMode.equals(EnvironmentMode.ANCESTOR_PRIORITY)) {
			var property = parent != null ? parent.getEnvironmentElement(name) : null;
			if(property == null) {
				property = getLocalEnvironmentElement(name);
			}
			
			return property;
		}
		
		return null;
	}
	
	/**
	 * Return an {@link Optional} value containing the {@link EnvironmentElement} present 
	 * locally on the <code>AnimaMundi</code> matching the provided name.
	 * <p>
	 * If no such element exists it will return an empty optional value.
	 * 
	 * @param name The name of the enviromnent element to get.
	 * @return	   An optional value containing the environment element matching the name,
	 * 			   or empty if no such element exists.
	 * 
	 * @see #getLocalEnvironmentElement(String)
	 */
	public Optional<EnvironmentElement> getLocalEnvironmentElementOpt(String name) {
		var optionalElement = envElements.stream().filter(element -> element.name()
				.equalsIgnoreCase(name)).findFirst();
		
		return optionalElement;
	}
	
	/**
	 * Return an {@link EnvironmentElement} present locally on the <code>AnimaMundi</code>
	 * matching the provided name.
	 * 
	 * @param name The name of the enviromnent element to get.
	 * @return	   The environment element matching the name.
	 * 
	 * @see #getLocalEnvironmentElementOpt(String)
	 * 
	 * @throws NoSuchElementException Thrown if no element present on the anima-mundi 
	 * 								  is matching the provided name.
	 */
	public EnvironmentElement getLocalEnvironmentElement(String name) {
		return getLocalEnvironmentElementOpt(name).get();
	}
	
	/**
	 * Return a readable-only array of the {@link EnvironmentElement} locally present on the 
	 * <code>AnimaMundi</code>.
	 * 
	 * @return A readable only version of the local environment elements.
	 */
	public ReadOnlyArray<EnvironmentElement> getLocalEnvironmentElements() {
		return envElements.readOnly();
	}
	
	/**
	 * Return the local {@link RenderState} corresponding to the provided {@link Type} locally present
	 * on the <code>AnimaMundi</code>.
	 * 
	 * @param type The type of render state to retrieve.
	 * @return	   The local render state of the anima-mundi.
	 */
	public RenderState getLocalRenderState(RenderState.Type type) {
		return renderStates.get(type);
	}
	
	/**
	 * Set the {@link RenderState} to use locally for the <code>AnimaMundi</code>.
	 * 
	 * @param states The render states to apply locally to the anima-mundi.
	 */
	public void setRenderStates(RenderState... states) {
		for(RenderState state : states) {
			renderStates.put(state.type(), state);
		}
		
		dirty(DirtyType.RENDER_STATE);
	}
	
	/**
	 * Set the {@link RenderState} to use locally for the <code>AnimaMundi</code>, and return the 
	 * previously applied one if any.
	 * 
	 * @param state The render state to apply locally to the anima-mundi.
	 * @return		The previously render state applied to the anima-mundi, or null if none.
	 */
	public RenderState setRenderState(RenderState state) {
		var previous = renderStates.put(state.type(), state);
		dirty(DirtyType.RENDER_STATE);
		return previous;
	}
	
	/**
	 * Collects the {@link RenderState} defined in the scenegraph to the given table, to be later applied
	 * to the <code>AnimaMundi</code>.
	 * 
	 * @param states The table to contain the render states (not null).
	 * 
	 * @see #applyState(Map)
	 */
	protected void collectState(Map<RenderState.Type, Stack<RenderState>> states) {
		Validator.nonNull(states, "The map to store render states can't be null!");
		for(var entry : renderStates.entrySet()) {
			var stack = states.getOrDefault(entry.getKey(), new Stack<RenderState>());
			stack.push(entry.getValue());
			states.put(entry.getKey(), stack);
		}
	}
	
	/**
	 * Applies all previously collected {@link RenderState} in the scenegraph inside the given table
	 * to the <code>AnimaMundi</code>.
	 * 
	 * @param states The table which contains the render states (not null).
	 * 
	 * @see #collectState(Map)
	 */
	protected void applyState(Map<RenderState.Type, Stack<RenderState>> states) {
		Validator.nonNull(states, "The map to apply render states can't be null!");
		for(var entry : states.entrySet()) {
			var stack = entry.getValue();
			if(stack != null) {
				renderStates.put(entry.getKey(), stack.peek());
			}
		}
		dirtyMarks.remove(DirtyType.RENDER_STATE);
	}
	
	/**
	 * Visit the <code>AnimaMundi</code> with the given {@link Visitor} and {@link VisitType}.
	 * The visit type defines in which order the concerned mundi should be visited.
	 * 
	 * @param visitor The visitor.
	 * @param type	  The type of visit to execute.
	 */
	public void visit(Visitor visitor, VisitType type) {
		visitor.visit(this);
	}
	
	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + " {name= " + name + "} ]";
	}
}
