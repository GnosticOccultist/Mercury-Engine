// The fog structure filled with the fr.mercury.nucleus.scenegraph.environment.Fog class
struct Fog {
	vec4 color;
	float density;
};

// Calculates the fog factor to be applied when mixing object's color and fog's color.
float calculateFogginess(float density, float distance) {
	float fogFactor = exp(-pow(density * distance, 2));
	fogFactor = 1.0 - fogFactor;
    fogFactor = clamp(fogFactor, 0.0, 1.0);
    
    return fogFactor;
}

// Calcualtes the final color of an object based on its color, the fog color & density and the distance.
vec4 mixFogColor(vec4 baseColor, vec4 fogColor, float density, float distance) {
	return mix(baseColor, fogColor, calculateFogginess(density, distance));
}