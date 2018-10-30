vec4 computePosition(vec3 position, mat4 viewProjectionWorldMatrix) {
	return viewProjectionWorldMatrix * vec4(position, 1.0);
}