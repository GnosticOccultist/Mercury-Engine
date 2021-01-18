package fr.mercury.nucleus.texture;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.math.objects.Vector2f;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderProgram;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.Uniform.UniformType;
import fr.mercury.nucleus.renderer.opengl.shader.uniform.UniformField;
import fr.mercury.nucleus.scenegraph.Mesh;

/**
 * <code>TextureAtlas</code> works the same way as a {@link Texture2D} but is  composed of multiple sub-textures which can be each one used 
 * in a {@link ShaderProgram} by setting the required index.
 * <p>
 * A texture atlas is mainly used to save <code>OpenGL</code> texture binding unit and switching between textures to increase performance. 
 * The shader takes in charge the computations of the appropriate texture coordinates to render properly the sub-texture on a {@link Mesh}.
 * 
 * @author GnosticOccultist
 */
public class TextureAtlas extends Texture {

    /**
     * The number of columns in the texture.
     */
    private final int numCols;
    /**
     * The number of rows in the texture.
     */
    private final int numRows;
    /**
     * The index of the image to use.
     */
    private int index;

    /**
     * Instantiates a new <code>TextureAtlas</code> with the given number of rows
     * and columns.
     * <p>
     * To be usable in an OpenGL context, you must call {@link #upload()} to upload
     * it to the GPU.
     *
     * @param numCols The number of columns in the atlas (&gt;1).
     * @param numRows The number of rows in the atlas (&gt;1).
     */
    public TextureAtlas(int numCols, int numRows) {
        this(numCols, numRows, 0);
    }

    /**
     * Instantiates a new <code>TextureAtlas</code> with the given number of rows
     * and columns, as well as the specified atlas index to use.
     * <p>
     * To be usable in an OpenGL context, you must call {@link #upload()} to upload
     * it to the GPU.
     *
     * @param numCols The number of columns in the atlas (&gt;1).
     * @param numRows The number of rows in the atlas (&gt;1).
     * @param index   The index of the image to use (&ge;0).
     */
    public TextureAtlas(int numCols, int numRows, int index) {
        Validator.inRange(numCols, 1, Integer.MAX_VALUE);
        Validator.inRange(numRows, 1, Integer.MAX_VALUE);
        Validator.nonNegative(index, "The atlas index can't be negative!");

        this.numCols = numCols;
        this.numRows = numRows;
        this.index = index;
    }

    /**
     * Return the atlas index of the <code>TextureAtlas</code> to use.
     *
     * @return The atlas index to use (&ge;0).
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the atlas index of the <code>TextureAtlas</code> to use.
     * 
     * @param index The atlas index to use (&ge;0).
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Return the number of columns inside the <code>TextureAtlas</code>.
     * 
     * @return The number of columns in the atlas (&gt;1).
     */
    @UniformField(name = "cols", type = UniformType.FLOAT)
    public float getNumCols() {
        return numCols;
    }

    /**
     * Return the number of rows inside the <code>TextureAtlas</code>.
     * 
     * @return The number of rows in the atlas (&gt;1).
     */
    @UniformField(name = "rows", type = UniformType.FLOAT)
    public float getNumRows() {
        return numRows;
    }

    /**
     * Compute the texture coordinates offset to use in the {@link ShaderProgram} to
     * render the correct atlas index.
     * 
     * @return A new vector containing the coordinates offset.
     */
    @UniformField(name = "uvOffset", type = UniformType.VECTOR2F)
    public Vector2f offset() {
        int col = index / numRows;
        int row = index % numRows;
        return new Vector2f(col, row);
    }

    /**
     * Creates and return a copy of the <code>TextureAtlas</code>'s implementation.
     * Note that the {@link Image} isn't copied an alias is being created.
     * <p>
     * To be usable in an OpenGL context, you must call {@link #upload()} to upload
     * it to the GPU.
     * 
     * @return A copy of the texture, not yet uploaded (not null).
     */
    @Override
    public TextureAtlas copy() {
        var copy = new TextureAtlas(numCols, numRows, index);
        copy.setTextureState(currentState, toApply);
        copy.setImage(image);

        return copy;
    }

    /**
     * Return the {@link TextureType type} of the <code>TextureAtlas</code>:
     * {@link TextureType#TEXTURE_2D}.
     * 
     * @return The 2D texture type.
     */
    @Override
    protected TextureType getType() {
        return TextureType.TEXTURE_2D;
    }
}
