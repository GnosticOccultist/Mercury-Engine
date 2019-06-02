package fr.mercury.nucleus.texture;

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
	 * The index of the image to use
	 */
	private int index = 0;
	
	public TextureAtlas(int numCols, int numRows) {
		this.numCols = numCols;
		this.numRows = numRows;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getNumCols() {
		return numCols;
	}
	
	public int getNumRows() {
		return numRows;
	}
	
	@Override
	protected TextureType getType() {
		return TextureType.TEXTURE_2D;
	}
}
