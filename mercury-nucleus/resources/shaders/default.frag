#version 330

out vec4 frag_Color;

uniform sampler2D texture_sampler;

in vec2 frag_TexCoord;
in vec4 viewPos;

struct Fog {
	
	vec4 color;
	float density;
};

uniform Fog fog;

void main() {
	
	vec4 baseColor = texture(texture_sampler, frag_TexCoord);
	float fogFactor = 1.0 - clamp(exp(-pow(fog.density * abs(viewPos.z/viewPos.w), 2)), 0.0, 1.0);
	frag_Color = mix(baseColor, fog.color, fogFactor);
}