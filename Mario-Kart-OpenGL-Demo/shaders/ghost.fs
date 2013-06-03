varying vec3 Reflect;
varying vec3 Refract;
varying float Ratio;

uniform samplerCube cubeMap;
uniform float opacity;

void main()
{
	vec3 refractColor = vec3(textureCube(cubeMap, Refract));
	vec3 reflectColor = vec3(textureCube(cubeMap, Reflect));
	vec3 color = mix(refractColor, reflectColor, Ratio);
	gl_FragColor = vec4(color, opacity);
}