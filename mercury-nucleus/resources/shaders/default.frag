#version 330

out vec4 frag_Color;

uniform sampler2D texture_sampler;

in vec2 frag_TexCoord;

void main() {
	
	vec4 baseColor = texture(texture_sampler, frag_TexCoord);
	
	frag_Color = baseColor;
}