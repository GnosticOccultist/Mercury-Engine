package fr.mercury.nucleus.scenegraph.shape;

import org.lwjgl.BufferUtils;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.GLBuffer.Usage;
import fr.mercury.nucleus.renderer.opengl.vertex.VertexBufferType;
import fr.mercury.nucleus.scenegraph.Mesh;

/**
 * <code>Quad</code> is an implementation of {@link Mesh} which describes the
 * vertex data of a four-sided, two dimensional shape extending from both the
 * X-axis and Y-axis.
 * 
 * @author GnosticOccultist
 */
public class Quad extends Mesh {

    /**
     * The texture coordinates vertex data of the quad.
     */
    private static final float[] TEX_COORDS_DATA = { 0, 1, 0, 0, 1, 0, 1, 1 };
    /**
     * The normals vertex data of the quad.
     */
    private static final float[] NORMALS_DATA = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
    /**
     * The indices of the quad.
     */
    private static final byte[] INDICES_DATA = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };

    /**
     * The width of the quad.
     */
    private float width;
    /**
     * The height of the quad.
     */
    private float height;

    /**
     * Instantiates a new <code>Quad</code> extending one unit from its center along
     * the X-axis and Y-axis.
     */
    public Quad() {
        createMesh(1, 1);
    }

    /**
     * Instantiates a new <code>Quad</code> extending from its center along the
     * X-axis and Y-axis.
     * 
     * @param width  The width of the quad (&gt;0).
     * @param height The height of the quad (&gt;0).
     */
    public Quad(float width, float height) {
        createMesh(width, height);
    }

    /**
     * Sets the dimension of the <code>Quad</code> with the provided width and
     * height, by altering its vertices.
     * 
     * @param width  The width of the quad (&gt;0).
     * @param height The height of the quad (&gt;0).
     * @return The quad for chaining purposes (not null).
     */
    public Quad setDimension(float width, float height) {
        Validator.positive(width, "The width must be strictly positive!");
        Validator.positive(height, "The width must be strictly positive!");
        if (this.width == width && this.height == height) {
            return this;
        }

        this.width = width;
        this.height = height;
        updateMesh();

        return this;
    }

    /**
     * Creates the initial mesh data (position, textures coordinates, normals and
     * indices) for the <code>Quad</code>.
     * 
     * @param width  The width of the quad (&gt;0).
     * @param height The height of the quad (&gt;0).
     */
    private void createMesh(float width, float height) {
        setupBuffer(VertexBufferType.POSITION, Usage.STATIC_DRAW, BufferUtils.createFloatBuffer(4 * 3));
        setupBuffer(VertexBufferType.TEX_COORD, Usage.STATIC_DRAW, TEX_COORDS_DATA);
        setupBuffer(VertexBufferType.NORMAL, Usage.STATIC_DRAW, NORMALS_DATA);

        var indexBuffer = BufferUtils.createByteBuffer(INDICES_DATA.length);
        indexBuffer.put(INDICES_DATA);
        indexBuffer.rewind();
        setupBuffer(VertexBufferType.INDEX, Usage.STATIC_DRAW, indexBuffer);

        setDimension(width, height);
    }

    /**
     * Update the mesh vertices according to the width and height of the
     * <code>Quad</code>.
     */
    private void updateMesh() {
        var x0 = -width / 2.0f;
        var y0 = -height / 2.0f;
        var x1 = width / 2.0f;
        var y1 = height / 2.0f;

        var vertex = getBuffer(VertexBufferType.POSITION).clear();
        vertex.put(x0).put(y1).put(0);
        vertex.put(x0).put(y0).put(0);
        vertex.put(x1).put(y0).put(0);
        vertex.put(x1).put(y1).put(0);
    }

    /**
     * Return the width of the <code>Quad</code>, the extent along the X-axis.
     * 
     * @return The width of the quad (&gt;0).
     */
    public float getWidth() {
        return width;
    }

    /**
     * Return the height of the <code>Quad</code>, the extent along the Y-axis.
     * 
     * @return The height of the quad (&gt;0).
     */
    public float getHeight() {
        return height;
    }
}
