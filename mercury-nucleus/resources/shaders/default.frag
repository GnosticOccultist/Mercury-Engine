#version 330

#ifdef USE_FOG
#import "/shaders/Fog.glsl"
#endif

out vec4 frag_Color;

#ifdef USE_TEXTURE
	uniform sampler2D texture_sampler;
#endif

uniform vec4 diffuseColor;

#ifdef USE_TEXTURE
	in vec2 frag_TexCoord;
#endif

#ifdef USE_FOG
	in vec4 viewPos;
	uniform Fog fog;
#endif

void main() {
	
	vec4 baseColor = diffuseColor;
	
	#ifdef USE_TEXTURE
		baseColor = texture(texture_sampler, frag_TexCoord);
	#endif
	
	#ifdef USE_FOG
		frag_Color = mixFogColor(baseColor, fog.color, fog.density, abs(viewPos.z / viewPos.w));
	#else
		frag_Color = baseColor;
	#endif
}