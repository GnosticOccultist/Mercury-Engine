package fr.mercury.nucleus.renderer.opengl.vertex;

import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL33C;

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
	 * The location of the attribute.
	 */
	private int location = UNKNOWN_LOCATION;
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
		this(name, location, 0, 1);
	}
	
	public VertexAttribute(String name, int location, int divisor, int span) {
		this.name = name;
		this.location = location;
		this.divisor = divisor;
		this.span = span;
	}
	
	@OpenGLCall
	public void enable() {
		assert location >= 0;
		
		if(useMultipleLocations()) {
			for(int loc = location; loc < location + span; loc++) {
				GL33C.glEnableVertexAttribArray(loc);
			}
		} else {
			GL33C.glEnableVertexAttribArray(location);
		}
	}
	
	@OpenGLCall
	public void bindAttribute(VertexBuffer buffer) {
		buffer.upload();
		
		var type = buffer.getVertexBufferType();
		var size = buffer.getSize();
		var format = buffer.getFormat() == null ? 
				type.getPreferredFormat() : buffer.getFormat();
		// Normalized for floating-point data type isn't possible, disable it.
		var normalized = format.isFloatingPoint() ? false : buffer.isNormalized();
		
		if(useMultipleLocations()) {
			var bytesPerData = size * format.getSizeInByte();
			var stride = span * bytesPerData;
			
			for(int i = 0; i < span; i++) {
				GL20C.glVertexAttribPointer(location + i, size, VertexBufferType.getOpenGLFormat(format), 
						normalized, stride, i * bytesPerData);
				GL33C.glVertexAttribDivisor(location + i, divisor);
			}
		} else {
			GL20C.glVertexAttribPointer(location, size, VertexBufferType.getOpenGLFormat(format), 
					normalized, 0, 0);
			GL33C.glVertexAttribDivisor(location, divisor);
		}
	}
	
	@OpenGLCall
	public void disable() {
		assert location >= 0;
		
		if(useMultipleLocations()) {
			for(int loc = location; loc < location + span; loc++) {
				GL33C.glDisableVertexAttribArray(loc);
			}
		} else {
			GL33C.glDisableVertexAttribArray(location);
		}
	}
	
	private boolean useMultipleLocations() {
		return span > 1;
	}
	
	public String getName() {
		return name;
	}
	
	public int getLocation() {
		return location;
	}
	
	@Override
	public String toString() {
		return "Attribute { name= " + name + ", loc= " + location + ", div= " + divisor + " }";
	}
}
