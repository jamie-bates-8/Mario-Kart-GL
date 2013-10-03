#extension GL_ARB_gpu_shader5 : enable

const float ETA_RED   = 0.95;
const float ETA_GREEN = 0.97; // Ratio of indices of refraction
const float ETA_BLUE  = 0.99;

const float FRESNEL_POWER = 5.0;
const float REFLECTANCE = ((1.0 - ETA_BLUE) * (1.0 - ETA_BLUE)) / ((1.0 + ETA_BLUE) * (1.0 + ETA_BLUE));

varying vec3 reflectDir;

varying vec3 refractRed;
varying vec3 refractGreen;
varying vec3 refractBlue;

varying float ratio;

uniform mat4 cameraMatrix;

uniform float eta;
uniform float reflectance;


void main()
{
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	vec3 position = (vertex / vertex.w).xyz;
	
	vec3 eyeDir       = normalize(vertex.xyz);
	vec3 vertexNormal = normalize(gl_NormalMatrix * gl_Normal);
	
	ratio = REFLECTANCE + (1.0 - REFLECTANCE) * pow((1.0 - dot(-eyeDir.xyz, vertexNormal.xyz)), FRESNEL_POWER);
	ratio = reflectance + (1.0 - reflectance) * pow((1.0 - dot(-eyeDir.xyz, vertexNormal.xyz)), FRESNEL_POWER);
	
	eyeDir       = (inverse(cameraMatrix) * vec4(eyeDir, 1.0)).xyz;
	vertexNormal = (inverse(cameraMatrix) * vec4(vertexNormal, 0.0)).xyz;
	
	reflectDir = reflect(eyeDir, vertexNormal);
	
	//refractRed   = refract(eyeDir, vertexNormal, ETA_RED);
	//refractGreen = refract(eyeDir, vertexNormal, ETA_GREEN);
	//refractBlue  = refract(eyeDir, vertexNormal, ETA_BLUE);
	
	refractRed   = refract(eyeDir, vertexNormal, eta - 0.01);
	refractGreen = refract(eyeDir, vertexNormal, eta);
	refractBlue  = refract(eyeDir, vertexNormal, eta + 0.01);

	gl_Position = ftransform();
}