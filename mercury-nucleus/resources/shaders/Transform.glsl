
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;
uniform mat4 viewProjectionModelMatrix;

vec4 computePosition(vec3 position) {
	return viewProjectionModelMatrix * vec4(position, 1.0);
}

vec4 computeWorldPosition(vec3 position) {
	return modelMatrix * vec4(position, 1.0);
}

vec4 computeViewPosition(vec3 position) {
	return viewMatrix * vec4(position, 1.0);
}

vec4 computeProjPosition(vec3 position) {
	return projectionMatrix * vec4(position, 1.0);
}

vec4 computeInstancePosition(vec3 position, mat4 instanceMatrix) {
	return instanceMatrix * vec4(position, 1.0);
}

vec4 computePosition(vec2 position) {
	return viewProjectionModelMatrix * vec4(position, 0.0, 1.0);
}