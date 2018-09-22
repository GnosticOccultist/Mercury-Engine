#version 330

layout (location = 0) in vec3 position;

uniform mat4 projectionMatrix;

void main() {
	
	gl_Position = vec4(position, 1.0);
}