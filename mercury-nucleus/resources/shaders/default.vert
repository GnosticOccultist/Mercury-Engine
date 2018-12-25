#version 330

#import "/shaders/Transform.glsl"

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 normal;

out vec2 frag_TexCoord;
out vec4 viewPos;

void main() {

	viewPos = viewMatrix * modelMatrix * vec4(position, 1.0);
	gl_Position = computePosition(position);
	
	frag_TexCoord = texCoord;
}