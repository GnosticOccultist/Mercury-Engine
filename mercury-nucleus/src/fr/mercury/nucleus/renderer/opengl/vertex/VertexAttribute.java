package fr.mercury.nucleus.renderer.opengl.vertex;

import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL33C;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.utils.OpenGLCall;

public class VertexAttribute {

	/**
	 * The location for an unknown attribute.
	 */
	public static final int UNKNOWN_LOCATION = -1;

	/**
	 * The name of the attribute.
	 */
	private String name;
	/**
	 * The type of VBO storing the attribute data.
	 */
	private String bufferType;
	/**
	 * The location of the attribute.
	 */
	private int location = UNKNOWN_LOCATION;
	/**
	 * The offset in bytes between consecutive vertex data.
	 */
	private int stride = 0;
	/**
	 * The offset in bytes of the first vertex data in the buffer.
	 */
	private int offset = 0;
	/**
	 * The divisor to modify the rate at which the attribute advance during
	 * instanced rendering.
	 */
	private int divisor = 0;
	/**
	 * The span of the attribute.
	 */
	private int span = 1;

	public VertexAttribute(String name, int location) {
		this(name, location, 0, 0);
	}

	public VertexAttribute(String name, int location, int stride, int offset) {
		this(name, null, location, stride, offset, 0, 1);
	}

	public VertexAttribute(String name, String bufferType, int location, int stride, int offset, int divisor, int span) {
		Validator.nonEmpty(name, "The name of the attribute can't be empty or null!");
		Validator.inRange(location, 0, Integer.MAX_VALUE /* TODO: Use capabilities instead */);
		Validator.nonNegative(stride, "The stride in bytes can't be negative!");
		Validator.nonNegative(offset, "The offset in bytes can't be negative!");
		Validator.nonNegative(divisor, "The divisor can't be negative!");
		Validator.positive(span, "The span must be strictly positive!");

		this.name = name;
		this.bufferType = bufferType;
		this.location = location;
		this.stride = stride;
		this.offset = offset;
		this.divisor = divisor;
		this.span = span;
	}

	@OpenGLCall
	public void enable() {
		assert location >= 0;

		if (useMultipleLocations()) {
			for (int loc = location; loc < location + span; loc++) {
				GL33C.glEnableVertexAttribArray(loc);
			}
		} else {
			GL33C.glEnableVertexAttribArray(location);
		}
	}

	@OpenGLCall
	public void bindAttribute(VertexBuffer buffer) {
		// First bind the concerned VBO.
		buffer.bind();

		var type = buffer.getVertexBufferType();
		var size = buffer.getSize();
		var format = buffer.getFormat() == null ? type.getPreferredFormat() : buffer.getFormat();
		// Normalized for floating-point data type isn't possible, disable it.
		var normalized = format.isFloatingPoint() ? false : buffer.isNormalized();

		var stride = this.stride == 0 ? buffer.getStride() : this.stride;
		var offset = this.offset == 0 ? buffer.getOffset() : this.offset;

		if (useMultipleLocations()) {
			var bytesPerData = size * format.getSizeInByte();
			var matStride = stride != 0 ? stride : span * bytesPerData;

			for (int i = 0; i < span; i++) {
				GL20C.glVertexAttribPointer(location + i, size, VertexBufferType.getOpenGLFormat(format), normalized,
						matStride, stride + i * bytesPerData /* Initial stride + offset of location */);
				GL33C.glVertexAttribDivisor(location + i, divisor);
			}
		} else {
			GL20C.glVertexAttribPointer(location, size, VertexBufferType.getOpenGLFormat(format), normalized, stride,
					offset);
			GL33C.glVertexAttribDivisor(location, divisor);
		}
	}

	@OpenGLCall
	public void disable() {
		assert location >= 0;

		if (useMultipleLocations()) {
			for (int loc = location; loc < location + span; loc++) {
				GL33C.glDisableVertexAttribArray(loc);
			}
		} else {
			GL33C.glDisableVertexAttribArray(location);
		}
	}

	/**
	 * Return whether the <code>VertexAttributes</code> uses multiple vertex
	 * attribute location in the shader file, meaning its span is greater than one.
	 * <p>
	 * This is used when the vertex data passed to the shader is using more than 4
	 * components such as matrices (mat4 or mat3).
	 * 
	 * @return Whether the attribute is spanning over multiple locations.
	 */
	private boolean useMultipleLocations() {
		return span > 1;
	}

	public String getName() {
		return name;
	}

	public String getBufferType() {
		return bufferType;
	}

	@Override
	public String toString() {
		var buffType = (bufferType == null || bufferType.isEmpty()) ? "" : ", bufferType= " + bufferType;
		return "Attribute { name= " + name + buffType + ", loc= " + location + ", stride= " + stride + ", offset= "
				+ offset + ", div= " + divisor + ", span= " + span + " }";
	}
}
