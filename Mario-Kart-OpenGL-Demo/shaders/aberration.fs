varying vec3 Reflect;
varying vec3 RefractR;
varying vec3 RefractG;
varying vec3 RefractB;
varying float Ratio;

uniform samplerCube cubeMap;
uniform float opacity;

void main()
{
	vec3 refractColor, reflectColor;
	
	refractColor.r = vec3(textureCube(cubeMap, RefractR)).r;
	refractColor.g = vec3(textureCube(cubeMap, RefractG)).g;
	refractColor.b = vec3(textureCube(cubeMap, RefractB)).b;
	
	reflectColor = vec3(textureCube(cubeMap, Reflect));
	vec3 color = mix(refractColor, reflectColor, Ratio);
	gl_FragColor = vec4(color, opacity);
}