package fr.mercury.nucleus.renderer.logic.state;

public abstract class RenderState {
	
	public abstract RenderState enable();
	
	public abstract RenderState disable();
	
	public abstract Type type();
	
	public abstract void reset();
	
	public enum Type {
		
		FACE_CULLING;
	}
}
