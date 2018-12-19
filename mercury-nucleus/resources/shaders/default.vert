#version 330

#import "/shaders/Transform.glsl"

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 normal;

out vec2 frag_TexCoord;

void main() {

	gl_Position = computePosition(position);
	
	frag_TexCoord = texCoord;
}