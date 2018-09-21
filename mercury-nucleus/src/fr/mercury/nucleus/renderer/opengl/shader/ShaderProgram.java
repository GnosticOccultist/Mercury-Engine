package fr.mercury.nucleus.renderer.opengl.shader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import fr.alchemy.utilities.Validator;
import fr.mercury.nucleus.renderer.opengl.GLObject;
import fr.mercury.nucleus.utils.MercuryException;
import fr.mercury.nucleus.utils.OpenGLCall;

/**
 * <code>ShaderProgram</code> is a program defined by the user to compute rendering effects 
 * on the graphics hardware (GPU). The object to render go through a rendering pipeline to determine 
 * positions of vertices, vertices subdivisions, pixels colors, shading, lighting, normal/bump mapping 
 * and more effects.
 * <p>
 * In a final stage, it can also be used to apply a post-processing effect to the scene using a framebuffer
 * altering every desired objects (SSAO, Fog, FXAA/MSSA).
 * 
 * @author GnosticOccultist
 */
public final class ShaderProgram extends GLObject {
	
	/**
	 * The list of shader sources.
	 */
	private final List<ShaderSource> sources;
	
	/**
	 * Instantiates a new <code>ShaderProgram</code> with empty sources.
	 * <p>
	 * Please use {@link #attachSource(ShaderSource)} to add a <code>ShaderSource</code> and 
	 * {@link #upload()} to upload the program to the GPU.
	 */
	public ShaderProgram() {
		this.sources = new ArrayList<>();
	}
	
	@Override
	@OpenGLCall
	public void upload() {
		create();
		
		for(var source : sources) {
			source.upload();
			
			// Attach the shader source.
			GL20.glAttachShader(id, source.getID());
		}
		
		// Link the program.
		GL20.glLinkProgram(id);
		
		// If failed, show info log.
		if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			throw new MercuryException("Error while linking shader program: "
					+ GL20.glGetProgramInfoLog(id, 1024));
		}
		
		// TODO: Should be called by a rendering manager.
		GL20.glUseProgram(id);
	}
	
	/**
	 * Attach a <code>ShaderSource</code> to the <code>ShaderProgram</code>.
	 * <p>
	 * The source cannot be null.
	 * 
	 * @param source The source to attach.
	 * @return		 The program with the attached source.
	 */
	public ShaderProgram attachSource(ShaderSource source) {
		Validator.nonNull(source, "The shader source cannot be null.");
		
		sources.add(source);
		return this;
	}
	
	@Override
	@OpenGLCall
	protected Integer acquireID() {
		return GL20.glCreateProgram();
	}
	
	@Override
	@OpenGLCall
	protected Consumer<Integer> deleteAction() {
		return GL20::glDeleteProgram;
	}
}
