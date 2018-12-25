#version 330

#import "/shaders/Fog.glsl"

out vec4 frag_Color;

uniform sampler2D texture_sampler;

in vec2 frag_TexCoord;
in vec4 viewPos;

uniform Fog fog;

void main() {
	
	vec4 baseColor = texture(texture_sampler, frag_TexCoord);
	frag_Color = mixFogColor(baseColor, fog.color, fog.density, abs(viewPos.z / viewPos.w));
}