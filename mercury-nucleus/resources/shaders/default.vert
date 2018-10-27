#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 normal;

uniform mat4 projectionMatrix;

out vec2 frag_TexCoord;

void main() {

	gl_Position = projectionMatrix * vec4(position, 1.0);
	
	frag_TexCoord = texCoord;
}