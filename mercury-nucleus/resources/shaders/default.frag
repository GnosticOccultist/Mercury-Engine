#version 330 core

#ifdef USE_FOG
#import "/lib/Fog.glsl"
#endif

out vec4 frag_Color;

#ifdef USE_TEXTURE
	uniform sampler2D texture_sampler;
#else
	// Use a color at least.
	uniform vec4 diffuseColor;
#endif

#ifdef USE_TEXTURE
	in vec2 frag_TexCoord;
#endif

#ifdef USE_FOG
	in vec4 viewPos;
	uniform Fog fog;
#endif

void main() {
	
	vec4 baseColor = vec4(1.0, 1.0, 1.0, 1.0);
	
	#ifdef USE_TEXTURE
		baseColor = texture(texture_sampler, frag_TexCoord);
	#else
		baseColor = diffuseColor;
	#endif
	
	if(baseColor.a < 0.4) {
		discard;
	}
	
	#ifdef USE_FOG
		frag_Color = mixFogColor(baseColor, fog.color, fog.density, abs(viewPos.z / viewPos.w));
	#else
		frag_Color = baseColor;
	#endif
}