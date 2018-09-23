#version 330

layout (location = 0) in vec3 position;

uniform mat4 transformMatrix;

void main() {
	
	gl_Position = transformMatrix * vec4(position, 1.0);
}