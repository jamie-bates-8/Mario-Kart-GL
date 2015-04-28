varying vec3 reflectDir;

varying vec3 refractRed;
varying vec3 refractGreen;
varying vec3 refractBlue;

varying float ratio;

uniform samplerCube cubeMap; 
uniform float opacity;

void main()
{
	vec3 refractColor, reflectColor;
	
	refractColor.r = vec3(textureCube(cubeMap, refractRed)).r;
	refractColor.g = vec3(textureCube(cubeMap, refractGreen)).g;
	refractColor.b = vec3(textureCube(cubeMap, refractBlue)).b;
	
	reflectColor = vec3(textureCube(cubeMap, reflectDir));
	
	vec3 color = mix(refractColor, reflectColor, ratio);
	
	gl_FragData[0] = vec4(color, 1.0);
	gl_FragData[1] = vec4(0.0);
}