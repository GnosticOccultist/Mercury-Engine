#version 330

#ifdef USE_FOG
#import "/shaders/Fog.glsl"
#endif

out vec4 frag_Color;

uniform sampler2D texture_sampler;

in vec2 frag_TexCoord;

#ifdef USE_FOG
	in vec4 viewPos;
	uniform Fog fog;
#endif

void main() {
	
	vec4 baseColor = texture(texture_sampler, frag_TexCoord);
	#ifdef USE_FOG
		frag_Color = mixFogColor(baseColor, fog.color, fog.density, abs(viewPos.z / viewPos.w));
	#else
		frag_Color = baseColor;
	#endif
}