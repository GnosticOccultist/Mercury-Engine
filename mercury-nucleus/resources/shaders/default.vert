#version 330

#import "/shaders/Transform.glsl"

layout (location = 0) in vec3 position;
#ifdef USE_TEXTURE
layout (location = 1) in vec2 texCoord;
#endif
layout (location = 2) in vec3 normal;
#ifdef INSTANCING
layout (location = 3) in mat4 instanceMatrix;
#endif

#ifdef USE_FOG
	out vec4 viewPos;
#endif

#ifdef USE_ATLAS
	uniform float cols;
	uniform float rows;
	uniform vec2 uvOffset;
#endif

#ifdef USE_TEXTURE
	out vec2 frag_TexCoord;
#endif

void main() {

	#ifdef USE_FOG
		viewPos = viewMatrix * modelMatrix * vec4(position, 1.0);
	#endif
	
	#ifdef USE_TEXTURE
		frag_TexCoord = texCoord;
	#endif
	
	#ifdef USE_ATLAS
		// Support for texture atlas, update texture coordinates
		float x = (texCoord.x / cols) + uvOffset.x;
		float y = (texCoord.y / rows) + uvOffset.y;
		frag_TexCoord = vec2(x, y);
	#endif
	
	#ifdef INSTANCING
		gl_Position = computeInstancePosition(position, instanceMatrix);
	#else
		gl_Position = computePosition(position);
	#endif
}