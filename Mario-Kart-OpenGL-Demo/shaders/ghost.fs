varying vec3 reflectDir;
varying vec3 refractDir;

varying float ratio;

uniform samplerCube cubeMap;
uniform float opacity;

void main()
{
	vec3 refractColor = vec3(textureCube(cubeMap, refractDir));
	vec3 reflectColor = vec3(textureCube(cubeMap, reflectDir));
	vec3 color = mix(refractColor, reflectColor, ratio);
	
	gl_FragColor = vec4(color, 1.0);
}