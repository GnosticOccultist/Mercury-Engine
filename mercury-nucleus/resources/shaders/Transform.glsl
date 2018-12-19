
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 worldMatrix;
uniform mat4 viewProjectionWorldMatrix;

vec4 computePosition(vec3 position) {
	return viewProjectionWorldMatrix * vec4(position, 1.0);
}