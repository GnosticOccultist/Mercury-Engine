package fr.mercury.nucleus.renderer;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

import fr.alchemy.utilities.Instantiator;
import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.AbstractApplicationService;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.FloatBufferPopulator;
import fr.mercury.nucleus.math.objects.Matrix3f;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.readable.ReadableMatrix3f;
import fr.mercury.nucleus.math.readable.ReadableMatrix4f;
import fr.mercury.nucleus.math.readable.ReadableTransform;
import fr.mercury.nucleus.renderer.logic.state.BlendState;
import fr.mercury.nucleus.renderer.logic.state.DepthBufferState;
import fr.mercury.nucleus.renderer.logic.state.FaceCullingState;
import fr.mercury.nucleus.renderer.logic.state.PolygonModeState;
import fr.mercury.nucleus.renderer.logic.state.PolygonModeState.PolygonMode;
import fr.mercury.nucleus.renderer.logic.state.RenderState;
import fr.mercury.nucleus.renderer.logic.state.RenderState.Face;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.UniformStructure;
import fr.mercury.nucleus.renderer.queue.BucketType;
import fr.mercury.nucleus.renderer.queue.RenderBucket;
import fr.mercury.nucleus.scenegraph.AnimaMundi;
import fr.mercury.nucleus.scenegraph.Material;
import fr.mercury.nucleus.scenegraph.PhysicaMundi;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.OpenGLCall;

public abstract class AbstractRenderer extends AbstractApplicationService implements Renderer {

    /**
     * The logger for the Mercury Renderer.
     */
    protected static final Logger logger = FactoryLogger.getLogger("mercury.renderer");

    /**
     * The table containing the various render buckets organized by their types.
     */
    protected final Map<BucketType, RenderBucket> buckets = new HashMap<BucketType, RenderBucket>();
    /**
     * The table containing the various matrices used for rendering in the shader as
     * a float buffer.
     */
    protected final EnumMap<MatrixType, FloatBufferPopulator> matrixMap = new EnumMap<>(MatrixType.class);
    /**
     * The clear values for the color buffer used by the renderer.
     */
    protected final Color clearColor = new Color(0, 0, 0, 0);
    /**
     * The render state machine for switching between states.
     */
    protected final RenderStateMachine renderStateMachine = new RenderStateMachine(this);
    /**
     * The camera used by the renderer.
     */
    protected Camera camera;

    /**
     * Instantiates a new <code>AbstractRenderer</code> with the provided
     * {@link Camera}.
     * 
     * @param camera The camera to use for rendering (not null).
     */
    protected AbstractRenderer(Camera camera) {
        this(camera, new FaceCullingState().setFace(Face.BACK).enable(), new PolygonModeState(), new BlendState(),
                new DepthBufferState().enable());
    }

    /**
     * Instantiates a new <code>AbstractRenderer</code> with the provided
     * {@link Camera}. Note that the provided default {@link RenderState} will be
     * applied directly.
     * 
     * @param camera        The camera to use for rendering (not null).
     * @param defaultStates The default render states used by the renderer.
     */
    protected AbstractRenderer(Camera camera, RenderState... defaultStates) {
        Validator.nonNull(camera, "The camera can't be null!");

        this.camera = camera;
        this.renderStateMachine.withDefaultStates(defaultStates);

        for (var type : RenderState.Type.values()) {
            renderStateMachine.applyDefault(type);
        }
    }

    /**
     * Register a new {@link RenderBucket} with the specified {@link BucketType} for
     * the <code>AbstractRenderer</code>.
     * 
     * @param type The bucket type to register (not null).
     */
    public void registerBucket(BucketType type) {
        Validator.nonNull(type, "The bucket type to register can't be null!");
        if (type.equals(BucketType.LEGACY) || type.equals(BucketType.NONE)) {
            throw new MercuryException("The bucket '" + type + "' cannot be registered!");
        }

        buckets.put(type, new RenderBucket(camera));
    }

    /**
     * Register a new {@link RenderBucket} with the specified {@link BucketType} and
     * {@link Comparator} for the <code>AbstractRenderer</code>.
     * 
     * @param type       The bucket type to register (not null).
     * @param comparator The comparator to sort the anima-mundi to render (not null).
     */
    public void registerBucket(BucketType type, Comparator<AnimaMundi> comparator) {
        Validator.nonNull(type, "The bucket type to register can't be null!");
        if (type.equals(BucketType.LEGACY) || type.equals(BucketType.NONE)) {
            throw new MercuryException("The bucket '" + type + "' cannot be registered!");
        }

        buckets.put(type, new RenderBucket(camera, comparator));
    }

    /**
     * Register a new {@link RenderBucket} with the specified {@link BucketType} and
     * {@link Comparator} for the <code>AbstractRenderer</code>.
     * 
     * @param type   The bucket type to register (not null).
     * @param bucket The render bucket to register for the type (not null).
     */
    public void registerBucket(BucketType type, RenderBucket bucket) {
        Validator.nonNull(type, "The bucket type to register can't be null!");
        Validator.nonNull(bucket, "The bucket to register can't be null!");

        if (type.equals(BucketType.LEGACY) || type.equals(BucketType.NONE)) {
            throw new MercuryException("The bucket '" + type + "' cannot be registered!");
        }

        buckets.put(type, bucket);
    }

    /**
     * Submit the specified {@link AnimaMundi} to a {@link RenderBucket} matching
     * the {@link BucketType}.
     * <p>
     * If the type of bucket isn't registered for this
     * <code>AbstractRenderer</code>, it will not add the anima to be rendered, call
     * {@link #registerBucket(BucketType)} to register the needed bucket's type.
     * 
     * @param anima The anima-mundi to add to a bucket.
     * @return      Whether the anima-mundi has been added to a bucket.
     */
    protected boolean submitToBucket(AnimaMundi anima) {
        if (anima.getBucket().equals(BucketType.NONE) || !camera.checkLayer(anima.getRenderLayer())) {
            return false;
        }

        var type = anima.getBucket();

        var bucket = buckets.get(type);
        if (bucket != null) {
            bucket.add(anima);
            return true;
        }

        logger.warning("The anima '" + anima + "' couldn't be submitted to a bucket of type " + type + "!");
        return false;
    }

    /**
     * Render the {@link RenderBucket} corresponding to the specified {@link BucketType}. It will first 
     * call {@link RenderBucket#sort()} and then {@link RenderBucket#render(AbstractRenderer)}.
     * 
     * @param type The type of bucket to render.
     * 
     * @throws MercuryException Thrown if the type is either {@link BucketType#LEGACY} 
     *                          or {@link BucketType#NONE}.
     * @throws IllegalStateException Thrown if there is no registered bucket of the 
     *                               specified type in the renderer.
     */
    protected void renderBucket(BucketType type) {
        if (type.equals(BucketType.LEGACY) || type.equals(BucketType.NONE)) {
            throw new MercuryException("The bucket '" + type + "' cannot be rendered!");
        }

        var bucket = buckets.get(type);
        if (bucket == null) {
            throw new IllegalStateException("No bucket for type: " + type + " is defined in the renderer!");
        }

        if (bucket.isEmpty()) {
            return;
        }

        bucket.sort();
        bucket.render(this);

        logger.debug("Rendered bucket of type '" + type + "' which contained " + bucket.size() + " anima-mundi.");
    }

    /**
     * Flushes all registered {@link RenderBucket} in the <code>AbstractRenderer</code>, by emptying the 
     * bucket of its {@link AnimaMundi} and reseting its size to 0.
     * 
     * @see RenderBucket#flush()
     */
    protected void flushBuckets() {
        buckets.values().forEach(RenderBucket::flush);
    }

    /**
     * Render the provided {@link AnimaMundi}. Override this method in your implementation of 
     * <code>AbstractRenderer</code>.
     * 
     * @param physica The physica-mundi to render (not null).
     */
    public abstract void render(PhysicaMundi physica);

    /**
     * Sets the clear values of the <code>AbstractRenderer</code> for the color-buffer.
     * 
     * @param color The color to be cleared from the buffer (not null).
     */
    @Override
    @OpenGLCall
    public void setClearColor(Color color) {
        Validator.nonNull(color, "The clear color can't be null!");
        if (!clearColor.equals(color)) {
            clearColor.set(color);
            GL11C.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
        }
    }

    /**
     * Sets the depth range for the viewport to the provided near and far values.
     * 
     * @param nearDepthRange The near depth range (&ge; 0 &le;1, default &rarr; 0).
     * @param farDepthRange  The far depth range (&ge; 0 &le;1, default &rarr; 1).
     */
    @OpenGLCall
    protected void setDepthRange(double nearDepthRange, double farDepthRange) {
        GL11C.glDepthRange(nearDepthRange, farDepthRange);
    }

    /**
     * Apply the provided <code>RenderState</code> to the <code>OpenGL</code> context.
     * 
     * @param state The render state to apply (not null).
     */
    @OpenGLCall
    protected void applyRenderState(RenderState state) {
        Validator.nonNull(state, "The render state can't be null!");

        logger.debug("Applying state " + state);
        switch (state.type()) {
        case FACE_CULLING:
            var cull = (FaceCullingState) state;
            if (cull.isEnabled()) {
                GL11.glEnable(GL11.GL_CULL_FACE);
                switch (cull.face()) {
                case BACK:
                    GL11.glCullFace(GL11.GL_BACK);
                    break;
                case FRONT:
                    GL11.glCullFace(GL11.GL_FRONT);
                    break;
                case FRONT_AND_BACK:
                    GL11.glCullFace(GL11.GL_FRONT_AND_BACK);
                    break;
                default:
                    break;
                }
            } else {
                GL11.glDisable(GL11.GL_CULL_FACE);
            }
            switch (cull.windingOrder()) {
            case CLOCKWISE:
                GL11.glFrontFace(GL11.GL_CW);
                break;
            case COUNTER_CLOCKWISE:
                GL11.glFrontFace(GL11.GL_CCW);
                break;
            default:
                break;
            }
            break;
        case POLYGON_MODE:
            var wireframe = (PolygonModeState) state;
            if (wireframe.isEnabled()) {
                PolygonMode fMode = wireframe.polygonMode(Face.FRONT);
                PolygonMode bMode = wireframe.polygonMode(Face.BACK);
                if (fMode == bMode) {
                    switch (bMode) {
                    case FILL:
                        GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_FILL);
                        break;
                    case LINE:
                        GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_LINE);
                        break;
                    case POINT:
                        GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_POINT);
                        break;
                    }
                } else if (fMode != bMode) {
                    switch (fMode) {
                    case FILL:
                        GL11C.glPolygonMode(GL11C.GL_FRONT, GL11C.GL_FILL);
                        break;
                    case LINE:
                        GL11C.glPolygonMode(GL11C.GL_FRONT, GL11C.GL_LINE);
                        break;
                    case POINT:
                        GL11C.glPolygonMode(GL11C.GL_FRONT, GL11C.GL_POINT);
                        break;
                    }
                    switch (bMode) {
                    case FILL:
                        GL11C.glPolygonMode(GL11C.GL_BACK, GL11C.GL_FILL);
                        break;
                    case LINE:
                        GL11C.glPolygonMode(GL11C.GL_BACK, GL11C.GL_LINE);
                        break;
                    case POINT:
                        GL11C.glPolygonMode(GL11C.GL_BACK, GL11C.GL_POINT);
                        break;
                    }
                }
            } else {
                GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_FILL);
            }
            break;
        case DEPTH_BUFFER:
            var zBuffer = (DepthBufferState) state;
            if (zBuffer.isEnabled()) {
                GL11C.glEnable(GL11.GL_DEPTH_TEST);
                switch (zBuffer.function()) {
                case NEVER:
                    GL11C.glDepthFunc(GL11C.GL_NEVER);
                    break;
                case ALWAYS:
                    GL11C.glDepthFunc(GL11C.GL_ALWAYS);
                    break;
                case EQUAL:
                    GL11C.glDepthFunc(GL11C.GL_EQUAL);
                    break;
                case NOT_EQUAL:
                    GL11C.glDepthFunc(GL11C.GL_NOTEQUAL);
                    break;
                case LESS:
                    GL11C.glDepthFunc(GL11C.GL_LESS);
                    break;
                case LESS_OR_EQUAL:
                    GL11C.glDepthFunc(GL11C.GL_LEQUAL);
                    break;
                case GREATER:
                    GL11C.glDepthFunc(GL11C.GL_GREATER);
                    break;
                case GREATER_OR_EQUAL:
                    GL11C.glDepthFunc(GL11C.GL_GEQUAL);
                    break;
                default:
                    break;
                }
            } else {
                GL11C.glDisable(GL11C.GL_DEPTH_TEST);
            }
            GL11C.glDepthMask(zBuffer.isWritable());
            break;
        case BLEND_STATE:
            var blend = (BlendState) state;
            if (blend.isEnabled()) {
                GL11C.glEnable(GL11.GL_BLEND);

                int srcFactor = GL11C.GL_ONE;
                int dstFactor = GL11C.GL_ZERO;
                switch (blend.srcFactor()) {
                case ONE:
                    srcFactor = GL11C.GL_ONE;
                    break;
                case ZERO:
                    srcFactor = GL11C.GL_ZERO;
                    break;
                case SOURCE_COLOR:
                    srcFactor = GL11C.GL_SRC_COLOR;
                    break;
                case ONE_MINUS_SOURCE_COLOR:
                    srcFactor = GL11C.GL_ONE_MINUS_SRC_COLOR;
                    break;
                case SOURCE_ALPHA:
                    srcFactor = GL11C.GL_SRC_ALPHA;
                    break;
                case ONE_MINUS_SOURCE_ALPHA:
                    srcFactor = GL11C.GL_ONE_MINUS_SRC_ALPHA;
                    break;
                default:
                    break;
                }
                switch (blend.dstFactor()) {
                case ONE:
                    dstFactor = GL11C.GL_ONE;
                    break;
                case ZERO:
                    dstFactor = GL11C.GL_ZERO;
                    break;
                case SOURCE_COLOR:
                    dstFactor = GL11C.GL_SRC_COLOR;
                    break;
                case ONE_MINUS_SOURCE_COLOR:
                    dstFactor = GL11C.GL_ONE_MINUS_SRC_COLOR;
                    break;
                case SOURCE_ALPHA:
                    dstFactor = GL11C.GL_SRC_ALPHA;
                    break;
                case ONE_MINUS_SOURCE_ALPHA:
                    dstFactor = GL11C.GL_ONE_MINUS_SRC_ALPHA;
                    break;
                default:
                    break;
                }
                GL11C.glBlendFunc(srcFactor, dstFactor);
            } else {
                GL11C.glDisable(GL11C.GL_BLEND);
            }
            break;
        default:
            break;
        }
    }

    /**
     * Setup the {@link Uniform} corresponding to the needed {@link MatrixType}
     * specified by the provided {@link Material} and applied for the given
     * {@link ShaderProgram}.
     * 
     * @param shader  The shader program to which the matrix uniforms need to be
     *                passed (not null).
     * @param physica The physica-mundi requesting the matrix uniforms (not null).
     */
    protected void setupMatrixUniforms(ShaderProgram shader, PhysicaMundi physica) {
        Validator.nonNull(shader, "The shader program can't be null!");
        Validator.nonNull(shader, "The physica-mundi can't be null!");

        var matrixUniforms = physica.getMaterial().getPrefabUniforms();

        for (MatrixType type : MatrixType.values()) {
            var name = type.name();

            if (matrixUniforms.contains(name)) {
                setupMatrixUniforms(shader, type);
            }
        }
    }

    /**
     * Setup the {@link Uniform} corresponding to the needed {@link MatrixType} for
     * the provided {@link ShaderProgram}.
     * 
     * @param shader The shader program to which the matrix uniforms need to be
     *               passed (not null).
     * @param type   The matrix type to pass as a uniform through the shader program
     *               (not null).
     */
    protected void setupMatrixUniforms(ShaderProgram shader, MatrixType type) {
        Validator.nonNull(shader, "The shader program can't be null!");
        Validator.nonNull(type, "The matrix type can't be null!");

        if (type.canCompute()) {
            computeMatrix(type);
        }

        shader.addUniform(type.getUniformName(), type.getUniformType(), matrixMap.get(type));
    }

    /**
     * Setup the {@link UniformStructure} specified by the provided {@link Material}
     * and apply it for the given {@link ShaderProgram}.
     * 
     * @param shader  The shader program to setup the structure of uniforms for.
     * @param physica The physica-mundi requesting the prefab uniforms.
     */
    protected void setupPrefabUniforms(ShaderProgram shader, PhysicaMundi physica) {
        var prefabUniforms = physica.getMaterial().getPrefabUniforms();

        for (int i = 0; i < prefabUniforms.size(); i++) {

            var prefabName = prefabUniforms.get(i);
            if ("CAMERA_POS".equals(prefabName)) {
                shader.addUniform("cameraPos", UniformType.VECTOR3F, camera.getLocation());
            }

            // Look for an environment element attached to an animae.
            var property = physica.getEnvironmentElement(prefabName);

            if (property != null) {
                property.uniforms(shader);
            }
        }
    }

    /**
     * Return the stored {@link ReadableMatrix4f} under the given
     * {@link MatrixType}, or null if none is stored.
     * 
     * @param type The type of the rendering matrix.
     * @return     The stored rendering matrix, or null if none.
     */
    @SuppressWarnings("unchecked")
    public <F extends FloatBufferPopulator> F getMatrix(MatrixType type) {
        return (F) type.cast(matrixMap.get(type));
    }

    /**
     * Stores the provided {@link ReadableMatrix4f} for the given usage
     * {@link MatrixType}.
     * 
     * @param type   The type of the rendering matrix.
     * @param matrix The rendering matrix to store.
     */
    public void setMatrix(MatrixType type, ReadableMatrix4f matrix) {
        type.checkType(matrix);
        var buffer = (Matrix4f) matrixMap.computeIfAbsent(type, k -> type.newInstance());
        buffer.set(matrix);
    }

    /**
     * Stores the provided {@link ReadableMatrix3f} for the given usage
     * {@link MatrixType}.
     * 
     * @param type   The type of the rendering matrix (not null).
     * @param matrix The rendering matrix to store (not null).
     */
    public void setMatrix(MatrixType type, ReadableMatrix3f matrix) {
        type.checkType(matrix);
        var buffer = (Matrix3f) matrixMap.computeIfAbsent(type, k -> type.newInstance());
        buffer.set(matrix);
    }

    /**
     * Stores the provided {@link ReadableTransform} for the given usage
     * {@link MatrixType}.
     * 
     * @param type      The type of the rendering matrix (not null).
     * @param transform The transform to store as a matrix (not null).
     */
    public void setMatrix(MatrixType type, ReadableTransform transform) {
        var buffer = matrixMap.computeIfAbsent(type, k -> type.newInstance());
        if (type.accepts(Matrix4f.class)) {
            var matrix = (Matrix4f) buffer;
            transform.asModelMatrix(matrix);
        }
    }

    /**
     * Compute the specified {@link MatrixType}, if possible, with the other
     * matrices if they are provided. The type of matrix that can be computed are
     * marked as {@link MatrixType#canCompute()}.
     * 
     * @param type The type of matrix to compute.
     */
    public void computeMatrix(MatrixType type) {
        if (!type.canCompute()) {
            throw new IllegalArgumentException("The provided type of matrix: " + type + " can't be computed!");
        }

        var buffer = matrixMap.computeIfAbsent(type, k -> type.newInstance());

        switch (type) {
        case VIEW_PROJECTION_MODEL:
            var store = (Matrix4f) buffer;
            Matrix4f viewProj = getMatrix(MatrixType.VIEW_PROJECTION);

            // First compute the view projection if not already present.
            if (viewProj == null) {
                computeMatrix(MatrixType.VIEW_PROJECTION);
                viewProj = getMatrix(MatrixType.VIEW_PROJECTION);
            }

            store.set(viewProj);

            Matrix4f model = getMatrix(MatrixType.MODEL);
            store.mult(model, store);
            break;
        case VIEW_PROJECTION:
            store = (Matrix4f) buffer;

            Matrix4f projection = getMatrix(MatrixType.PROJECTION);
            Matrix4f view = getMatrix(MatrixType.VIEW);

            store.set(view);
            store.mult(projection, store);
            break;
        case NORMAL:
            // TODO:
            break;
        default:
            throw new UnsupportedOperationException("The provided type of matrix: " + type + " can't be computed!");
        }
    }

    /**
     * Return the {@link Camera} used for rendering in the <code>AbstractRenderer</code>.
     * 
     * @param camera The camera to render with (not null).
     */
    @Override
    public Camera getCamera() {
        assert camera != null;
        return camera;
    }
    
    /**
     * Sets the {@link Camera} used for rendering in the
     * <code>AbstractRenderer</code>. It will automatically set the camera for the
     * registered {@link RenderBucket} as well.
     * 
     * @param camera The camera to render with (not null).
     */
    @Override
    public void setCamera(Camera camera) {
        Validator.nonNull(camera, "The camera can't be null");

        this.buckets.values().forEach(bucket -> bucket.setCamera(camera));
        this.camera = camera;
    }

    /**
     * <code>MatrixType</code> is the enumeration of all matrices used for rendering
     * inside a {@link ShaderProgram}.
     * 
     * @author GnosticOccultist
     */
    public enum MatrixType {
        /**
         * The model matrix used to display a {@link PhysicaMundi} correctly in
         * world-space. It should take into account the world transform not the local
         * one.
         */
        MODEL("modelMatrix", false),
        /**
         * The view matrix used to display the scene-graph based on camera position.
         */
        VIEW("viewMatrix", false),
        /**
         * The projection matrix used to display the scene-graph based on window size.
         */
        PROJECTION("projectionMatrix", false),
        /**
         * The view-projection matrix is computed by the camera (or the renderer)
         * depending on implementations. It is mostly used to compute the
         * {@link #VIEW_PROJECTION_MODEL}.
         */
        VIEW_PROJECTION("viewProjectionMatrix", true),
        /**
         * The view-projection-model matrix used to display an entire scene-graph
         * correctly in 3D-space taking into account the camera, window and object's
         * transform.
         */
        VIEW_PROJECTION_MODEL("viewProjectionModelMatrix", true),
        /**
         * The normal matrix computed using the model matrix.
         */
        NORMAL("normalMatrix", true, Matrix3f.class);

        /**
         * The uniform name used inside the shader.
         */
        private final String uniformName;
        /**
         * Whether the matrix type can be computed.
         */
        private final boolean compute;
        /**
         * The type of matrix used.
         */
        private final Class<? extends FloatBufferPopulator> type;

        private MatrixType(String uniformName, boolean compute) {
            this(uniformName, compute, Matrix4f.class);
        }

        private MatrixType(String uniformName, boolean compute, Class<? extends FloatBufferPopulator> type) {
            this.uniformName = uniformName;
            this.compute = compute;
            this.type = type;
        }

        /**
         * Return the {@link Uniform} name used by the <code>MatrixType</code>.
         * 
         * @return The uniform name string.
         */
        public String getUniformName() {
            return uniformName;
        }

        /**
         * Return whether the <code>MatrixType</code> should be first computed, before
         * being added to a {@link Uniform}.
         * 
         * @return Whether the matrix should be computed.
         */
        public boolean canCompute() {
            return compute;
        }

        /**
         * Create and return a new instance to store the <code>MatrixType</code> in.
         * 
         * @return A new instance of matrix (not null).
         */
        @SuppressWarnings("unchecked")
        public <F extends FloatBufferPopulator> F newInstance() {
            return (F) Instantiator.fromClass(type);
        }

        @SuppressWarnings("unchecked")
        public <F extends FloatBufferPopulator> F cast(F populator) {
            return (F) type.cast(populator);
        }

        public <F extends FloatBufferPopulator> void checkType(F obj) {
            if (!accepts(obj)) {
                throw new IllegalArgumentException(this + " only accepts " + type.getSimpleName() + " !");
            }
        }

        public <F extends FloatBufferPopulator> boolean accepts(F obj) {
            return accepts(obj.getClass());
        }

        public <F extends FloatBufferPopulator> boolean accepts(Class<F> clazz) {
            return type.isAssignableFrom(clazz);
        }

        public UniformType getUniformType() {
            if (type == Matrix3f.class) {
                return UniformType.MATRIX3F;
            }

            return UniformType.MATRIX4F;
        }
    }
}
