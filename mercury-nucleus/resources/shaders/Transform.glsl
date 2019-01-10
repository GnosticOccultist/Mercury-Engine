
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;
uniform mat4 viewProjectionModelMatrix;

vec4 computePosition(vec3 position) {
	return viewProjectionModelMatrix * vec4(position, 1.0);
}